package dev.emortal.lidar;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;

public class NoTickingEntity extends Entity {
    public NoTickingEntity(EntityType entityType) {
        super(entityType);
        hasPhysics = false;
        setNoGravity(true);
    }

    @Override
    public void tick(long time) {

    }
}
