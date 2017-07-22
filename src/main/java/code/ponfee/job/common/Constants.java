package code.ponfee.job.common;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.resource.ResourcesScanner;
import code.ponfee.commons.util.Networks;
import code.ponfee.job.sched.handler.JobHandlerMeta;

/**
 * 常量类
 * @author fupf
 */
@SuppressWarnings("unchecked")
public final class Constants {
    public static final String CACHE_KEY_PREFIX = "sched:"; // cache key prefix of job center
    public static final String IP_ADDRESS = Networks.getSiteIp();
    public static final Integer PLACEHOLDER = Integer.MIN_VALUE;
    public static final int MAX_ERROR_LENGTH = 8000;

    public static final Map<String, String> JOB_HANDLER_CONFIGS;
    static {
        Set<Class<?>> classes = new ResourcesScanner("code.ponfee.job").scan4class(new Class[] { JobHandlerMeta.class });
        Builder<String, String> builder = ImmutableMap.builder();
        for (Class<?> clazz : classes) {
            builder.put(ClassUtils.getClassName(clazz), clazz.getAnnotation(JobHandlerMeta.class).value());
        }
        JOB_HANDLER_CONFIGS = builder.build();
    }

    public static void main(String[] args) {
        System.out.println(JOB_HANDLER_CONFIGS);
        JOB_HANDLER_CONFIGS.put("a", "b");
    }

}
