//package com.uddernetworks.space.items;
//
//import org.bukkit.Material;
//import org.bukkit.event.player.PlayerDropItemEvent;
//import org.bukkit.event.player.PlayerInteractAtEntityEvent;
//import org.bukkit.event.player.PlayerInteractEvent;
//
//public class Wrench extends CustomItem {
//
//    public Wrench(Material material, short damage) {
//        super(material, damage, "Wrench");
//    }
//
//    @Override
//    public void onClick(PlayerInteractEvent event) {
//        System.out.println("Clicked with a wrench!");
//    }
//
//    @Override
//    public void onDrop(PlayerDropItemEvent event) {
//        System.out.println("Dropped a wrench!");
//    }
//
//    @Override
//    public void onClickEntity(PlayerInteractAtEntityEvent event) {
//        System.out.println("Clicked an entity with the wrench!");
//    }
//}
