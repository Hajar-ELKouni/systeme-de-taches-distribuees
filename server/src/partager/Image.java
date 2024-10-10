
package partager;

//import partager.DataTask;

public class Image extends DataTask{
    byte[] f;
    int id ;
    int hegth ;
    int width ;
    float[] arrayKirnel ;

    public Image(float[][] matA, float[][] matB, char operation) {
        super();
    }

    public Image(byte[] f, int id, int hegth, int width, float[] arrayKirnel, float[][] matA, float[][] matB, char operation) {
        super();
        this.f = f;
        this.id = id;
        this.hegth = hegth;
        this.width = width;
        this.arrayKirnel = arrayKirnel;
    }

    public byte[] getF() {
        return f;
    }

    public int getId() {
        return id;
    }

    public int getHegth() {
        return hegth;
    }

    public int getWidth() {
        return width;
    }

    public float[] getArrayKirnel() {
        return arrayKirnel;
    }

    public void setF(byte[] f) {
        this.f = f;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setHegth(int hegth) {
        this.hegth = hegth;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setArrayKirnel(float[] arrayKirnel) {
        this.arrayKirnel = arrayKirnel;
    }
    
}
