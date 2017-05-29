package code.ponfee.job.slb;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * 最少已使用法
 * @author: fupf
 */
public class LeastActiveBalance extends AbstractLoadBalance {
    private final TreeMap<String, Integer> servers;

    public LeastActiveBalance(Map<String, Integer> serverMap) {
        this.servers = new TreeMap<>(new MapComparator(serverMap));
        servers.putAll(serverMap);
    }

    @Override
    public String select() {
        return servers.lastKey();
    }

    private static final class MapComparator implements Comparator<String> {
        Map<String, Integer> map;

        private MapComparator(Map<String, Integer> map) {
            this.map = map;
        }

        @Override
        public int compare(String a, String b) {
            if (map.get(a) >= map.get(b)) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
