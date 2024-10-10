/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package partager;
import java.util.LinkedList;



/**
 *
 * @author DELL
 */
public class Matrice extends DataTask {

    private float[][] matA;
    private float[][] matB;
    private float[][] Res = {{0, 0}, {0, 0}};
    private char operation;
    private final LinkedList<MiniDataTask> miniTaskQueue;

    public Matrice(float[][] matA, float[][] matB, char operation) {
        super();
        this.matA = matA;
        this.matB = matB;
        this.operation = operation;
        this.miniTaskQueue = new LinkedList<MiniDataTask>();
    }

    public float[][] getMatA() {
        return matA;
    }

    public float[][] getMatB() {
        return matB;
    }

    public float[][] getRes() {
        return Res;
    }

    public char getOperation() {
        return operation;
    }

    public LinkedList<MiniDataTask> getMiniTaskQueue() {
        return miniTaskQueue;
    }

    public void setMatA(float[][] matA) {
        this.matA = matA;
    }

    public void setMatB(float[][] matB) {
        this.matB = matB;
    }

    public void setRes(float[][] Res) {
        this.Res = Res;
    }

    public void setOperation(char operation) {
        this.operation = operation;
    }
   
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("DataTask{id=" + getId() +
                ", matA=\n");
        result.append(matrixToString(matA));
        result.append(", matB=\n");
        result.append(matrixToString(matB));
        result.append(", Res=\n");
        result.append(matrixToString(Res));
        result.append(", operation=").append(operation).
                append(", miniTaskQueue=").append(miniTaskQueue).
                append('}');
        return result.toString();
    }

    
    private String matrixToString(float[][] matrix) {
        StringBuilder result = new StringBuilder("[");
        for (float[] row : matrix) {
            result.append("[");
            for (float value : row) {
                result.append(value).append(" ");
            }
            result.append("]\n");
        }
        result.append("]\n");
        return result.toString();
    }
    
    
}
