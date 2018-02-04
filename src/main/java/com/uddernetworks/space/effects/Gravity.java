package com.uddernetworks.space.effects;

import com.uddernetworks.space.generator.MoonGenerator;
import com.uddernetworks.space.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class Gravity implements Listener {

    private Main main;

    public Gravity(Main main) {
        this.main = main;
    }

    public void updateEntities() {
        Bukkit.getServer().getWorlds().stream().filter(world -> world.getGenerator().getClass().equals(MoonGenerator.class)).forEach(world -> {
            world.getEntities().forEach(entity -> {
                // Set maximum velocity deteriorating after a given time frame
            });
        });
    }

}
