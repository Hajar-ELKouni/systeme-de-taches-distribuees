package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;


import partager.Matrice;

public class MatrixOperation {
    
    public static float[][] ReadMatrice(String text, int size, Scanner sc) {
        System.out.println(text);
        float[][] a = new float[size][size];
        int i, j;
        for (i = 0; i < size; ++i) {
            for (j = 0; j < size; ++j) {
                System.out.println("  M[" + i + "][" + j + "] =  ");
                a[i][j] = Float.parseFloat(sc.next());
            }
        }
        return a;
    }

    public static void sendMatrice(Socket socket, Matrice data, ObjectOutputStream out) throws IOException {
        System.out.println("objet datask envoyee "+data.toString());
        out.reset();
        out.writeObject(data);
        out.flush();
    }

    public static float[][] recieveMatrice(Socket socket, ObjectInputStream in) throws IOException, ClassNotFoundException {
        Matrice responseData = (Matrice) in.readObject();
        System.out.println("objet datask recived "+responseData.toString());
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
