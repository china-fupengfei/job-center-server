package code.ponfee.job.main;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 用start.sh启动执行的是com.alibaba.dubbo.container.Main类
 * 用java -jar job-center-server.jar启动时才是执行的此类
 * @author fupf
 */
public class Provider {

    private static ClassPathXmlApplicationContext context;

    public static void main(String[] args) {
        context = new ClassPathXmlApplicationContext(new String[] { "META-INF/spring/job-center-provider.xml" });
        context.start();
        System.out.println("==========================调度中心已启动=========================");
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
