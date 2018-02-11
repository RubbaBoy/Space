package com.uddernetworks.space.guis;

import com.uddernetworks.space.main.Main;

import java.util.ArrayList;
import java.util.List;

public class ProgressBarManager {

    private Main main;
    private List<ProgressBar> progressBars = new ArrayList<>();

    public ProgressBarManager(Main main) {
        this.main = main;
    }

    public void addProgressBar(ProgressBar progressBar) {
        if (!progressBars.contains(progressBar)) progressBars.add(progressBar);
    }

    public ProgressBar getProgressBar(String name) {
        for (ProgressBar progressBar : progressBars) {
            if (progressBar.getName().equals(name)) return progressBar;
        }

        return null;
    }

}
