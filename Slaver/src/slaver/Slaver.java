package slaver;

import partager.ResultMiniTask;
import partager.MiniDataTask;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Slaver implements Runnable {
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private ObjectInputStream socketIn;
    private ObjectOutputStream socketOut;
    private final Object taskLock = new Object();
    private MiniDataTask currentTask;
private static final int[] PORTS = {1095, 1096, 1097, 1099, 1100}; // Liste des ports à essayer

  public Slaver(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        startRegistry(); // Appel de la méthode pour démarrer le registre RMI
    }

    @Override
    public void run() {
        //RMI
      
        //RMI
        try {
            while (!Thread.currentThread().isInterrupted()) {
                MiniDataTask task;
                synchronized (taskLock) {
                    while (currentTask == null) {
                        taskLock.wait(); // Attendre une nouvelle tâche
                    }
                    task = currentTask;
                    currentTask = null; // Réinitialiser la tâche
                }
                processMiniTask(task);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // Log or handle interruption
        } finally {
            closeResources();
        }
    }

    public void receiveTask(MiniDataTask task) {
        synchronized (taskLock) {
            currentTask = task;
            taskLock.notify(); // Signaler qu'une nouvelle tâche est arrivée
        }
    }

    public void connect() throws IOException {
        this.socket = new Socket(serverAddress, serverPort);
        this.socketOut = new ObjectOutputStream(socket.getOutputStream());
        this.socketIn = new ObjectInputStream(socket.getInputStream());
        System.out.println("Slaver est connecté...");

        // Thread pour écouter les tâches entrantes
        new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                                       
                     MiniDataTask task = (MiniDataTask) socketIn.readObject();
                    receiveTask(task);
                }
                
            } catch (ClassNotFoundException |StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException ex) {
                Logger.getLogger(Slaver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    }

    private void closeResources() {
        try {
            if (socketOut != null) {
                socketOut.close();
            }
            if (socketIn != null) {
                socketIn.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processMiniTask(MiniDataTask miniTask) {
        float[] tabA = miniTask.getTabA();
        float[] tabB = miniTask.getTabB();
        char operation = miniTask.getOperation();
        float[] result;

        switch (operation) {
            case '+':
            case '-':
                result = new float[tabA.length];
                for (int i = 0; i < tabA.length; i++) {
                    if (operation == '+') {
                        result[i] = tabA[i] + tabB[i];
                    } else {
                        result[i] = tabA[i] - tabB[i];
                    }
                }
                break;
            case '*':
                // For multiplication (matrix multiplication)
                result = new float[1]; // Result is a single element
                result[0] = 0;
                for (int i = 0; i < tabA.length; i++) {
                    result[0] += tabA[i] * tabB[i];
                }
                break;
            default:
                System.out.println("Opération inconnue");
                return; // Early return for unknown operation
        }

        // Enregistrez le résultat dans MiniDataTask
        ResultMiniTask R = new ResultMiniTask(result, miniTask.getIdDataTask(), miniTask.getId());

        // Envoyez le résultat au client
        sendResultToWorker(R);
    }

    // Ajoutez la méthode d'envoi au client ici
    private void sendResultToWorker(ResultMiniTask ResultminiTask) {
        try {
            
               socketOut.reset();
              socketOut.writeObject(ResultminiTask);
              socketOut.flush();
            System.out.println("MiniTask :" + ResultminiTask.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
 public void startRegistry() {
    try {
        boolean registryStarted = false;
        for (int port : PORTS) {
            try {
                Registry registry = LocateRegistry.createRegistry(port);
                ImpSlaveService remoteServer = new ImpSlaveService();
                registry.bind("RemoteSalver", remoteServer);
                System.out.println("RMI registry started successfully on port " + port);
                registryStarted = true;
                break; // Sortir de la boucle une fois que le registre a été démarré avec succès
            } catch (Exception e) {
                // Affichez un message si le port est déjà utilisé
                System.out.println("Port " + port + " is already in use. Trying another port...");
            }
        }
        if (!registryStarted) {
            System.out.println("Could not start RMI registry. All ports from 1095 to 1100 are in use.");
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    // ... Existing methods ...
    public static void main(String[] args) {
        int portSlaver = 1235; // Assurez-vous que le port correspond à celui utilisé par le serveur
        Slaver slaver = new Slaver("127.0.0.1", portSlaver);

        try {
            slaver.connect(); // Établir la connexion
        } catch (IOException e) {
            e.printStackTrace();
            return; // Quitter en cas d'échec de la connexion
        }

        Thread thread = new Thread(slaver);
        thread.start();
    }
}
