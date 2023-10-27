package cn.mapland.yuanlu.bc.chatLog;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * 过滤器
 *
 * @param <T> 数据类型
 * @author yuanlu
 */
abstract class Filter<T> {
    /**
     * 全部否定
     */
    private static final Predicate<?> ALL_FALSE = x -> false;
    /**
     * 全部过滤
     */
    private static final Filter<?> ALL_PASS = new AllPass(null);

    /**
     * 获取一个全部否定的谓词
     *
     * @param <E> 测试类型
     * @return 全部否定
     */
    @SuppressWarnings("unchecked")
    private static <E> Predicate<E> allFalse() {
        return (Predicate<E>) ALL_FALSE;
    }

    /**
     * 获取一个全部否定的过滤器
     *
     * @param <E> 测试类型
     * @return 全部否定
     */
    @SuppressWarnings("unchecked")

    private static <E> Filter<E> allPass() {
        return (Filter<E>) ALL_PASS;
    }

    /**
     * 构造谓词测试器
     * <p>
     * 将 {@code check} 置为null, 则使用 {@code contains} 匹配法, 当配置数据集中存在对象时返回true<br>
     * 否则, 将遍历 {@code data}, 数据逐一传入 {@code check} 进行检测, 其第一个数据为待检测的数据,
     * 第二个数据为配置数据集中某一项数据
     *
     * @param <E>   数据类型
     * @param data  配置数据集
     * @param check 检查方式
     * @return 构造完成的{@link Predicate}
     */
    private static <E> Predicate<E> build(Collection<E> data, BiPredicate<E, E> check) {
        if (data == null || data.isEmpty()) return allFalse();

        final Collection<E> collection;
        if (check == null) collection = new HashSet<>(data);
        else collection = new ArrayList<>(data);

        if (check == null) return collection::contains;

        return e -> {
            for (E d : collection) if (check.test(e, d)) return true;
            return false;
        };
    }

    /**
     * 检查数据是否<b>不</b>合格
     *
     * @param data 数据
     * @return 是否<b>不</b>合格
     */
    public abstract boolean check(T data);

    /**
     * 玩家过滤器
     *
     * @author yuanlu
     */
    public static final class PlayerFilter extends Filter<Player> {
        /**
         * 用户名测试器
         */
        private final transient Predicate<String> namePredicate;
        /**
         * 用户名关键字测试器
         */
        private final transient Predicate<String> keysPredicate;

        /**
         * 构造玩家过滤器
         *
         * @param toml 配置文件
         */
        private PlayerFilter(@NonNull Toml toml) {
            this.namePredicate = build(toml.getList("name"), null);
            this.keysPredicate = build(toml.getList("contain"), String::contains);
        }

        /**
         * 构造过滤器
         *
         * @param toml 配置文件
         * @return 过滤器
         */
        public static Filter<Player> get(Toml toml) {
            if (toml == null) return allPass();
            return new PlayerFilter(toml);
        }

        @Override
        public boolean check(@NonNull Player player) {
            final String name = player.getUsername();
            return namePredicate.test(name) || keysPredicate.test(name);
        }
    }

    /**
     * 服务器过滤器
     *
     * @author yuanlu
     */
    public static final class ServerFilter extends Filter<ServerInfo> {
        /**
         * 服务器名称测试器
         */
        private final transient Predicate<String> namePredicate;
        /**
         * 服务器名称关键字测试器
         */
        private final transient Predicate<String> keysPredicate;

        /**
         * 构造服务器过滤器
         *
         * @param toml 配置文件
         */
        private ServerFilter(@NonNull Toml toml) {
            this.namePredicate = build(toml.getList("name"), null);
            this.keysPredicate = build(toml.getList("contain"), String::contains);
        }

        /**
         * 构造过滤器
         *
         * @param toml 配置文件
         * @return 过滤器
         */
        public static Filter<ServerInfo> get(Toml toml) {
            if (toml == null) return allPass();
            return new ServerFilter(toml);
        }

        @Override
        public boolean check(@NonNull ServerInfo server) {
            final String name = server.getName();
            return namePredicate.test(name) || keysPredicate.test(name);
        }
    }

    /**
     * 字符串过滤器
     *
     * @author yuanlu
     */
    public static final class StringFilter extends Filter<String> {
        /**
         * 字符串前缀测试器
         */
        private final transient Predicate<String> prefixPredicate;
        /**
         * 字符串后缀测试器
         */
        private final transient Predicate<String> suffixPredicate;
        /**
         * 字符串关键字测试器
         */
        private final transient Predicate<String> containPredicate;
        /**
         * 字符串全匹配测试器
         */
        private final transient Predicate<String> equalPredicate;

        /**
         * 构造字符串过滤器
         *
         * @param toml 配置文件
         */
        private StringFilter(@NonNull Toml toml) {
            this.prefixPredicate = build(toml.getList("prefix"), String::startsWith);
            this.suffixPredicate = build(toml.getList("suffix"), String::endsWith);
            this.containPredicate = build(toml.getList("contain"), String::contains);
            this.equalPredicate = build(toml.getList("equals"), null);
        }

        /**
         * 构造过滤器
         *
         * @param toml 配置文件
         * @return 过滤器
         */
        public static Filter<String> get(Toml toml) {
            if (toml == null) return allPass();
            return new StringFilter(toml);
        }

        @Override
        public boolean check(@NonNull String string) {
            return equalPredicate.test(string) || //
                    prefixPredicate.test(string) || //
                    suffixPredicate.test(string) || //
                    containPredicate.test(string);
        }
    }

    /**
     * 全部否定
     *
     * @author yuanlu
     */
    private static final class AllPass extends Filter<Object> {
        /**
         * 构造全部否定过滤器
         *
         * @param toml 配置文件
         */
        private AllPass(Toml toml) {
        }

        @Override
        public boolean check(@NonNull Object data) {
            return false;
        }
    }
}
