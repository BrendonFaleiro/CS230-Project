package unwrapp;
import java.io.File;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.lang.System;
import java.io.PrintStream;
import java.io.FileOutputStream;

public class Execute {

    public static void main(String[] args) {
        try {

            Scanner s = new Scanner(System.in);
            System.out.println("Enter k(k-Grams) : Example :- 5");
            int k = Integer.parseInt(s.nextLine());
            System.out.println("Enter m(Bit vector size) : Example :- 30000");
            int m = Integer.parseInt(s.nextLine());
            System.out.println("Enter number of clusters : Example :- 1");
            int numClusters = Integer.parseInt(s.nextLine());
            System.out.println("Enter Linkage type : Example :- c");
            String linkageInputType = s.nextLine();
            System.out.println("Entered values are : " + k + " " + m + " " + numClusters + " " + linkageInputType);

        /* Feature Vector Generation */
            Map<String, BitSet> bitVectors = new HashMap<String, BitSet>();
            String dir = FeatureVectorGenerator.getCurrentDirectory();
            File folder = new File(dir + Constants.PACKAGE_PREFIX + Constants.BB_FOLDER);
            String filePath = dir + Constants.PACKAGE_PREFIX + Constants.BB_FOLDER;
            for (File opCodeFile : folder.listFiles()) {
                System.out.print(opCodeFile.getName()+"\n");
                if (opCodeFile.toString().contains(Constants.MAC_STORE)) {
                    continue;
                }
                //System.out.println("Input to readOpCode : "+filePath+" "+opCodeFile.getName());
                FeatureVectorGenerator.readOpCode(filePath + opCodeFile.getName());
            /* k = 5, m = 30000 */
            Map<Long, List<FeatureVectorNode>> featureVector = FeatureVectorGenerator.processOpCode(k, m);
            BitSet bitVec = FeatureVectorGenerator.generateBitVector(featureVector);
            bitVectors.put(opCodeFile.getName(), bitVec);
                FeatureVectorGenerator.serializeFeatureVector(featureVector, opCodeFile.getName());
                featureVector = null;
            }
            FeatureVectorGenerator.serializeBitVector(bitVectors);


        /* Clustering */
            ItemT[] items = null;
            dir = FeatureVectorGenerator.getCurrentDirectory() + Constants.PACKAGE_PREFIX;
            //System.out.println("File being read : " + dir + Constants.BIT_VECTOR_FILE + Constants.OUTPUT_FORMAT);
            items = Agglomerate.processInput(items, dir + Constants.BIT_VECTOR_FILE + Constants.OUTPUT_FORMAT);
            int numOfItems = items.length;
            Agglomerate.setLinkage(linkageInputType);
            ClusterT cluster = null;
            if (numOfItems > 0) {
                cluster = Agglomerate.agglomerate(numOfItems, items);
                if (cluster != null) {
                    System.out.println("CLUSTER HIERARCHY \n--------------------");
                    Agglomerate.printCluster(cluster);
                    PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
                    System.setOut(out);
                    System.out.println(numClusters + " CLUSTERS \n--------------------");
                    Agglomerate.getKClusters(cluster, numClusters);
                    System.setOut(System.out);
                }

            }

            System.out.println("Enter APK-Name:- ");
            String apkname = s.nextLine();
            BitSet coords = null;
            boolean APIfound = false;
            for (ItemT item : items) {
                if (item.label.contains(apkname)) {
                    coords = item.coord;
                    APIfound = true;
                    break;
                }
            }
            if (APIfound) {
                ClusterNodeT[] mynodes = Agglomerate.getTopKNodes(cluster, numClusters);
                if (mynodes == null) {
                    System.out.println("Nodes Issue!");
                }
                ClusterNodeT node = Agglomerate.getNearestCluster(cluster, mynodes, coords);
                System.out.println("API most similar to Cluster with " + node.numOfItems + " items.");
                if (node.numOfItems > 0) {
                    System.out.print(cluster.nodes[node.items[0]].label + " ");
                    for (int i = 1; i < node.numOfItems; ++i)
                        System.out.print(", " + cluster.nodes[node.items[i]].label);
                }
                System.out.println();

                ClusterNodeT itemNode = Agglomerate.getNearestItem(cluster, mynodes, coords);
                System.out.println("Most similar item = " + itemNode.label);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

}
