package makisimperium.ffa.arena.model;

import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;

public final class CuboidZone {

    private String worldName;
    private double minX;
    private double minY;
    private double minZ;
    private double maxX;
    private double maxY;
    private double maxZ;

    public CuboidZone() {
    }

    public CuboidZone(String worldName, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.worldName = worldName;
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public static CuboidZone fromCorners(String worldName, Vector3 first, Vector3 second) {
        return new CuboidZone(
                worldName,
                first.getX(),
                first.getY(),
                first.getZ(),
                second.getX(),
                second.getY(),
                second.getZ()
        );
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean contains(Location location) {
        if (location == null || location.getLevel() == null) {
            return false;
        }
        if (worldName == null || !worldName.equalsIgnoreCase(location.getLevel().getName())) {
            return false;
        }

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }
}


