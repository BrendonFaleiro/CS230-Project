package unwrapp;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Shubham Mittal on 6/6/17.
 */
public class Utils {

    static Map<String, Integer> globalPermMap;  // Map of permission mapping to the number of times they appeared over the course
    static Map<String, Integer> permIndexMap;   // Map of permission pointing to the index in the feature vector
    static List<int[]> globalFeatureMatrix;
    static String header = null;
    static int benignCount = 0;
    static int malwareCount = 0;

    static {
        globalFeatureMatrix = new ArrayList<>();
        String permissionPath = Constants.PACKAGE_PREFIX + Constants.PERMISSIONS_FILE + Constants.PERMISSION_OUT;
        File file = new File(permissionPath);
        /*if (file.exists()) {
            try {
                deSerialize(permissionPath);
            } catch (Exception e) {
                System.out.println("Unable to deserialize PermissionList");
                e.printStackTrace();
            }
        }
        else {*/
            globalPermMap = new ConcurrentHashMap<>();
            permIndexMap = new HashMap<>();

            try {
                BufferedReader br = new BufferedReader(new FileReader(Constants.PACKAGE_PREFIX + Constants.PERMISSIONS_FILE + Constants.CSV));
                String line;
                StringBuilder sb = new StringBuilder();
                int iter=0;
                while ((line = br.readLine())!=null) {
                    sb.append(line.trim());
                    sb.append(Constants.COMMA_DELIMITER);
                    globalPermMap.putIfAbsent(line.trim(), 0);
                    permIndexMap.put(line.trim(), iter++);
                }
                //sb.delete(sb.length()-2, sb.length());
                header = sb.toString();
                br.close();
            } catch (FileNotFoundException e) {
                System.out.println("No such file as " + Constants.PACKAGE_PREFIX + Constants.PERMISSIONS_FILE + Constants.CSV);
            } catch (IOException e) {
                System.out.println("Line could not be read!");
                e.printStackTrace();
            }
        //}

        //Drain the globalPermMap into a map to allow indexing
        //permIndexMap = new HashMap<>();

        //loadPermissionIndexMap(permIndexMap);
    }

    protected static void serialize(Object object, String outputPath) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(outputPath);
        ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
        oos.writeObject(object);
        oos.flush();
        oos.close();
    }

    protected static Map deSerialize(String inputPath) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(inputPath);
        ObjectInputStream ois = new ObjectInputStream(fileInputStream);
        globalPermMap = (Map<String, Integer>) ois.readObject();
        ois.close();
        return globalPermMap;
    }

    /*private static void loadPermissionIndexMap(Map<String, Integer> permIndexMap) {
        int iter=0;
        for (String key : globalPermMap.keySet()) {
            permIndexMap.put(key, iter++);
        }
    }*/

    protected static void covertCSV2Arff(String inputPath, String outputPath) throws IOException {
        CSVLoader csv = new CSVLoader();
        csv.setSource(new File(inputPath));
        Instances instances = csv.getDataSet();
        writeInstanceToFile(instances, outputPath);
    }

    protected static void writeInstanceToFile(Instances instances, String outputPath) throws IOException {
        ArffSaver arffSaver = new ArffSaver();
        arffSaver.setInstances(instances);
        arffSaver.setFile(new File(outputPath));
        arffSaver.writeBatch();
    }

    protected static void writeMap2CSV(Map<Attribute, Double> hashMap, String outputPath) throws IOException {
        FileWriter writer = new FileWriter(outputPath);
        StringBuilder sb = new StringBuilder();
        for (Attribute attribute: hashMap.keySet()) {
            sb.append(attribute.toString());
            sb.append(Constants.COMMA_DELIMITER);
            sb.append(hashMap.get(attribute));
            sb.append(Constants.NEWLINE_CHARACTER);
        }
        writer.append(sb.toString());
        writer.flush();
        writer.close();
    }

    protected static void writeHashMap2CSV(Map<String, Integer> hashMap, String outputPath) throws IOException {
        FileWriter writer = new FileWriter(outputPath);
        StringBuilder sb = new StringBuilder();
        for (String string: hashMap.keySet()) {
            sb.append(string.toString());
            sb.append(Constants.COMMA_DELIMITER);
            sb.append(hashMap.get(string));
            sb.append(Constants.NEWLINE_CHARACTER);
        }
        writer.append(sb.toString());
        writer.flush();
        writer.close();
    }
}