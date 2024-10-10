package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
//import java.util.Scanner;

//import partager.DataTask;
import partager.Matrice;

public class MatrixOperation {
    
   

    public static void sendMatrice( Matrice matrice, ObjectOutputStream out) throws IOException {
        out.writeObject(matrice);
        out.flush();
    }

    public static float[][] recieveMatrice(Socket socket, ObjectInputStream in) throws IOException, ClassNotFoundException {
        Matrice responseData = (Matrice) in.readObject();
        return responseData.getRes();
    }

    public static void printMatrice(float[][] matrice) {
        String matAs = "\n";
        matAs += "\n";
        for (int i = 0; i < matrice.length; ++i) {
            matAs += "[";
            for (int j = 0; j < matrice.length; ++j) {
                matAs += matrice[i][j];
                matAs += " ";
            }
            matAs += " ]\n";
        }
        System.out.println(matAs);
    }
    
}
