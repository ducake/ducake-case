package com.ducake.config;

import org.springframework.core.NamedThreadLocal;
import org.springframework.util.ObjectUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 93477
 */
public class DataSourceContextHolder {

    private DataSourceContextHolder() {
        throw new IllegalStateException("DataSourceContextHolder class");
    }

    /**
     * 为什么要用链表存储(准确的是栈)
     * 为了支持嵌套切换，如ABC三个service都是不同的数据源
     * 其中A的某个业务要调B的方法，B的方法需要调用C的方法。一级一级调用切换，形成了链。
     * 传统的只设置当前线程的方式不能满足此业务需求，必须使用栈，后进先出。
     */
    private static final ThreadLocal<Deque<String>> LOOKUP_KEY_HOLDER = new NamedThreadLocal<Deque<String>>("dynamic-datasource") {
        @Override
        protected Deque<String> initialValue() {
            return new ArrayDeque<>();
        }
    };

    private static Map<Object, Object> datasourceMap = new ConcurrentHashMap<>(10);

    public static Map<Object, Object> getDatasourceMap() {
        return DataSourceContextHolder.datasourceMap;
    }

    public static void setDatasourceMap(Map<Object, Object> datasourceMap) {
        DataSourceContextHolder.datasourceMap = datasourceMap;
    }
    /**
     * 获得当前线程数据源
     *
     * @return 数据源名称
     */
    public static String getDataSource() {
        String dataSource = LOOKUP_KEY_HOLDER.get().peek();
        return ObjectUtils.isEmpty(dataSource) ? "default" : dataSource;
    }

    /**
     * 设置当前线程数据源
     * <p>
     * 如非必要不要手动调用，调用后确保最终清除
     * </p>
     *
     * @param dataSourceName 数据源名称
     */
    public static void setDataSource(String dataSourceName) {
        LOOKUP_KEY_HOLDER.get().push(dataSourceName);
    }


    /**
     * 清空当前线程数据源
     * <p>
     * 如果当前线程是连续切换数据源 只会移除掉当前线程的数据源名称
     * </p>
     */
    public static void removeDataSource() {
        Deque<String> deque = LOOKUP_KEY_HOLDER.get();
        deque.poll();
        if (deque.isEmpty()) {
            clearDataSource();
        }
    }

    /**
     * 强制清空本地线程
     * <p>
     * 防止内存泄漏，如手动调用了push可调用此方法确保清除
     * </p>
     */
    public static void clearDataSource() {
        LOOKUP_KEY_HOLDER.remove();
    }

}
