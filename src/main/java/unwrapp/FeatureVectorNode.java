package unwrapp;

import java.io.Serializable;

public class FeatureVectorNode implements Serializable{
    String className;
    String methodName;
    int windowOffset;

    FeatureVectorNode(String className, String methodName, int windowOffset){
        this.className = className;
        this.methodName = methodName;
        this.windowOffset = windowOffset;
    }

    public void print(){
        System.out.println("Printing object ......................");
        System.out.println(className+" "+methodName+" "+windowOffset);
    }


}
