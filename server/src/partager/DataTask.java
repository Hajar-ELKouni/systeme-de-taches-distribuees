package partager;
import java.io.Serializable;



public class DataTask implements Serializable {
 private static int nextId = 0;
    int id;

    public DataTask() {
        this.id = getNextId();
    }
    public int getId() {
        return id;
    }
    private static synchronized int getNextId() {
        return nextId++;
    }
  
}