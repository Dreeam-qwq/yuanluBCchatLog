/**
 * auto: <br>
 * user: yuanlu<br>
 * date: 星期日 08 12 2019
 */
package bid.yuanlu.chatLog;

import com.moandjiezana.toml.Toml;
import lombok.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 * 工具类
 *
 * @author yuanlu
 */
public final class Tool {
    /**
     * Toml的get方法
     */
    private static final Method TOML_METHOD;

    static {
        try {
            TOML_METHOD = Toml.class.getDeclaredMethod("get", String.class);
            TOML_METHOD.setAccessible(true);
        } catch (Throwable e) {
            throw new RuntimeException("无法启动: 反射 Toml 失败", e);
        }
    }

    /**
     * 获取数据, 抑制错误
     *
     * @param key  键
     * @param def  值
     * @param func 获取器
     * @return 数据
     */
    static <K, E> E get(@NonNull K key, E def, @NonNull Function<K, E> func) {
        E obj = null;
        try {
            obj = func.apply(key);
        } catch (Throwable ignore) {
        }
        return obj == null ? def : obj;
    }

    /**
     * 获取配置文件中的一个节点<br>
     * 将会尝试匹配类型, 若无匹配/null则返回默认值
     *
     * @param <E> 数据类型
     * @param key 键
     * @param def 默认值
     * @return 数据
     */
    @SuppressWarnings("unchecked")
    static <E> E getConf(@NonNull String key, E def) {
        Toml toml = Main.getMain().config;
        Object obj;
        try {
            obj = TOML_METHOD.invoke(toml, key);
        } catch (Throwable e) {
            Main.getMain().logger.error("无法调用 {}.get(String)", Toml.class.getName(), e);
            return def;
        }

        if (obj == null) return def;

        if (String.class.isAssignableFrom(obj.getClass())) return (E) obj;

        String.class.isAssignableFrom(String.class);
        if (obj instanceof List) {
            StringJoiner sj = new StringJoiner("\n");
            ((List<?>) obj).stream().map(String::valueOf).forEach(sj::add);
            return (E) sj.toString();
        }

        return def;
    }

    /**
     * 转换为Memory流
     *
     * @param in 输入流
     * @return Memory流
     * @throws IOException IO错误
     */
    public static ByteArrayInputStream toByte(InputStream in) throws IOException {
        var out = new ByteArrayOutputStream(Math.max(in.available(), 32));
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        return new ByteArrayInputStream(out.toByteArray());
    }
}
