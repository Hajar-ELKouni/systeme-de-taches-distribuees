package partager;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Random;

public class DataNoise implements Filter, Serializable {

   private final double intensity = 0.4; // Intensité élevée

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage noisyImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int black = Color.BLACK.getRGB();
        int white = Color.WHITE.getRGB();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double randomValue = Math.random();
                if (randomValue < intensity / 2) {
                    noisyImage.setRGB(x, y, black); // Salt noise
                } else if (randomValue > 1 - intensity / 2) {
                    noisyImage.setRGB(x, y, white); // Pepper noise
                } else {
                    noisyImage.setRGB(x, y, image.getRGB(x, y));
                }
            }
        }

        return noisyImage;
        
    }

}