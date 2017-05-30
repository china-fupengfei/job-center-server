package code.ponfee.job.slb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 随机法
 * @author fupf
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    private final List<String> servers;

    public RandomLoadBalance(Map<String, Integer> serverMap) {
        this.servers = new ArrayList<>(serverMap.keySet());
    }

    @Override
    public String select() {
        return servers.get(new Random().nextInt(servers.size()));
    }

}
