//package code.ponfee.job.sched.handler;
//
//import static java.text.MessageFormat.format;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.concurrent.Callable;
//import java.util.concurrent.Future;
//import java.util.concurrent.FutureTask;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import cn.bus.service.inner.DepotScheduleCrawler;
//import cn.commons.biz.BizType;
//import code.ponfee.job.model.SchedJob;
//import code.ponfee.job.common.Commons;
//import code.ponfee.job.common.Constants;
//import code.ponfee.job.exception.JobExecuteExecption;
//import cn.third.bean.HotLine;
//import cn.third.bean.param.PagerParam;
//import cn.third.service.IDepotProxyService;
//import code.ponfee.commons.json.Jsons;
//import code.ponfee.commons.model.Pager;
//import code.ponfee.commons.util.Dates;
//import code.ponfee.commons.util.ObjectUtils;
//import code.ponfee.commons.util.SpringContextHolder;
//
///**
// * 车站接口数据爬取调度处理器
// * @author: fupf
// */
//@JobHandlerMeta(name = "车站接口数据爬取")
//public class DepotCrawlerJobHandler implements JobHandler {
//    private static Logger logger = LoggerFactory.getLogger(DepotCrawlerJobHandler.class);
//    private static final int LOAD_PAG_SIZE = 1000;
//    private static final int LOAD_THREAD_COUNT = 5;
//
//    @Override
//    public void handle(SchedJob job) {
//        Date thisSchedTime = job.getLastSchedTime();
//        if (logger.isInfoEnabled()) {
//            logger.info("depot crawler job execute for [{} {}]", job.getId(), Dates.format(thisSchedTime));
//        }
//
//        // 获取爬取数据请求参数
//        CrawlerJobParams params = Jsons.NORMAL.fromJson(job.getExecParams(), CrawlerJobParams.class);
//        List<HotLine> datas = loadHotLine(params.getDepotId(), LOAD_PAG_SIZE, LOAD_THREAD_COUNT);
//        if (datas == null || datas.isEmpty()) {
//            throw new JobExecuteExecption("hotline data is empty");
//        }
//
//        List<Date> dates = Commons.listPreSaleDate(thisSchedTime, params.getDays());
//
//        // 均匀分配到多个线程执行
//        ThreadPoolTaskExecutor task = (ThreadPoolTaskExecutor) SpringContextHolder.getBean("schedJobExecutor");
//        int[] array = ObjectUtils.equalize(datas.size(), task.getCorePoolSize());
//
//        List<Future<String>> calls = new ArrayList<>();
//        //CountDownLatch latch = new CountDownLatch(array.length);
//        for (int index = 0, start = index, i = 0; i < array.length; start = index, i++) {
//            index += array[i];
//            calls.add(task.submit(new DepotCrawlerExecutor(job, datas, dates, start, index)));
//        }
//
//        // 等待获取执行结果
//        StringBuffer buff = new StringBuffer();
//        for (Future<String> future : calls) {
//            try {
//                buff.append(future.get());
//            } catch (Exception e) {
//                logger.error("future get error -> depot crawler", e);
//            }
//        }
//        //latch.await();
//
//        if (buff.length() > 0) throw new JobExecuteExecption(buff.toString());
//    }
//
//    @Override
//    public boolean verifyJobParams(String params) {
//        try {
//            CrawlerJobParams _params = Jsons.NORMAL.fromJson(params, CrawlerJobParams.class);
//            if (_params.depotId > 0 && _params.days > 0) return true;
//        } catch (Exception e) {
//        }
//        return false;
//    }
//    /**
//     * 预售期时间列表
//     * @param refer
//     * @param days
//     * @return
//     */
//    public static List<Date> listPreSaleDate(Date refer, int days) {
//        List<Date> dates = new ArrayList<>();
//        for (int day = 0; day < days; day++) {
//            dates.add(Dates.addDays(refer, day));
//        }
//        return dates;
//    }
//
//    /**
//     * 加载热门线路
//     * @param depotId
//     * @param PagerSize
//     * @return
//     */
//    private List<HotLine> loadHotLine(int depotId, int PagerSize, int threadCount) {
//        PagerParam param = new PagerParam();
//        param.setInterfaceId(depotId);
//        param.setPagerNum(1);
//        param.setPagerSize(PagerSize);
//
//        IDepotProxyService depotService = ((IDepotProxyService) SpringContextHolder.getBean("depotProxyService"));
//        Pager<HotLine> Pager = depotService.getHotLineForPager(param);
//        List<HotLine> list = Pager.getRows();
//        if (Pager.getPagers() <= 1) return list;
//
//        int[] array = ObjectUtils.equalize(Pager.getPagers() - 1, threadCount);
//        List<Future<List<HotLine>>> futures = new ArrayList<>();
//        for (int index = 2, start = index, i = 0; i < array.length; start = index, i++) {
//            index += array[i];
//            HotLineLoader call = new HotLineLoader(depotService, depotId, PagerSize, start, index);
//            FutureTask<List<HotLine>> future = new FutureTask<>(call);
//            futures.add(future);
//            new Thread(future).start();
//        }
//
//        for (Future<List<HotLine>> future : futures) {
//            try {
//                list.addAll(future.get());
//            } catch (Exception e) {
//                logger.error("future get error -> load hot line", e);
//            }
//        }
//
//        return list;
//    }
//
//    /**
//     * depot job参数类
//     * @author: fupf
//     */
//    public static class CrawlerJobParams implements Serializable {
//        private static final long serialVersionUID = 2742712498742363661L;
//        private int depotId;
//        private int days;
//
//        public int getDepotId() {
//            return depotId;
//        }
//
//        public int getDays() {
//            return days;
//        }
//
//        public void setDepotId(int depotId) {
//            this.depotId = depotId;
//        }
//
//        public void setDays(int days) {
//            this.days = days;
//        }
//    }
//
//    /**
//     * 热门线路数据拉取
//     * @author: fupf
//     */
//    private static final class HotLineLoader implements Callable<List<HotLine>> {
//        private final IDepotProxyService service;
//        private final int depotId;
//        private final int PagerSize;
//        private final int startPos;
//        private final int endPos;
//
//        private HotLineLoader(IDepotProxyService service, int depotId, int PagerSize, int startPos, int endPos) {
//            this.service = service;
//            this.depotId = depotId;
//            this.PagerSize = PagerSize;
//            this.startPos = startPos;
//            this.endPos = endPos;
//        }
//
//        @Override
//        public List<HotLine> call() {
//            List<HotLine> list = new ArrayList<>();
//            PagerParam param = new PagerParam();
//            param.setInterfaceId(depotId);
//            param.setPagerSize(PagerSize);
//            for (int PagerNum = startPos; PagerNum < endPos; PagerNum++) {
//                param.setPagerNum(PagerNum);
//                list.addAll(service.getHotLineForPage(param).getRows());
//            }
//            return list;
//        }
//    }
//
//    /**
//     * 车站数据爬取执行类
//     * @author: fupf
//     */
//    private static final class DepotCrawlerExecutor implements Callable<String> {
//        private static Logger logger = LoggerFactory.getLogger(DepotCrawlerExecutor.class);
//        private final SchedJob job;
//        private final List<HotLine> datas;
//        private final List<Date> dates;
//        private final int startPos;
//        private final int endPos;
//
//        private DepotCrawlerExecutor(SchedJob job, List<HotLine> datas, List<Date> dates, int startPos, int endPos) {
//            this.job = job;
//            this.datas = datas;
//            this.dates = dates;
//            this.startPos = startPos;
//            this.endPos = endPos;
//        }
//
//        @Override
//        public String call() {
//            Thread thread = Thread.currentThread();
//            String threadName = thread.getThreadGroup().getName() + "," + thread.getId() + "," + thread.getName();
//            if (logger.isInfoEnabled()) {
//                logger.info("subtask start [{}]-[{}]", job.getId() + "," + job.getName(), threadName);
//            }
//
//            DepotScheduleCrawler crawler = SpringContextHolder.getBean(DepotScheduleCrawler.class);
//            StringBuilder fail = new StringBuilder();
//            HotLine l;
//            for (int i = startPos; i < endPos; i++) {
//                l = datas.get(i);
//                for (Date date : dates) {
//                    try {
//                        crawler.crawlSchedule(BizType.getEnum(l.getBizType()), l.getInterfaceId(), l.getFromCityId(), l.getFromCityCode(), l.getFromCityName(), l.getToCityId(), l.getToCityCode(), l.getToCityName(), l.getMainFromCityId(), l.getMainFromCityName(), l.getMainToCityId(), l.getMainToCityName(), date);
//                    } catch (Exception e) {
//                        String error = format("[{0}]－[{1}]－[{2}]", date, Jsons.NORMAL.toJson(l), e.getMessage());
//                        if (fail.length() < Constants.MAX_ERROR_MSG_SIZE) fail.append(error).append("\n");
//                        logger.error("depot crawler exception {}", error, e);
//                    }
//                }
//            }
//
//            if (logger.isInfoEnabled()) {
//                logger.info("subtask end [{}]-[{}]", job.getId() + "," + job.getName(), threadName);
//            }
//            return fail.toString();
//        }
//    }
//
//    public static void main(String[] args) {
//        int[] array = ObjectUtils.equalize(4561, 5);
//        for (int index = 0, start = 0, i = 0; i < array.length; start = index, i++) {
//            index += array[i];
//            System.out.println(start + "," + index);
//        }
//    }
//}
