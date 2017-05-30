package code.ponfee.job.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import code.ponfee.job.sched.handler.JobHandlerMeta;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.resource.ResourcesScanner;

/**
 * spring容器启动完成后执行
 * @author fupf
 */
@Component
public class SpringStartupListener implements ApplicationListener<ContextRefreshedEvent> {
    private static final Map<String, String> JOB_HANDLER_CONFIGS = new HashMap<>();

    /**
     * spring初始化完成后执行一次
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() != null) return;

        loadJobHandlers();
    }

    @SuppressWarnings("unchecked")
    private void loadJobHandlers() {
        ResourcesScanner scanner = new ResourcesScanner("code.ponfee.job");
        Set<Class<?>> classes = scanner.scan4class(new Class[]{JobHandlerMeta.class});
        for (Class<?> clazz : classes) {
            String name = clazz.getAnnotation(JobHandlerMeta.class).value();
            JOB_HANDLER_CONFIGS.put(ClassUtils.getClassName(clazz), name);
        }
    }

    public static Map<String, String> listJobHandlers() {
        Map<String, String> result = new HashMap<>();
        result.putAll(JOB_HANDLER_CONFIGS);
        return result;
    }

}
