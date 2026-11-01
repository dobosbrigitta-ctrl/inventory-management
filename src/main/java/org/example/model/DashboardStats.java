package org.example.model;

import java.util.Map;

public class DashboardStats {
    private long standardItemsCount;
    private long rescueItemsCount;
    private Map<String, Integer> criticalStockItems;

    public DashboardStats(long standardItemsCount, long rescueItemsCount, Map<String, Integer> criticalStockItems) {
        this.standardItemsCount = standardItemsCount;
        this.rescueItemsCount = rescueItemsCount;
        this.criticalStockItems = criticalStockItems;
    }

    public long getStandardItemsCount() {
        return standardItemsCount;
    }

    public long getRescueItemsCount() {
        return rescueItemsCount;
    }

    public Map<String, Integer> getCriticalStockItems() {
        return criticalStockItems;
    }
}
