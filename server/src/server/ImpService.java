package server;

import partager.IService;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import partager.Image;

public class ImpService extends UnicastRemoteObject implements IService {
  
    private byte[] result;
    private final Object lock = new Object();

    protected ImpService() throws RemoteException {
        super();
  
    }

    @Override
    public byte[] processImage(byte[] imageData,int choice) throws RemoteException {
        synchronized (lock) {
            try {
                result = Worker.processImage(imageData,choice);
                lock.notify(); // Notify waiting threads that result is ready
            } catch (IOException ex) {
                Logger.getLogger(ImpService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return getResult();
    }

    private byte[] getResult() {
        synchronized (lock) {
            // If result is null, wait until it's filled by another thread
            while (result == null) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Logger.getLogger(ImpService.class.getName()).log(Level.SEVERE, null, e);
                }
            }
            return result;
        }
    }
}
