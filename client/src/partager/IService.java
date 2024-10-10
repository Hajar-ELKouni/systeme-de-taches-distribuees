package partager;

import java.io.File;
import java.io.IOException;
import java.rmi.Remote; 
import java.rmi.RemoteException;
import partager.Image;

public interface IService extends Remote {
    public byte[] processImage(byte[] imageData,int choice) throws RemoteException ;

}
