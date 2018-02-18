package com.uddernetworks.space.utils;

public class MutableInt {
    private int value;
    private int maxValue;

    public MutableInt() {}

    public MutableInt(int baseValue) {
        this.value = baseValue;
    }

    /***
     * Sets at what value the counter resets at 0.
     * The internal value may be this value, but will not exceed this value.
     * @param maxValue  The maximum value's amount
     * @return The current MutableInt object
     */
    public MutableInt setLoopbackCap(int maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public int getValue() {
        return this.value;
    }

    public void increment() {
        this.value++;
        if (this.value > this.maxValue) this.value = 0;
    }

    public void decrement() {
        this.value--;
    }
}
