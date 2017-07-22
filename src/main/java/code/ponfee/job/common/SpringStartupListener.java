package code.ponfee.job.common;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * spring容器启动完成后执行
 * @author fupf
 */
@Component
public class SpringStartupListener implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * spring初始化完成后执行一次
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            // do something for application init
        }
    }

}
