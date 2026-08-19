package com.mooclient.waypoint;

import java.util.UUID;

/**
 * Represents a saved waypoint / navigation marker in the world.
 */
public class Waypoint {

    private String id;
    private String name;
    private double x;
    private double y;
    private double z;
    private String dimension;       // "minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"
    private String serverOrWorld;   // Server IP or singleplayer world name
    private int color;              // RGB (e.g. 0xFF5555)
    private boolean visible;
    private boolean beacon;

    public Waypoint(String name, double x, double y, double z, String dimension, String serverOrWorld, int color, boolean beacon) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension != null ? dimension : "minecraft:overworld";
        this.serverOrWorld = serverOrWorld != null ? serverOrWorld : "global";
        this.color = color;
        this.visible = true;
        this.beacon = beacon;
    }

    public Waypoint(String id, String name, double x, double y, double z, String dimension, String serverOrWorld, int color, boolean visible, boolean beacon) {
        this.id = id != null ? id : UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension != null ? dimension : "minecraft:overworld";
        this.serverOrWorld = serverOrWorld != null ? serverOrWorld : "global";
        this.color = color;
        this.visible = visible;
        this.beacon = beacon;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getServerOrWorld() {
        return serverOrWorld;
    }

    public void setServerOrWorld(String serverOrWorld) {
        this.serverOrWorld = serverOrWorld;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isBeacon() {
        return beacon;
    }

    public void setBeacon(boolean beacon) {
        this.beacon = beacon;
    }

    public String getFormattedCoords() {
        return String.format("X: %.0f  Y: %.0f  Z: %.0f", x, y, z);
    }

    public String getDimensionDisplayName() {
        if (dimension == null) return "Overworld";
        if (dimension.contains("nether")) return "Nether";
        if (dimension.contains("end")) return "The End";
        return "Overworld";
    }
}
