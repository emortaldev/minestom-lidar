package dev.emortal.lidar;

import dev.emortal.lidar.util.ScannerEntity;
import net.hollowcube.polar.PolarLoader;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) throws IOException {
        var server = MinecraftServer.init();

//        BlockColours.init();

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instance = instanceManager.createInstanceContainer();

        InputStream lobbyFile = Main.class.getClassLoader().getResourceAsStream("emclobby.polar");
        PolarLoader polarLoader = new PolarLoader(lobbyFile);
        instance.setChunkLoader(polarLoader);
        instance.enableAutoChunkLoad(false);

        // Load some chunks!!!!!
        var chunkFutures = polarLoader.world().chunks().stream().map(chunk -> instance.loadChunk(chunk.x(), chunk.z())).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(chunkFutures).thenRun(() -> {
            try {
                lobbyFile.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        MojangAuth.init();

        var eventNode = MinecraftServer.getGlobalEventHandler();
        eventNode.addListener(AsyncPlayerConfigurationEvent.class, e -> {
            e.setSpawningInstance(instance);
            e.getPlayer().setRespawnPoint(new Pos(0, 70, 0));
        });

        ItemStack gunItem = ItemStack.builder(Material.BLAZE_ROD)
                .customName(Component.text("Scanner", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                .build();

        eventNode.addListener(PlayerUseItemEvent.class, e -> {
            e.getPlayer().scheduler().submitTask(new Supplier<>() {
                int angle = -30;

                @Override
                public TaskSchedule get() {
                    if (angle > 30) {
                        return TaskSchedule.stop();
                    }

                    e.getPlayer().playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_BIT, Sound.Source.MASTER, 0.3f, 1.5f));

                    Vec lookDir = e.getPlayer().getPosition().direction();
                    for (int i = -50; i < 50; i += 1) {
                        shoot(e.getPlayer(), lookDir
                                .rotateAroundY(Math.toRadians(i))
                                .rotateAroundAxis(lookDir.rotateAroundY(Math.toRadians(90)), Math.toRadians(angle)));
                        shoot(e.getPlayer(), lookDir
                                .rotateAroundY(Math.toRadians(i))
                                .rotateAroundAxis(lookDir.rotateAroundY(Math.toRadians(90)), Math.toRadians(angle + 1)));
                    }

                    angle += 2;

                    return TaskSchedule.tick(1);
                }
            });
        });

        eventNode.addListener(PlayerSpawnEvent.class, e -> {
            e.getPlayer().setGameMode(GameMode.ADVENTURE);

            Entity blackHead = new Entity(EntityType.ITEM_DISPLAY);
            blackHead.editEntityMeta(ItemDisplayMeta.class, meta -> {
                meta.setItemStack(ItemStack.builder(Material.DIAMOND).customModelData(100).build());
                meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
                meta.setBrightnessOverride(0);
                meta.setScale(new Vec(-500));
            });
            blackHead.setInstance(instance, e.getPlayer().getPosition()).thenRun(() -> {
                e.getPlayer().addPassenger(blackHead);
            });

            e.getPlayer().getInventory().setItemInMainHand(gunItem);
        });

        server.start("0.0.0.0", 25565);
    }

    private static void shoot(Player shooter, Vec direction) {
        Entity scannerEntity = new ScannerEntity(shooter, EntityType.SNOWBALL);
        scannerEntity.setAutoViewable(false);
        scannerEntity.setNoGravity(true);
        scannerEntity.setVelocity(direction.mul(200).add(Vec.EPSILON));

        scannerEntity.setInstance(shooter.getInstance(), shooter.getPosition().add(0, shooter.getEyeHeight(), 0));
    }

}