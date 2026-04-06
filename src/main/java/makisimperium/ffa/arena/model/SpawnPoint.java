package makisimperium.ffa.arena.model;

import cn.nukkit.level.Level;
import cn.nukkit.level.Location;

public final class SpawnPoint {

    private double x;
    private double y;
    private double z;
    private double yaw;
    private double pitch;

    public SpawnPoint() {
    }

    public SpawnPoint(double x, double y, double z, double yaw, double pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static SpawnPoint fromLocation(Location location) {
        return new SpawnPoint(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public Location toLocation(Level level) {
        return new Location(x, y, z, yaw, pitch, level);
    }
}


