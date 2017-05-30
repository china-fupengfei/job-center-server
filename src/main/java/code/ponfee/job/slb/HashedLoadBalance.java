package code.ponfee.job.slb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 源地址哈希法
 * @author fupf
 */
public class HashedLoadBalance extends AbstractLoadBalance {
    private final List<String> servers;
    private final String invokeIp;

    public HashedLoadBalance(Map<String, Integer> serverMap, String invokeIp) {
        this.invokeIp = invokeIp;
        this.servers = new ArrayList<>(serverMap.keySet());
    }

    @Override
    public String select() {
        // Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE
        int index = invokeIp.hashCode();
        if (index == Integer.MIN_VALUE) index = Integer.MAX_VALUE;
        return servers.get(Math.abs(index) % servers.size());
    }

}
