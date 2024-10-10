package server;

import partager.ConvolutionFilter;
import partager.ISlaveService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import partager.ResultMiniTask;
import partager.DataTask;
import partager.MiniDataTask;
import partager.Matrice;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import partager.IService;
import partager.Filter;

public class Worker implements Runnable {
    private final Socket clientSocket;
    private ObjectOutputStream socketOut;
    private ObjectInputStream socketIn;
private static final int[] PORTS = {1095, 1096, 1097, 1099, 1100}; // Liste des ports à essayer

    Worker() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
    public final LinkedList<DataTask> taskQueue;
    public final LinkedList<ResultMiniTask> ResultaMinitaskQueue;
    private final Object slaveAvailabilityLock = new Object();
    private int miniTaskCounter = 0; // Counter for mini task IDs


    public Worker(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.taskQueue = new LinkedList<>();
        this.ResultaMinitaskQueue = new LinkedList<>();
    }

    public void displayTaskQueue() {
        System.out.println("Current task queue:");
        for (DataTask task : taskQueue) {
            System.out.println(task);
        }
    }

    @Override
    public void run() {
        
      
        try {
            System.out.println("Worker is running...");
            this.socketOut = new ObjectOutputStream(this.clientSocket.getOutputStream());
            this.socketIn = new ObjectInputStream(this.clientSocket.getInputStream());

            while (!clientSocket.isClosed()) {
                try {
                    Matrice MatriceTask = (Matrice) this.socketIn.readObject();
                    taskQueue.add(MatriceTask);

                    // Afficher la file d'attente des tâches
                    displayTaskQueue();

                    splitTask(MatriceTask);
                    getSlavesForMiniTasks(MatriceTask);

                    listenForResultsFromSlaves();

                    // Attendre que tous les mini-tâches soient traitées
                    while (ResultaMinitaskQueue.size() <MatriceTask.getMiniTaskQueue().size()) {
                        synchronized (slaveAvailabilityLock) {
                            slaveAvailabilityLock.wait(5000); // Attente active - peut être améliorée
                        }
                    }

                    Matrice d = buildFinalResultFromResultTasks(MatriceTask);

                    sendTask(d);
                    d.toString();

                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (socketOut != null) {
                socketOut.close();
            }
            if (socketIn != null) {
                socketIn.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void splitTask(Matrice matriceTask) {
        float[][] matA = matriceTask.getMatA();
        float[][] matB = matriceTask.getMatB();
        char operation = matriceTask.getOperation();
        int sizeA = matA.length;
        int sizeB = matB.length;

        switch (operation) {
            case '+':
            case '-':
                // For addition and subtraction, split tasks by rows
                for (int i = 0; i < Math.max(sizeA, sizeB); i++) {
                    float[] rowA = (i < sizeA) ? getLigne(matA, i) : new float[0];
                    float[] rowB = (i < sizeB) ? getLigne(matB, i) : new float[0];
                    MiniDataTask miniTask = new MiniDataTask(i,rowA, rowB, operation, matriceTask.getId());
                    matriceTask.getMiniTaskQueue().add(miniTask);
                }
                break;
            case '*':
                // For multiplication, using different logic for matrix multiplication
                for (int i = 0; i < sizeA; i++) {
                    for (int j = 0; j < sizeB; j++) {
                        float[] rowA = getLigne(matA, i);
                        float[] columnB = getColumn(matB, j);
                        MiniDataTask miniTask = new MiniDataTask(miniTaskCounter++,rowA, columnB, operation, matriceTask.getId());
                        matriceTask.getMiniTaskQueue().add(miniTask);
                    }
                }
                
                break;
        }
        miniTaskCounter=0;
    }

    private float[] getLigne(float[][] matrix, int rowIndex) {
        if (rowIndex < 0 || rowIndex >= matrix.length) {
            throw new IllegalArgumentException("Indice de ligne invalide");
        }

        int cols = matrix[rowIndex].length;
        float[] ligne = new float[cols];
        System.arraycopy(matrix[rowIndex], 0, ligne, 0, cols);
        return ligne;
    }

    private float[] getColumn(float[][] matrix, int columnIndex) {
        int rows = matrix.length;
        float[] column = new float[rows];
        for (int i = 0; i < rows; i++) {
            column[i] = matrix[i][columnIndex];
        }
        return column;
    }

    public void sendMiniTask(MiniDataTask miniDataTask, SlaveSocket slave) throws IOException {
        ObjectOutputStream Out = slave.getOut();
        Out.writeObject(miniDataTask);
        Out.flush();
    }

    public void sendTask(DataTask dataTask) throws IOException {
        this.socketOut.flush();
        this.socketOut.writeObject(dataTask);
        this.socketOut.flush();
    }

    public void listenForResultsFromSlaves() {
        new Thread(() -> {
            while (true) {
                synchronized (Server.slaveSockets) {
                    for (Map.Entry<SlaveSocket, Boolean> entry : Server.slaveSockets.entrySet()) {
                        SlaveSocket slave = entry.getKey();
                        if (isResultAvailable(slave)) {
                            try {
                                receiveResultMiniData(slave);
                                entry.setValue(true); // Marquer le slave comme disponible
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                try {
                    synchronized (slaveAvailabilityLock) {
                        slaveAvailabilityLock.wait(1000); // Pause pour éviter l'utilisation excessive du CPU
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private boolean isResultAvailable(SlaveSocket slave) {
        try {
            return slave.getSocket().getInputStream().available() > 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void receiveResultMiniData(SlaveSocket slave) throws IOException, ClassNotFoundException {
        ObjectInputStream in = slave.getIn();
        ResultMiniTask resultMiniDataTask = (ResultMiniTask) in.readObject();
        System.out.println("Objet reçu : " + resultMiniDataTask);
        ResultaMinitaskQueue.add(resultMiniDataTask);
    }

    private void getSlavesForMiniTasks(Matrice dataTask) throws IOException {
        for (MiniDataTask miniTask : dataTask.getMiniTaskQueue()) {
            SlaveSocket slave = getAvailableSlave();
            while (slave == null) {
                try {
                    synchronized (slaveAvailabilityLock) {
                        slaveAvailabilityLock.wait(500); // Attendre un esclave disponible
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                slave = getAvailableSlave();
            }
            sendMiniTask(miniTask, slave);
        }
    }

    public Matrice buildFinalResultFromResultTasks(Matrice matriceTask) {
        int size = matriceTask.getMatA().length;
        float[][] matrix = new float[size][size];
        char operation = matriceTask.getOperation();

        if (operation == '*') {
            // Reset the matrix to zeros for multiplication
            for (int i = 0; i < size; i++) {
                Arrays.fill(matrix[i], 0);  // More efficient way to initialize rows to 0
            }
            // For multiplication, each mini task is a single element in the result matrix
            for (ResultMiniTask resultaMinitaskQueue : this.ResultaMinitaskQueue) {
                if (resultaMinitaskQueue.getIdDataTask() == matriceTask.getId()) {
                    int rowId = resultaMinitaskQueue.getIdMiniDataTask() / size;
                    int colId = resultaMinitaskQueue.getIdMiniDataTask() % size;
                    matrix[rowId][colId] += resultaMinitaskQueue.getRes()[0]; // Accumulate the result
                }
            }
        } else {
            // For addition or subtraction, each mini task is a row in the result matrix
            Iterator<ResultMiniTask> iterator = this.ResultaMinitaskQueue.iterator();
            while (iterator.hasNext()) {
                ResultMiniTask R = iterator.next();
                if (R.getIdDataTask() == matriceTask.getId()) {
                    int rowId = R.getIdMiniDataTask();
                    if (rowId < size) {
                        System.arraycopy(R.getRes(), 0, matrix[rowId], 0, R.getRes().length);
                    } else {
                        System.err.println("Row ID " + rowId + " is outside the matrix size.");
                    }
                    iterator.remove(); // Safe removal while iterating
                }
            }
        }

        // Clear the ResultaMinitaskQueue after building the final result
        this.ResultaMinitaskQueue.clear();

        matriceTask.setRes(matrix);
        return matriceTask;
    }

    public static SlaveSocket getAvailableSlave() {
        synchronized (Server.slaveSockets) {
            for (Map.Entry<SlaveSocket, Boolean> entry : Server.slaveSockets.entrySet()) {
                if (entry.getValue()) { // Vérifier si l'esclave est marqué comme disponible
                    entry.setValue(false); // Marquer l'esclave comme indisponible
                    synchronized (Server.slaveSockets) {
                        Server.slaveSockets.notifyAll(); // Notifier les threads en attente qu'un esclave est disponible
                    }
                    return entry.getKey();
                }
            }
        }
        return null; // Retourner null si aucun esclave disponible n'est trouvé
    }
    
public static byte[] processImage(byte[] imageData,int choice) throws IOException {
    if (imageData == null) {
        System.out.println("image Data n'arrive pas au worker");
        return null;
    } else {
        System.out.println("image Data arrivée au worker");
    }

    int n = 4;
    byte[] imageFilter = null;
    // Convertir les données d'image en BufferedImage
    ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
    BufferedImage image = ImageIO.read(inputStream);

    // Vérifier si l'image est null
    if (image == null) {
        throw new IllegalArgumentException("Image data is null or invalid");
    }

    // Diviser l'image en parties
    List<BufferedImage> imageParts = divideImage(image, n);

    // Convertir la classe ConvolutionFilter.class en tableau de bytes
    String filePath = null;
    if(choice==1){
        filePath = "D:\\MQL\\Poo\\Socket\\RMIVersion\\lasteOne14\\server\\src\\partager\\DataNoise.class";
    }else if(choice==2){
        filePath = "D:\\MQL\\Poo\\Socket\\RMIVersion\\lasteOne14\\server\\src\\partager\\ConvolutionFilter.class";
    }
    else{
        System.out.println("Votre choix invalide   !!!");
    }

    // Lire le contenu du fichier en bytes
    byte[] convolutionFilterBytes = readFileToBytes(filePath);

    // Map pour stocker les résultats filtrés pour chaque partie de l'image
    Map<Integer, byte[]> filteredImageParts = new HashMap<>();

    //RMI
    try {
        // Créer des threads pour envoyer chaque partie de l'image à un esclave disponible
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < imageParts.size(); i++) {
            final int partIndex = i;
            Thread thread = new Thread(() -> {
                try {
                    BufferedImage part = imageParts.get(partIndex);
                    // Convertir BufferedImage en tableau de bytes
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(part, "jpg", baos);
                    byte[] partData = baos.toByteArray();
                    baos.close();

                    // Envoyer la partie de l'image à un esclave disponible
                    Registry registry = LocateRegistry.getRegistry("localhost", PORTS[partIndex % PORTS.length]);
                    ISlaveService remoteSlave = (ISlaveService) registry.lookup("RemoteSalver");
                    // Appeler la méthode appliquerFilter du serveur avec le tableau de bytes de la classe ConvolutionFilter.class
                    byte[] result = remoteSlave.appliquerFilter(partData, convolutionFilterBytes,choice);
                    if (result != null) {
                        synchronized (filteredImageParts) {
                            filteredImageParts.put(partIndex, result);
                        }
                    }
                } catch (IOException | NotBoundException ex) {
                    Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            thread.start();
            threads.add(thread);
        }

        // Attendre la fin de tous les threads
        for (Thread thread : threads) {
            thread.join();
        }

        // Reconstruire l'image filtrée à partir des résultats
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        imageFilter = constructFilteredImage(filteredImageParts, imageWidth, imageHeight, n);

    } catch (InterruptedException e) {
        e.printStackTrace();
    }

    //RMI
    return imageFilter;
}
private static boolean allImagePartsProcessed(Map<Integer, byte[]> filteredImageParts, int totalParts) {
    // Vérifier si le nombre de parties d'image traitées est égal au nombre total de parties d'image
    return filteredImageParts.size() == totalParts;
}

//
public static byte[] constructFilteredImage(Map<Integer, byte[]> filteredImageParts, int imageWidth, int imageHeight, int divisions) {
        // Création d'une liste pour stocker les parties filtrées des images sous forme de BufferedImage
        List<BufferedImage> filteredImages = new ArrayList<>();

        int divisionHeight = imageHeight / divisions;
        int remainingHeight = imageHeight % divisions;
        int startY = 0;

        for (int i = 0; i < divisions; i++) {
            int currentHeight = divisionHeight + (i == divisions - 1 ? remainingHeight : 0);
            byte[] filteredPart = filteredImageParts.get(i);

            // Vérifier si la partie filtrée existe
            if (filteredPart == null) {
                // Si une partie filtrée est manquante, l'image filtrée est incomplète
                throw new IllegalStateException("Filtered image is incomplete");
            }

            // Convertir la partie filtrée en BufferedImage
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(filteredPart);
                BufferedImage filteredImage = ImageIO.read(inputStream);
                filteredImages.add(filteredImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            startY += currentHeight;
        }

        // Concaténer les parties filtrées pour former une seule image filtrée
        BufferedImage combinedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        int y = 0;
        for (BufferedImage filteredImage : filteredImages) {
            combinedImage.createGraphics().drawImage(filteredImage, 0, y, null);
            int currentHeight = filteredImage.getHeight(); // Hauteur de l'image courante
            y += currentHeight; // Utilisation de la hauteur correcte pour chaque partie d'image
        }

        // Convertir l'image combinée en tableau de bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(combinedImage, "jpg", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Retourner l'image filtrée en tant que tableau de bytes
        return baos.toByteArray();
    }


    /*public static byte[] constructFilteredImage(Map<Integer, byte[]> filteredImageParts, int imageWidth, int imageHeight, int divisions) {
        // Création d'une liste pour stocker les parties filtrées des images sous forme de BufferedImage
        List<BufferedImage> filteredImages = new ArrayList<>();

        int divisionHeight = imageHeight / divisions;
        int remainingHeight = imageHeight % divisions;
        int startY = 0;

        for (int i = 0; i < divisions; i++) {
            int currentHeight = divisionHeight + (i == divisions - 1 ? remainingHeight : 0);
            byte[] filteredPart = filteredImageParts.get(i);

            // Vérifier si la partie filtrée existe
            if (filteredPart == null) {
                // Si une partie filtrée est manquante, l'image filtrée est incomplète
                throw new IllegalStateException("Filtered image is incomplete");
            }

            // Convertir la partie filtrée en BufferedImage
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(filteredPart);
                BufferedImage filteredImage = ImageIO.read(inputStream);
                filteredImages.add(filteredImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            startY += currentHeight;
        }

        // Concaténer les parties filtrées pour former une seule image filtrée
        BufferedImage combinedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        int y = 0;
        for (BufferedImage filteredImage : filteredImages) {
            combinedImage.createGraphics().drawImage(filteredImage, 0, y, null);
            y += filteredImage.getHeight();
        }

        // Convertir l'image combinée en tableau de bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(combinedImage, "jpg", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
             try {
            saveImage(baos.toByteArray(), "as");
        } catch (IOException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Retourner l'image filtrée en tant que tableau de bytes
        return baos.toByteArray();
    }*/

    


public static byte[] serializeConvolutionFilter(int choice) throws IOException {
    // Chemin du fichier ConvolutionFilter.class
    String filePath= null;
    if(choice==1){
        filePath="D:\\MQL\\Poo\\Socket\\RMIVersion\\lasteOne14\\server\\src\\partager\\DataNoise.class";
    }else if(choice==2){
      filePath="D:\\MQL\\Poo\\Socket\\RMIVersion\\lasteOne14\\server\\src\\partager\\ConvolutionFilter.class";
    }
    // Lire le contenu du fichier en bytes
    return readFileToBytes(filePath);
}

public static List<BufferedImage> divideImage(BufferedImage image, int divisions) {
    List<BufferedImage> dividedImages = new ArrayList<>();

    int height = image.getHeight();
    int divisionHeight = height / divisions;
    int remainingHeight = height % divisions;
    int startY = 0;

    for (int i = 0; i < divisions; i++) {
        int currentHeight = divisionHeight + (i == divisions - 1 ? remainingHeight : 0);

        BufferedImage subImage = image.getSubimage(0, startY, image.getWidth(), currentHeight);
        dividedImages.add(subImage);

        startY += currentHeight;
    }

    return dividedImages;
}



  
  
    public static byte[] readFileToBytes(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
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
    
 
}