package partager;

import java.io.Serializable;

/**
 *
 * @author DELL
 */
public class MiniDataTask implements Serializable{
    int id ;
    float[] tabA;
    float[] tabB;
  
    char operation;

    int idDataTask;

    public MiniDataTask(int id,float[] tabA, float[] tabB, char operation,int idDataTask) {
        this.id = id;
        this.tabA = tabA;
        this.tabB = tabB;
        this.operation = operation;
        this.idDataTask =idDataTask;
        
    }

    public void setId(int id) {
        this.id = id;
    }



    public void setIdDataTask(int idDataTask) {
        this.idDataTask = idDataTask;
    }

    public float[] getTabA() {
        return tabA;
    }

 

    public int getIdDataTask() {
        return idDataTask;
    }

    public float[] getTabB() {
        return tabB;
    }



    public char getOperation() {
        return operation;
    }

    public void setTabA(float[] tabA) {
        this.tabA = tabA;
    }

    public void setTabB(float[] tabB) {
        this.tabB = tabB;
    }



    public void setOperation(char operation) {
        this.operation = operation;
    }
    
    public int getId() {
        return id;
    }
}