package partager;
import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISlaveService extends Remote{
    public byte[] appliquerFilter (byte[] imageData, byte[] file,int choice) throws RemoteException ;
    
}
