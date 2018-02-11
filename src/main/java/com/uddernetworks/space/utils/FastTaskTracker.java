package com.uddernetworks.space.utils;

import com.uddernetworks.space.main.Main;

import java.util.ArrayList;
import java.util.List;

public class FastTaskTracker {

    private Main main;
    private List<FastTask> fastTasks = new ArrayList<>();

    public FastTaskTracker(Main main) {
        this.main = main;
    }

    public void addFastTask(FastTask fastTask) {
        fastTasks.add(fastTask);
    }

    public void removeFastTask(FastTask fastTask) {
        if (fastTask.getTask() != null && !fastTask.getTask().isCancelled()) fastTask.getTask().cancel();
        fastTasks.remove(fastTask);
    }

    public void stopAll() {
        new ArrayList<>(fastTasks).forEach(this::removeFastTask);
    }

}
