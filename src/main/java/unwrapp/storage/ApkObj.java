package unwrapp.storage;

/**
 * Created by mitshubh on 6/11/17.
 */
public class ApkObj {
    public String apkName;
    public float apkScore;
    public int iter=0;

    public ApkObj(int iter, String apkName, float apkScore) {
        this.iter = iter;
        this.apkName = apkName;
        this.apkScore = apkScore;
    }
}
