package slaver;

import partager.Filter;
import partager.ISlaveService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class ImpSlaveService extends UnicastRemoteObject implements ISlaveService {

    public ImpSlaveService() throws RemoteException {}

    @Override
    public byte[] appliquerFilter(byte[] imageData, byte[] filterClassBytes,int choice) throws RemoteException {
    try {
    
        // Instancier le filtre
       Filter filterInstance = deserializeAndLoadFilterClass(filterClassBytes,choice);

        // Convertir les données d'image en BufferedImage
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
        BufferedImage originalImage = ImageIO.read(inputStream);

        // Appliquer le filtre sur l'image
        BufferedImage filteredImage = filterInstance.apply(originalImage);

        // Convertir l'image filtrée en tableau de bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(filteredImage, "jpg", outputStream);
        byte[] filteredImageData = outputStream.toByteArray();
        outputStream.close();

        System.out.println("filter applied successfully.");
        return filteredImageData;
    } catch (IOException e) {
        e.printStackTrace();
        throw new RemoteException("IO error while applying  filter.", e);
    }  
     
    }

    
    public Filter deserializeAndLoadFilterClass(byte[] serializedClassBytes,int choice) {
    try {
        // Spécifier le chemin du répertoire où vous souhaitez sauvegarder le fichier désérialisé
        File targetDirectory = new File("D:\\MQL\\Poo\\Socket\\RMIVersion\\lasteOne14\\Slaver\\src\\partager\\");
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs(); // Créer le répertoire s'il n'existe pas
        }
        String f = null;
        if (choice == 1) {
    f =  "DataNoise.class";
       } else if (choice == 2) {
    f =  "ConvolutionFilter.class";
       }
        // Créer le fichier temporaire dans le répertoire spécifié
       final File  tempFile= new File(targetDirectory, f);


        // Écrire les bytes sérialisés dans le fichier temporaire
        // Écrire les bytes sérialisés dans le fichier temporaire
try (FileOutputStream fos = new FileOutputStream(tempFile)) {
    fos.write(serializedClassBytes);
} catch (IOException e) {
    // Handle exception
}

        // Charger la classe à partir du fichier temporaire
        ClassLoader classLoader = new ClassLoader() {
            @Override
            public Class<?> findClass(String name) throws ClassNotFoundException {
                try {
                    byte[] classData = Files.readAllBytes(tempFile.toPath());
                    return defineClass(name, classData, 0, classData.length);
                } catch (IOException e) {
                    throw new ClassNotFoundException("Cannot load class " + name, e);
                }
            }
        };
        String NameC = null;
        if (choice == 1) {
    NameC =  "partager.DataNoise";
       } else if (choice == 2) {
    
    NameC =  "partager.ConvolutionFilter";
       }
        // Charger la classe à partir du nouveau class loader
        Class<?> filterClass = Class.forName(NameC, true, classLoader);

        // Instancier et retourner la classe chargée
        return (Filter) filterClass.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}


}
