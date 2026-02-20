package com.dgphoenix.casino.system;

import com.google.common.collect.Lists;

import java.util.*;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 03.06.16
 */
public class ItemDistributor {

    private final List<Integer> servers;
    private final List<Long> items;

    public ItemDistributor(List<Integer> servers, List<Long> items) {
        List<Integer> serversCopy = new ArrayList<>(servers);
        Collections.sort(serversCopy);
        this.servers = serversCopy;

        List<Long> itemsCopy = new ArrayList<>(items);
        Collections.sort(itemsCopy);
        this.items = itemsCopy;
    }

    public Map<Integer, List<Long>> distributeEvenly() {
        int itemsCount = items.size();
        int serversCount = servers.size();
        Map<Integer, List<Long>> itemsByGS = new HashMap<>();
        if (itemsCount > serversCount) {
            int itemsPerServer = itemsCount / serversCount;
            int itemsRemainder = itemsCount % serversCount;
            List<List<Long>> itemsPartitions = Lists.partition(items, itemsPerServer);
            List<Long> remainderPartition = new ArrayList<>();
            for (int i = serversCount; i < itemsPartitions.size(); i++) {
                remainderPartition.addAll(itemsPartitions.get(i));
            }
            for (int serverIndex = 0; serverIndex < serversCount; serverIndex++) {
                List<Long> serverBanks = new ArrayList<>();
                if (serverIndex < itemsPartitions.size()) {
                    serverBanks.addAll(itemsPartitions.get(serverIndex));
                }
                if (serverIndex < itemsRemainder) {
                    serverBanks.add(remainderPartition.get(serverIndex));
                }
                itemsByGS.put(servers.get(serverIndex), serverBanks);
            }
        } else {
            for (int serverIndex = 0; serverIndex < serversCount; serverIndex++) {
                List<Long> itemForServer = new ArrayList<>();
                if (serverIndex < itemsCount) {
                    itemForServer.add(items.get(serverIndex));
                }
                itemsByGS.put(servers.get(serverIndex), itemForServer);
            }
        }
        return itemsByGS;
    }
}
