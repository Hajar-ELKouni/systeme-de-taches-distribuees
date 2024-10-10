package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private ServerSocket serverSocketClient;
    private ServerSocket serverSocketSlave;
    private List<Worker> clientWorkers;
    public static Map<SlaveSocket, Boolean> slaveSockets;

    public Server(int portClient, int portSlave) throws IOException {
        serverSocketClient = new ServerSocket(portClient);
        serverSocketSlave = new ServerSocket(portSlave);
        clientWorkers = Collections.synchronizedList(new LinkedList<>());
        slaveSockets = new ConcurrentHashMap<>();
    }

    public void start() {
        System.out.println("Server is running...");
        
        
        //RMI
        try {
            Registry registry = LocateRegistry.createRegistry(1098);
            ImpService remoteServer = new ImpService();
            registry.bind("RemoteServer", remoteServer);
          
        } catch (Exception e) {
            e.printStackTrace();
        }
        //RMI

        // Thread handling client connections
        Thread clientHandlerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket client = serverSocketClient.accept();
                    System.out.println("New client connected: " + client.getInetAddress().getHostAddress());
                    
                    Worker clientWorker = new Worker(client);
                    Thread clientThread = new Thread(clientWorker);
                    clientThread.start();
                    clientWorkers.add(clientWorker);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Thread handling slave connections
        Thread slaveHandlerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket slave = serverSocketSlave.accept();
                    System.out.println("New Slave connected: " + slave.getInetAddress().getHostAddress());
                     ObjectOutputStream out = new ObjectOutputStream(slave.getOutputStream());
                     ObjectInputStream in = new ObjectInputStream(slave.getInputStream());
                     SlaveSocket s = new SlaveSocket(slave,out,in);
                    synchronized (slaveSockets) {
                        slaveSockets.put(s, true);
                        slaveSockets.notifyAll(); // Notify any waiting process about the new slave
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        clientHandlerThread.start();
        slaveHandlerThread.start();
    }
     public void removeClient(Worker clientWorker) {
        clientWorkers.remove(clientWorker);
        System.out.println("Client disconnected: " + clientWorker.getClientSocket().getInetAddress().getHostAddress());
    }

    public void removeSlave(SlaveSocket slaveSocket) {
        slaveSockets.remove(slaveSocket);
        System.out.println("Slave disconnected: " +slaveSocket.getSocket().getInetAddress().getHostAddress());
    }

    public static void main(String[] args) throws IOException {
        int portClient = 1234;
        int portSlave = 1235;
        Server server = new Server(portClient, portSlave);
        server.start();
    }
}
