package unwrapp;

import java.io.File;
import java.util.*;

/* Call execute() from web service */
/* Input : application name (without extension) */
/* Output : List of String(application name and similarity score separated by space)
 * For now all applications are being returned in the result in descending order of their similarity score
 */
public class WebServiceUtil {
    private static Map<String, BitSet> bitVectors = new HashMap<String, BitSet>();
    private static List<String> result = new ArrayList<>();

    public static List<String> execute(String apkName) {
        try {
            /* Feature Vector Generation */
            String dir = FeatureVectorGenerator.getCurrentDirectory();
            File folder = new File(dir + Constants.PACKAGE_PREFIX + Constants.BB_FOLDER);
            String filePath = dir + Constants.PACKAGE_PREFIX + Constants.BB_FOLDER;
            for (File opCodeFile : folder.listFiles()) {
                if (opCodeFile.toString().contains(Constants.MAC_STORE)) {
                    continue;
                }
                FeatureVectorGenerator.readOpCode(filePath + opCodeFile.getName());
                /* k = 5, m = 240007 */
                int k = 5;
                int m = 240007;
                Map<Long, List<FeatureVectorNode>> featureVector = FeatureVectorGenerator.processOpCode(k, m);
                if (featureVector == null) {
                    System.out.println("HALT! Error in processed opcode");
                    return result;
                }
                /* Generating bit vector */
                BitSet bitVec = FeatureVectorGenerator.generateBitVector(featureVector);
                bitVectors.put(opCodeFile.getName(), bitVec);
                /* Serializing complete feature vector object information (For future traceback if need be )*/
                FeatureVectorGenerator.serializeFeatureVector(featureVector, opCodeFile.getName());
                featureVector = null;
            }
            /* Serializing bit vectors of all read files (For future traceback/processing if need be ) */
            FeatureVectorGenerator.serializeBitVector(bitVectors);
            /*
            * Calculating similarity of input application with all existing application
            * and sorting by similarity score
            */
            sortedBySimilarity(apkName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void sortedBySimilarity(String apkName) throws Exception{
        Map<String, Double> similarityMap = new HashMap<String, Double>();
        apkName = apkName + ".out";
        BitSet bitVectorToCompare = bitVectors.get(apkName);

        /* Calculating similarity with each application in dataset */
        for(Map.Entry<String, BitSet> entry: bitVectors.entrySet()){
            if(entry.getKey().equals(apkName))
                continue;
            else{
                double distance = Agglomerate.euclideanDistance(bitVectorToCompare, entry.getValue());
                double similarity = 1.0 - distance;
                similarityMap.put(entry.getKey(), distance);
            }
        }

        /* Sorting */
        List<Map.Entry<String, Double>> simList = new LinkedList<>(similarityMap.entrySet());
        Collections.sort( simList, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare( Map.Entry<String, Double> o1, Map.Entry<String, Double> o2 ) {
                return (o2.getValue() ).compareTo(o1.getValue() );
            }
        });

        /* Forming list in required output manner */
        for(Map.Entry<String, Double> ele : simList){
            result.add(ele.getKey()+" "+ele.getValue());
        }
    }

    /* execute() method would be called from web service
    public static void main(String[] args) {
        System.out.println("Wait for it.........");
        System.out.println(execute("cc.dojo.amplopin-10"));
        System.out.println("And it's done!");

    }
    */

}
