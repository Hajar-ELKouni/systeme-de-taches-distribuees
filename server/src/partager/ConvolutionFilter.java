package partager;
import partager.Filter;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.Serializable;

public class ConvolutionFilter implements Filter, Serializable {

    @Override
    public BufferedImage apply(BufferedImage image) {
        // Convertir l'image en niveaux de gris
        BufferedImage grayscaleImage = convertToGrayscale(image);

        // Ajouter un remplissage blanc autour de l'image en niveaux de gris
        int paddingSize = 1; // 10 pixels de remplissage blanc
//        BufferedImage filledImage = addPadding(grayscaleImage, paddingSize);
        BufferedImage filledImage = grayscaleImage;
        // Définir le noyau de la matrice pour le filtre de convolution (par exemple, flou gaussien)
        float[] matrix = {
            0.2f, 0.2f, 0.2f,
            0.2f, 0.2f, 0.2f,
            0.2f, 0.2f, 0.2f
        };
        Kernel kernel = new Kernel(3, 3, matrix);

        // Appliquer le filtre de convolution à l'image en niveaux de gris avec remplissage
        ConvolveOp convolution = new ConvolveOp(kernel, ConvolveOp.EDGE_ZERO_FILL, null);
        BufferedImage filteredImage = convolution.filter(filledImage, null);

        // Supprimer le remplissage après l'application du filtre
        BufferedImage resultImage = removePadding(filteredImage, paddingSize);

        return resultImage;
    }

    // Méthode pour convertir l'image en niveaux de gris
    private BufferedImage convertToGrayscale(BufferedImage image) {
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return grayImage;
    }

    // Méthode pour ajouter un remplissage autour de l'image
    private BufferedImage addPadding(BufferedImage image, int paddingSize) {
        int newWidth = image.getWidth() + 2 * paddingSize;
        int newHeight = image.getHeight() + 2 * paddingSize;
        BufferedImage paddedImage = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g2d = paddedImage.createGraphics();
        g2d.setColor(Color.WHITE); // Remplissage blanc
        g2d.fillRect(0, 0, newWidth, newHeight);
        g2d.drawImage(image, paddingSize, paddingSize, null);
        g2d.dispose();
        return paddedImage;
    }

    // Méthode pour supprimer le remplissage autour de l'image
    // Méthode pour supprimer le remplissage autour de l'image
// Méthode pour supprimer le remplissage autour de l'image
private BufferedImage removePadding(BufferedImage image, int paddingSize) {
    int newWidth = image.getWidth() - 2 * paddingSize;
    int newHeight = image.getHeight() - 2 * paddingSize;
    BufferedImage resultImage = new BufferedImage(newWidth, newHeight, image.getType());
    Graphics2D g2d = resultImage.createGraphics();
    // Dessiner l'image sans le padding
    g2d.drawImage(image, 0, 0, newWidth, newHeight, paddingSize, paddingSize, image.getWidth() - paddingSize, image.getHeight() - paddingSize, null);
    g2d.dispose();
    return resultImage;
}


}