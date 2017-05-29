package code.ponfee.test;

import java.util.HashMap;

import code.ponfee.job.slb.AbstractLoadBalance;
import code.ponfee.job.slb.LeastActiveBalance;

public class TestLoadBalance {

    public static void main(String[] args) {
        HashMap<String, Integer> serverWeightMap = new HashMap<String, Integer>();
        serverWeightMap.put("192.168.1.100", 1);
        serverWeightMap.put("192.168.1.101", 1);
        // 权重为4
        serverWeightMap.put("192.168.1.102", 4);
        serverWeightMap.put("192.168.1.103", 1);
        serverWeightMap.put("192.168.1.104", 1);
        // 权重为3
        serverWeightMap.put("192.168.1.105", 3);
        serverWeightMap.put("192.168.1.106", 1);
        // 权重为2
        serverWeightMap.put("192.168.1.107", 2);
        serverWeightMap.put("192.168.1.108", 1);
        serverWeightMap.put("192.168.1.109", 1);
        serverWeightMap.put("192.168.1.110", 1);
        serverWeightMap.put("192.168.1.111", 5);
        
        
        AbstractLoadBalance slb = new LeastActiveBalance(serverWeightMap);
        System.out.println(slb.select());
    }
}
