package dev.emortal.lidar;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BlockColours {

    private static final Map<Block, Color> colorMap = new HashMap<>();

    public static void init() throws IOException {
        // Downloads the mc resource pack and average all block textures to one colour
        // TODO: redo this eventually
//        for (@NotNull Block value : Block.values()) {
//            var file = new File("C:\\Users\\emortal\\AppData\\Roaming\\PrismLauncher\\instances\\1.21.1\\.minecraft\\resourcepacks\\mcmeta-451045f224aefbb4d2f91b1adf04f9a04df40820\\assets\\minecraft\\textures\\block\\" + value.namespace().path() + ".png");
//            if (file.exists()) {
//                Color averageColor = averageColor(ImageIO.read(file));
//                colorMap.put(value, averageColor);
//            }
//        }
    }

    private static Color averageColor(BufferedImage image) {
        int ignored = 0;
        float sumR = 0;
        float sumG = 0;
        float sumB = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color col = new Color(image.getRGB(x, y));

                if (col.getAlpha() == 0) {
                    ignored++;
                    continue;
                }

                sumR += col.getRed();
                sumG += col.getGreen();
                sumB += col.getBlue();
            }
        }
        int pixelCount = image.getWidth() * image.getHeight() - ignored;
        return new Color((int)(sumR / pixelCount), (int)(sumG / pixelCount), (int)(sumB / pixelCount));
    }

    public static @NotNull Color getColor(@NotNull Block block) {
        return colorMap.getOrDefault(block, Color.WHITE);
    }

}
