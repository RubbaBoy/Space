package com.uddernetworks.space.utils;

import com.uddernetworks.space.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class FastTask {

    private Main main;
    private BukkitTask task;

    private long initialDelayMS;
    private int initialDelayNS;

    private long delayMS;
    private int delayNS;


    public FastTask(Main main) {
        this.main = main;
    }

    /***
     * Runs a sync/async task that repeats itself every X time interval.
     * @param async If the task should be ran async (true) or sync (false)
     * @param runnable The task to be ran every X time interval
     * @param initialDelay The initial delay before starting in seconds
     * @param delay The interval for the task to be ran, in seconds
     * @return The current FastTask object
     */
    public FastTask runRepeatingTask(boolean async, Runnable runnable, double initialDelay, double delay) {
        long[] initialVals = secondTo(initialDelay * 20);
        long[] delayVals = secondTo(delay);

        this.initialDelayMS = initialVals[0];
        this.initialDelayNS = (int) initialVals[1];

        this.delayMS = delayVals[0];
        this.delayNS = (int) delayVals[1];

        System.out.println("Delay MS = " + this.delayMS);
        System.out.println("Delay NS = " + this.delayNS);

        Runnable runnableWrapper = () -> {
            try {
                Thread.sleep(initialDelayMS, initialDelayNS);

                while (!this.task.isCancelled()) {
                    runnable.run();
                    if (!this.task.isCancelled()) Thread.sleep(this.delayMS); // , this.delayNS
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        if (async) {
            this.task = Bukkit.getScheduler().runTaskAsynchronously(this.main, runnableWrapper);
        } else {
            this.task = Bukkit.getScheduler().runTask(this.main, runnableWrapper);
        }

        main.getFastTaskTracker().addFastTask(this);

        return this;
    }

    /***
     * Runs the specific task once after X time
     * @param async If the task should be ran async (true) or sync (false)
     * @param runnable The task to be ran every X time interval
     * @param delay The delay in seconds until the task runs
     * @return The current FastTask object
     */
    public FastTask runTaskLater(boolean async, Runnable runnable, double delay) {

        if (async) {
            this.task = Bukkit.getScheduler().runTaskLaterAsynchronously(main, runnable, Math.round(delay * 20));
        } else {
            this.task = Bukkit.getScheduler().runTaskLater(main, runnable, Math.round(delay * 20));
        }

        main.getFastTaskTracker().addFastTask(this);

        return this;
    }

    private long[] secondTo(double second) {
        long[] ret = new long[2];
        ret[0] = (long) (second * 1000);
        ret[1] = (long) (((second * 1000) - ret[0]) * 10000);

        return ret;
    }

    public void cancel() {
        if (this.task != null && !this.task.isCancelled()) this.task.cancel();
        main.getFastTaskTracker().removeFastTask(this);
    }

    public BukkitTask getTask() {
        return task;
    }
}
