package partager;

import java.io.Serializable;
import java.util.Arrays;

public class ResultMiniTask implements Serializable{
   
    private float[] Res ;
    private int idDataTask;
    private int idMiniDataTask;
  
    
     public ResultMiniTask(float[] Res, int idDataTask, int idMiniDataTask) {
     
        this.Res = Res;
        this.idDataTask = idDataTask;
        this.idMiniDataTask = idMiniDataTask;
    }

    



    public float[] getRes() {
        return Res;
    }

    public int getIdDataTask() {
        return idDataTask;
    }

    public int getIdMiniDataTask() {
        return idMiniDataTask;
    }

 

    public void setRes(float[] Res) {
        this.Res = Res;
    }

    public void setIdDataTask(int idDataTask) {
        this.idDataTask = idDataTask;
    }

    public void setIdMiniDataTask(int idMiniDataTask) {
        this.idMiniDataTask = idMiniDataTask;
    }

 
        @Override
    public String toString() {
        return "ResultMiniTask{" +
                ", Res=" + Arrays.toString(Res) +
                ", idDataTask=" + idDataTask +
                ", idMiniDataTask=" + idMiniDataTask +
                '}';
    }
    
}
