package client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import partager.IService;
import partager.Matrice;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String serverAddress;
    private int serverPort;
    private volatile float[][] resultMatrix;
    private final Object resultLock = new Object();

    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void connect() throws IOException {
        this.socket = new Socket(serverAddress, serverPort);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

    public void matriceOperation(Scanner sc) throws IOException {
        System.out.println("Choose your operation (+, -, *):");
        char operation = sc.nextLine().charAt(0);
        System.out.println("Enter matrix size:");
        int size = sc.nextInt();
        sc.nextLine(); // Consume the newline

        float[][] matA = MatrixOperation.ReadMatrice("Enter the first matrix:", size, sc);
        float[][] matB = MatrixOperation.ReadMatrice("Enter the second matrix:", size, sc);
        Matrice data = new Matrice(matA, matB, operation);

        MatrixOperation.sendMatrice(this.socket, data, this.out);

        Thread receiverThread = new Thread(() -> {
            try {
                synchronized (resultLock) {
                    resultMatrix = MatrixOperation.recieveMatrice(this.socket, this.in);
                    resultLock.notify(); // Notify the main thread that the result is ready
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });

        receiverThread.start();

        synchronized (resultLock) {
            while (resultMatrix == null) {
                try {
                    resultLock.wait(); // Wait for the receiver thread to notify
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            System.out.println("The response is:\n");
            MatrixOperation.printMatrice(resultMatrix);
            this.resultMatrix = null;
        }
    }

    public void processImageWithRMI(Scanner sc) {
    try {
        System.out.println("Enter the path of the image to process:");
        String imagePath = sc.nextLine();
        System.out.println("Choose the filter to apply:");
        System.out.println("1.Noise ");
        System.out.println("2. Convolution");
        int choice = sc.nextInt();
        sc.nextLine(); // Consume the newline
        
        Registry registry = LocateRegistry.getRegistry("localhost", 1098);
        IService remoteServer = (IService) registry.lookup("RemoteServer");
        
        File imageFile = new File(imagePath);
        if (!imageFile.exists() || !imageFile.isFile()) {
            System.out.println("Invalid image path. Please provide a valid path to an image file.");
            return;
        }
        
        byte[] imageData = ImageOperation.uploadImage(imageFile);
        byte[] processedImageData=remoteServer.processImage(imageData,choice);
        
        System.out.println("Enter the name for the filtered image:");
        String filename = sc.nextLine();
        filename = filename+".jpg";
        if (filename.isEmpty()) {
            filename = "filtered_image.jpg"; // Default filename if not specified
        }
        
        ImageOperation.saveImage(processedImageData, filename);
        System.out.println("Filtered image saved successfully as: " + filename);
    } catch (Exception e) {
        e.printStackTrace();
    }
   }


    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 1234);
        try (Scanner sc = new Scanner(System.in)) {
            client.connect();
            while (true) {
                System.out.println("Choose operation:");
                System.out.println("1. Matrix Operation");
                System.out.println("2. Image Processing");
                System.out.println("Enter 'exit' to quit.");
                String decision = sc.nextLine();
                if (decision.equalsIgnoreCase("exit")) {
                    client.close();
                    break;
                } else if (decision.equals("1")) {
                    client.matriceOperation(sc);
                } else if (decision.equals("2")) {
                    client.processImageWithRMI(sc);
                } else {
                    System.out.println("Invalid choice. Please enter 1, 2, or 'exit'.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}