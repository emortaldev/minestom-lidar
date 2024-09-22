package dev.emortal.lidar.util;

import dev.emortal.lidar.BlockColours;
import dev.emortal.lidar.NoTickingEntity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.ServerFlag;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.Shape;
import net.minestom.server.collision.ShapeImpl;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class ScannerEntity extends Entity {

    protected final @Nullable Player shooter;
    private int ticks = 0;

    public ScannerEntity(@Nullable Player shooter, @NotNull EntityType entityType) {
        super(entityType);

        setBoundingBox(0.01, 0.01, 0.01);

        this.shooter = shooter;
        super.hasPhysics = false;
    }

    @Override
    public void tick(long time) {
        ticks++;
        if (ticks > 10) {
            remove();
            return;
        }

        Pos posBefore = super.getPosition();
        super.tick(time);
        Pos posNow = super.getPosition();

        Vec diff = Vec.fromPoint(posNow.sub(posBefore));
        PhysicsResult result = CollisionUtils.handlePhysics(
                super.instance, super.getChunk(),
                super.getBoundingBox(),
                posBefore, diff,
                null, true
        );

        // Causes lag for some reason
        // TODO: Scan red pixels for entities, like the gmod addon
//        Collection<EntityCollisionResult> entityResult = CollisionUtils.checkEntityCollisions(instance, getBoundingBox(), posBefore, diff, 1, ent -> ent instanceof Player && ent != this.shooter, result);
//        for (EntityCollisionResult entityCollisionResult : entityResult) {
//
//            spawnDot(Pos.fromPoint(entityCollisionResult.collisionPoint()), Color.RED);
//            remove();
//            return;
//        }

        if (result.hasCollision()) {
            Shape[] shapes = result.collisionShapes();
            Point[] points = result.collisionPoints();

            Point hitPoint = null;
            if (shapes[0] instanceof ShapeImpl block) {
                hitPoint = points[0];
            }
            if (shapes[1] instanceof ShapeImpl block) {
                hitPoint = points[1];
            }
            if (shapes[2] instanceof ShapeImpl block) {
                hitPoint = points[2];
            }

            if (hitPoint == null) return;

            Block likelyBlock = instance.getBlock(hitPoint.add(velocity.normalize().mul(0.3)), Block.Getter.Condition.TYPE);

            spawnDot(Pos.fromPoint(hitPoint), BlockColours.getColor(likelyBlock));
            this.remove();
        }
    }

    private void spawnDot(Pos pos, Color color) {
        Entity dot = new NoTickingEntity(EntityType.TEXT_DISPLAY);
        dot.editEntityMeta(TextDisplayMeta.class, meta -> {
            meta.setText(Component.text("█", TextColor.color(color.getRed(), color.getGreen(), color.getBlue())));
            meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
            meta.setScale(new Vec(0.4));
//            meta.setSeeThrough(true); Causes big lag due to transparency
            meta.setWidth(1);
            meta.setHeight(1);
            meta.setBackgroundColor(0); // TODO: try with background square to see if more performant
        });

        // Delete the dot after one minute
        instance.scheduler().buildTask(() -> {
            dot.remove();
        }).delay(TaskSchedule.tick(ServerFlag.SERVER_TICKS_PER_SECOND * 60)).schedule();

        dot.setInstance(instance, pos);
    }
}