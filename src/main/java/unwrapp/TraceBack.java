package unwrapp;

import java.io.*;
import java.util.*;

/*
 * This code is responsible for generating traceback of the applications if there is a need to explore the applications of more.
 * On evaluation, if two applications are found to be more similar than other applications, their traceback information stored in
 * feature vector can be traced using below functions
 */
public class TraceBack {
    private static String appendFileSuffix = "";
    private static String dir = "";

    public static void setDirectory(){
        dir = FeatureVectorGenerator.getCurrentDirectory();
    }

    public static void setFileSuffix(String apkName1, String apkName2){
        if(apkName1.compareTo(apkName2) < 0)
            appendFileSuffix = "_"+apkName1+"_"+apkName2;
        else
            appendFileSuffix = "_"+apkName2+"_"+apkName1;
    }

    public static void deleteFileIfExists(){
        File f = new File(dir+ Constants.PACKAGE_PREFIX  + Constants.TRACE_BACK_FILE + appendFileSuffix +Constants.TRACE_FILE_OUTPUT_FORMAT);
        if(f.exists()) {
            f.delete();
        }
    }

    public static void generateTraceBack(String apkName1, String apkName2){
        Map<String, BitSet> readInput = null;
        Map<Long, List<FeatureVectorNode>> apkInfo1 = null;
        Map<Long, List<FeatureVectorNode>> apkInfo2 = null;
        readInput = FeatureVectorGenerator.deserializeBitVector();
        if(readInput == null){
            System.out.println("ABORT ! Bit vector file is empty.");
            return;
        }
        BitSet featureBits1 = readInput.get(apkName1);
        BitSet featureBits2 = readInput.get(apkName2);
        if(featureBits1.isEmpty() || featureBits2.isEmpty()){
            System.out.println("ABORT ! file do not have generated bit vectors");
            return;
        }
        featureBits1.and(featureBits2);
        readInput = null;         // Eligible for garbage collection

        apkInfo1 = FeatureVectorGenerator.deserializeFeatureVector(apkName1);
        apkInfo2 = FeatureVectorGenerator.deserializeFeatureVector(apkName2);

        setDirectory();
        setFileSuffix(apkName1, apkName2);
        deleteFileIfExists();

        long[] indexes = featureBits1.stream().asLongStream().toArray();
        for(long index : indexes){
            List<FeatureVectorNode> fvNodeList = apkInfo1.get(index);
            writeTraceBackToFile(fvNodeList, apkName1, index);
            fvNodeList = apkInfo2.get(index);
            writeTraceBackToFile(fvNodeList, apkName2, index);
        }
    }


    public static void writeTraceBackToFile(List<FeatureVectorNode> fvNodeList, String apkName, long index){
        FileWriter fileWriter = null;
        BufferedWriter bis = null;
        try{
            fileWriter = new FileWriter(dir+ Constants.PACKAGE_PREFIX  + Constants.TRACE_BACK_FILE + appendFileSuffix +Constants.TRACE_FILE_OUTPUT_FORMAT, true);
            bis = new BufferedWriter(fileWriter);
            bis.write("=========== Index Match =========== "+index+"\n");
            bis.write("=========== Apk Name =========== "+apkName+"\n");
            bis.write("=========== Feature vectors list =========== "+"\n");
            for(FeatureVectorNode fvNode : fvNodeList){
                bis.write("Class Name: "+fvNode.className+"\t"+"Method Name: "+fvNode.methodName+"\t"+"Window Offset: "+fvNode.windowOffset);
                bis.write("\n\n");
            }
            bis.close();
        }
        catch(IOException ie){
            ie.printStackTrace();
        }
    }

    public static void main(String[] args){
        Scanner s = new Scanner(System.in);
        System.out.println("Enter fully qualified name of first application : Example :- abdallaessa.msgapp-1");
        String apkName1 = s.nextLine()+".out";
        System.out.println("Enter fully qualified name of second application : Example :- cd.ark.menu-3");
        String apkName2 = s.nextLine()+".out";

        System.out.println("Entered applications name are : "+apkName1+" "+apkName2);

        generateTraceBack(apkName1, apkName2);
        System.out.println("Traceback generated.");
    }
}
