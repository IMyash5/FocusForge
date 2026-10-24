
package com.focusforge.focusforge.dto;

public class TaskStatisticsResponse {

    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private double completionPercentage;

    public TaskStatisticsResponse(
            long totalTasks,
            long completedTasks,
            long pendingTasks,
            double completionPercentage) {

        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
        this.completionPercentage = completionPercentage;
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public long getPendingTasks() {
        return pendingTasks;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }
}
