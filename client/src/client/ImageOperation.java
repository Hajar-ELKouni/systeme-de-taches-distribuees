
package client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.imageio.ImageIO;

public class ImageOperation {
        public static byte[] uploadImage(File imageData) throws RemoteException, IOException {
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        
        try {
            fis = new FileInputStream(imageData);
            bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            
            return bos.toByteArray();
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
    }
    
    public static void saveImage(byte[] imageData, String filename) throws IOException {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
    BufferedImage image = ImageIO.read(inputStream);
    
    // Chemin du dossier où l'image modifiée sera sauvegardée
   String folderPath = "ImagesModifie\\";
    File folder = new File(folderPath);
    
    // Vérifier si le dossier existe, sinon le créer
    if (!folder.exists()) {
        folder.mkdirs(); // Créez tous les répertoires et sous-répertoires dans le chemin spécifié
    }
    
    // Chemin complet du fichier de sortie
    String outputPath = folderPath + filename;
    File outputFile = new File(outputPath);
    
    // Sauvegarder l'image modifiée dans le dossier spécifié
    ImageIO.write(image, "jpg", outputFile);
}
    
    
     // Méthode pour calculer la largeur (width) d'une image à partir de son tableau de bytes
    public static int getImageWidth(byte[] imageData) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
        BufferedImage image = ImageIO.read(inputStream);
        return image.getWidth();
    }

    // Méthode pour calculer la hauteur (height) d'une image à partir de son tableau de bytes
    public static int getImageHeight(byte[] imageData) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
        BufferedImage image = ImageIO.read(inputStream);
        return image.getHeight();
    }

}
