package code.ponfee.job.sched.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import code.ponfee.commons.util.Dates;
import code.ponfee.commons.util.SpringContextHolder;
import code.ponfee.job.model.SchedJob;

/**
 * 测试调度类
 * @author fupf
 */
@JobHandlerMeta("测试调度类")
public class JobHandlerTest implements JobHandler {

    @Override
    public void handle(SchedJob job) {
        ThreadPoolTaskExecutor task = (ThreadPoolTaskExecutor) SpringContextHolder.getBean("schedJobExecutor");
        int size = task.getCorePoolSize();
        List<Future<Boolean>> calls = new ArrayList<>();
        System.out.println("=======job start " + Dates.format(new Date()) + "  " + job.getName());
        for (int i = 0; i < size; i++) {
            calls.add(task.submit(new TestExecutor(job)));
        }
        for (Future<Boolean> future : calls) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("=======job end " + Dates.format(new Date()) + "  " + job.getName());
    }

    @Override
    public boolean verify(SchedJob job) {
        return true;
    }

    private static final class TestExecutor implements Callable<Boolean> {
        private SchedJob job;

        private TestExecutor(SchedJob job) {
            this.job = job;
        }

        @Override
        public Boolean call() {
            Thread t = Thread.currentThread();
            String s = job.getName() + "[" + t.getThreadGroup().getName() + "-" + t.getId() + "-" + t.getName() + "]";
            System.out.println(s + " start " + Dates.format(new Date()));
            try {
                Thread.sleep(30000 + ThreadLocalRandom.current().nextInt(60000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(s + " end " + Dates.format(new Date()));

            return true;
        }
    }

}
