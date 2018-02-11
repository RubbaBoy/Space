package com.uddernetworks.space.utils;

public class Debugger {

    private long startTime = -1;
    private long lastTime = -1;

    public void log(String message) {
        if (lastTime == -1) {
            startTime = System.currentTimeMillis();
            lastTime = System.currentTimeMillis();
        }

        System.out.println(message + " after " + (System.currentTimeMillis() - lastTime) + "ms");

        lastTime = System.currentTimeMillis();
    }

    public void end() {
        System.out.println("Ended with total time " + (System.currentTimeMillis() - startTime) + "ms");
    }

}
