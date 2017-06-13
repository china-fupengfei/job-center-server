package code.ponfee.job.common;

import code.ponfee.commons.util.Networks;

/**
 * 常量类
 * @author fupf
 */
public final class Constants {
    public static final String CACHE_KEY_PREFIX = "sched:"; // cache key prefix of job center
    public static final String IP_ADDRESS = Networks.getSiteIp();
    public static final Integer PLACEHOLDER = Integer.MIN_VALUE;
    public static final int MAX_ERROR_LENGTH = 8000;
}
