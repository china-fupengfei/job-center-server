package code.ponfee.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import code.ponfee.job.model.SchedJob;
import code.ponfee.job.sched.handler.TesterJobHandler;
import code.ponfee.job.service.ISchedJobService;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.util.Streams;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:dubbo-consumer.xml" })
public class TestSchedJobService {

    @Resource
    private ISchedJobService schedJobService;

    @Before
    public void setup() {}

    @After
    public void teardown() {}

    @Test
    public void testAdd() {
        SchedJob job = new SchedJob();
        job.setName("调度测试类");
        job.setCronExpression("0 */1 * * * ?");
        job.setHandler("code.ponfee.job.sched.handler.TesterJobHandler");
        job.setExecParams("{\"days\":3}");
        job.setStatus(1);
        job.setCreatorId(1L);
        job.setCreatorName("fupengfei");
        Result<?> result = schedJobService.addJob(job);
        System.out.println(Jsons.NORMAL.stringify(result));
    }

    @Test
    public void testAdd2() throws IOException {
        for (int i = 0; i < 10; i++) {
            SchedJob job = new SchedJob();
            job.setName("repairBug" + i);
            job.setCronExpression("0 */2 * * * ?");
            job.setHandler(Streams.file2string(MavenProjects.getMainJavaFile(TesterJobHandler.class)));
            job.setStatus(1);
            job.setCreatorId(1L);
            job.setCreatorName("username");
            Result<?> result = schedJobService.addJob(job);
            System.out.println(Jsons.NORMAL.stringify(result));
        }
    }

    @Test
    public void testUpdate() {
        SchedJob job = new SchedJob();
        job.setId(1);
        job.setStatus(1);
        job.setName("name22");
        job.setCronExpression("*/30 * * * * ?");
        job.setExecParams("{\"depotId\":11, \"days\":3}");
        job.setHandler("code.ponfee.job.sched.handler.TesterJobHandler");
        job.setModifierId(1234L);
        job.setModifierName("李四");
        job.setVersion(2);
        Result<?> result = schedJobService.updJob(job);
        System.out.println(Jsons.NORMAL.stringify(result));
    }

    @Test
    public void testStop() {
        Result<?> result = schedJobService.stopJob(1, 16);
        System.out.println(Jsons.NORMAL.stringify(result));
    }

    @Test
    public void testStart() {
        Result<?> result = schedJobService.startJob(1, 17);
        System.out.println(Jsons.NORMAL.stringify(result));
    }

    @Test
    public void testGet() {
        Result<?> result = schedJobService.getJob(1);
        System.out.println(Jsons.NORMAL.stringify(result));
    }

    @Test
    public void testDelete() {
        Result<?> result = schedJobService.delJob(1, 123);
        System.out.println(Jsons.NORMAL.stringify(result));
    }

    @Test
    public void testQuery4Page() {
        Map<String, ?> map = new HashMap<>();
        Result<?> result = schedJobService.queryJobsForPage(map);
        System.out.println(Jsons.NORMAL.stringify(result));
    }

    @Test
    public void testTrigger() {
        Result<?> result = schedJobService.triggerJob(1);
        System.out.println(Jsons.NORMAL.stringify(result));
    }

    @Test
    public void testQueryLogsForPage() {
        Map<String, Object> map = new HashMap<>();
        map.put("jobName", "test job");
        map.put("pageSize", 0);
        Result<?> result = schedJobService.queryLogsForPage(map);
        System.out.println(Jsons.NORMAL.stringify(result));
    }

}
