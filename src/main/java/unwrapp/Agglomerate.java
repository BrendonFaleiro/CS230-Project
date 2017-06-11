package unwrapp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class Agglomerate{
    private static String linkageString;
    private static final int bitvectorsize = 240007;

    public static double euclideanDistance(BitSet a, BitSet b){
        /* This method evaluates jaccard distance between given two bit vectors */
        BitSet x = (BitSet)a.clone();
        BitSet y = (BitSet)a.clone();

        x.and(b); // Result placed in x
        y.or(b); // Result placed in y

        if(y.cardinality() == 0)
            System.out.println("Bitvector cannot be zero. apk must have some feature atleast");
        double xc = x.cardinality();
        double yc = y.cardinality();

        double score = 1.0 - xc/yc;
        return score;
    }

    public static void fillEuclideanDistances(double[][] matrix, int numOfItems, ItemT[] items) {
        System.out.println("Matrix is : ");
        for (int i = 0; i < numOfItems; ++i) {
            System.out.print(items[i].label+"\t\t\t\t\t");
        }
        System.out.println();

        for (int i = 0; i < numOfItems; ++i) {
            for (int j = 0; j < numOfItems; ++j) {
                matrix[i][j] = euclideanDistance(items[i].coord, items[j].coord);
                matrix[j][i] = matrix[i][j];
                System.out.print(matrix[i][j]+"\t\t\t");
            }
            System.out.println();
        }
    }

    public static double[][] generateDistanceMatrix(int numOfItems, final ItemT[] items){
        double[][] matrix = new double[numOfItems][numOfItems];
        if (matrix != null) {
            fillEuclideanDistances(matrix, numOfItems, items);
        }
        else
            System.err.println("Failed to allocate memory for distance matrix"); /* Although alloc_fail operates on low level
                                                                           memory level - which java don't */
        return matrix;
    }

    // takes two clusters and returns the min distance between 2 items - one from cluster a and the other from cluster b
    public static double singleLinkage(double[][] distances, final int[] a, final int[] b, int m, int n) {
        double min = Double.MAX_VALUE;
        double d = 0.0;
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                d = distances[a[i]][b[j]];
                if (d < min)
                    min = d;
            }
        }
        return min;
    }

    // takes two clusters and returns the max distance between 2 items - one from cluster a and the other from cluster b
    public static double completeLinkage(double[][] distances, final int[] a, final int[] b, int m, int n){
        double d = 0.0;
        double max = 0.0;  /* assuming distances are positive */
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                d = distances[a[i]][b[j]];
                if (d > max)
                    max = d;
            }
        }
        return max;
    }

    // return average distance between 2 clusters
    public static double averageLinkage(double[][] distances, final int[] a, final int[] b, int m, int n) {
        double total = 0.0;
        for (int i = 0; i < m; ++i){
            for (int j = 0; j < n; ++j) {
                total += distances[a[i]][b[j]];
            }
        }
        return total /(m * n);
    }

    public static double getDistance(ClusterT cluster, int index, int target) {
        /* if both are leaves, just use the distances matrix */
        if (index < cluster.numOfItems && target < cluster.numOfItems)
            return cluster.distances[index][target];
        else {
            ClusterNodeT a = cluster.nodes[index];
            ClusterNodeT b = cluster.nodes[target];
            switch (linkageString) {
                case AgglomerateConstants.AVERAGE_LINKAGE:
                    return averageLinkage(cluster.distances, a.items, b.items, a.numOfItems, b.numOfItems);
                case AgglomerateConstants.COMPLETE_LINKAGE:
                    return completeLinkage(cluster.distances, a.items, b.items, a.numOfItems, b.numOfItems);
                /*
                case AgglomerateConstants.CENTROID_LINKAGE:
                      distance_fptr = centroid_linkage;
                    break;
                */

            }
            return singleLinkage(cluster.distances, a.items, b.items, a.numOfItems, b.numOfItems);
        }
    }

    public static void insertBefore(NeighbourT current, NeighbourT neighbours, ClusterNodeT node) {
        neighbours.next = current;
        if (current.prev != null) {
            current.prev.next = neighbours;
            neighbours.prev = current.prev;
        } else
            node.neighbours = neighbours;
        current.prev = neighbours;
    }

    public static void insertAfter(NeighbourT current, NeighbourT neighbours) {
        neighbours.prev = current;
        current.next = neighbours;
    }

    public static void insertSorted(ClusterNodeT node, NeighbourT neighbours) {
        NeighbourT temp = node.neighbours;
        while (temp.next != null) {
            if (temp.distance >= neighbours.distance) {
                insertBefore(temp, neighbours, node);
                return;
            }
            temp = temp.next;
        }
        if (neighbours.distance < temp.distance)
            insertBefore(temp, neighbours, node);
        else
            insertAfter(temp, neighbours);
    }

    public static NeighbourT addNeighbour(ClusterT cluster, int index, int target) {
        NeighbourT neighbour = new NeighbourT();
        neighbour.target = target;
        neighbour.distance = getDistance(cluster, index, target);
        ClusterNodeT node = cluster.nodes[index];
        if (node.neighbours != null) {
            insertSorted(node, neighbour);
        }
        else {
            node.neighbours = neighbour;
        }
        return neighbour;
    }

    public static ClusterT updateNeighbours(ClusterT cluster, int index) {
        ClusterNodeT node = cluster.nodes[index];
        if (node.type == AgglomerateConstants.NOT_USED) {
            System.err.println("Invalid cluster node at index "+index);
            cluster = null;
        }
        else {
            int rootClustersSeen = 1;
            int target = index;
            while (rootClustersSeen < cluster.numOfClusters) {
                ClusterNodeT temp = cluster.nodes[--target];
                if (temp.type == AgglomerateConstants.NOT_USED) {
                    System.err.println("Invalid cluster node at index "+index);
                    cluster = null;
                    break;
                }
                if (temp.isRoot) {
                    ++rootClustersSeen;
                    addNeighbour(cluster, index, target);
                }
            }
        }
        return cluster;
    }

    public static void intialiseLeaf(ClusterT cluster, ClusterNodeT node, final ItemT item){
        node.label = new String(item.label);
        node.centroid = item.coord;
        node.type = AgglomerateConstants.LEAF_NODE;
        node.isRoot = true;
        node.height = 0;
        node.numOfItems = 1;
        node.items[0] = cluster.numOfNodes++;
    }

    public static ClusterNodeT addLeaf(ClusterT cluster, final ItemT item) {
        ClusterNodeT leaf = cluster.nodes[cluster.numOfNodes];
        leaf.items = new int[1];
        if (leaf.items != null) {
            intialiseLeaf(cluster, leaf, item);
            cluster.numOfClusters++;
        }
        else {
            System.err.println("Failed to allocate memory for node items");
            leaf = null;
        }
        return leaf;
    }

    public static ClusterT addLeaves(ClusterT cluster, ItemT[] items) {
        for (int i = 0; i < cluster.numOfItems; ++i) {
            if (addLeaf(cluster, items[i]) != null) {
                updateNeighbours(cluster, i);
            }
            else {
                cluster = null;
                break;
            }
        }
        return cluster;
    }

    public static void printClusterItems(ClusterT cluster, int index) {
        ClusterNodeT node = cluster.nodes[index];
        System.out.print("Items: ");
        if (node.numOfItems > 0) {
            //System.out.print(cluster.nodes[node.items[0]].label +" ");
            for (int i = 0; i < node.numOfItems; ++i)
                System.out.print(cluster.nodes[node.items[i]].label + "\n");
        }
        System.out.println();
    }

    public static void printClusterNode(ClusterT cluster, int index) {
        ClusterNodeT node = cluster.nodes[index];
        if (node.label != null)
            System.out.print("\tLeaf: "+ node.label+ "\n\t");
        else
            System.out.print("\tMerged: "+node.merged[0]+" "+node.merged[1]+"\n\t");
        printClusterItems(cluster, index);
        System.out.print("\tNeighbours: ");
        NeighbourT t = node.neighbours;
        while (t != null) {
            System.out.format("\n\t\t%2d: %5.3f", t.target, t.distance);
            t = t.next;
        }
        System.out.println();
    }

    public static void mergeItems(ClusterT cluster, ClusterNodeT node, ClusterNodeT[] toMerge) {
        node.type = AgglomerateConstants.A_MERGER;
        node.isRoot = true;
        node.height = -1;
        List<BitSet> cent = new ArrayList<BitSet>();

        /* copy leaf indexes from merged clusters */
        int k = 0, idx;
        BitSet centroid = new BitSet(bitvectorsize);
        for (int i = 0; i < toMerge.length; ++i) {
            ClusterNodeT t = toMerge[i];
            t.isRoot = false;
            /* no longer root: merged */
            if (node.height == -1 || node.height < t.height)
                node.height = t.height;
                for (int j = 0; j < t.numOfItems; ++j) {
                    idx = t.items[j];
                    node.items[k++] = idx;
                }
            cent.add(t.centroid);
        }
        for(int i = 0; i < bitvectorsize; i++) {
            int count = 0;
            for(int j = 0; j < cent.size(); j++) {
                if(cent.get(j).get(i))
                    count++;
            }

            if(count >= cent.size()/2)
                centroid.set(i);
        }

        /* calculate centroid */
        node.centroid = centroid;
        node.height++;
        }


    public static ClusterNodeT merge(ClusterT cluster, Distance dObj) {
        int newIdx = cluster.numOfNodes;
        ClusterNodeT node = cluster.nodes[newIdx];
        node.merged = new int [2];
        if (node.merged != null) {
            ClusterNodeT[] toMerge = new ClusterNodeT[2];
            toMerge[0] = cluster.nodes[dObj.first];
            toMerge[1] = cluster.nodes[dObj.second];
            node.merged[0] = dObj.first;
            node.merged[1] = dObj.second;
            mergeToOne(cluster, toMerge, node, newIdx);
        } else {
            System.err.println("Failed to allocate memory for array of merged nodes");
            node = null;
        }
        return node;
    }

    public static void mergeToOne(ClusterT cluster, ClusterNodeT[] toMerge, ClusterNodeT node, int nodeIdx){
        node.numOfItems = toMerge[0].numOfItems + toMerge[1].numOfItems;
        node.items = new int[node.numOfItems];
        if (node.items != null) {
            mergeItems(cluster, node, toMerge);
            cluster.numOfNodes++;
            cluster.numOfClusters--;
            updateNeighbours(cluster, nodeIdx);
        } else {
            System.err.println("Failed to allocate memory for array of merged items");
            node = null;
        }
    }

    public static void findBestDistanceNeighbour(ClusterNodeT[] nodes, int nodeIdx, NeighbourT neighbour, Distance obj) {
        while (neighbour != null) {
            if (nodes[neighbour.target].isRoot != false) {
                if (obj.first == -1 || neighbour.distance < obj.bestDistance) {
                    obj.first = nodeIdx;
                    obj.second = neighbour.target;
                    obj.bestDistance = neighbour.distance;
                }
                break;
            }
            neighbour = neighbour.next;
        }
    }

    public static int findClustersToMerge(ClusterT cluster, Distance dObj) {
        double bestDistance = 0.0;
        int rootClustersSeen = 0;
        int j = cluster.numOfNodes;
        /* traverse hierarchy top-down */
        dObj.first = -1;
        while (rootClustersSeen < cluster.numOfClusters) {
            ClusterNodeT node = cluster.nodes[--j];
            if (node.type == AgglomerateConstants.NOT_USED || !node.isRoot)
                continue;
            ++rootClustersSeen;
            findBestDistanceNeighbour(cluster.nodes, j, node.neighbours, dObj);
        }
        return dObj.first;
    }

    public static ClusterT mergeClusters(ClusterT cluster) {
        Distance dObj = new Distance();
        while (cluster.numOfClusters > 1) {
            if (findClustersToMerge(cluster, dObj) != -1)
                merge(cluster, dObj);
        }
        return cluster;
    }

    public static void initCluster(ClusterT cluster, int numOfItems, ItemT[] items){
        cluster.distances = generateDistanceMatrix(numOfItems, items);
        if (cluster.distances == null) {
            cluster = null;
        }
        cluster.numOfItems = numOfItems;
        cluster.numOfNodes = 0;
        cluster.numOfClusters = 0;
        if (addLeaves(cluster, items) != null) {
            mergeClusters(cluster);
        }
        else
            cluster = null;
    }

    public static ClusterT agglomerate(int numOfItems, ItemT[] items) {
        ClusterT cluster = new ClusterT();
        if (cluster != null) {
            cluster.nodes = new ClusterNodeT[2 * numOfItems - 1];  // Ques : Why 2 * numOfItems-1??? Why twice ??
            for(int i =0; i < (2 * numOfItems - 1); i++){
                cluster.nodes[i] = new ClusterNodeT();
            }
            if (cluster.nodes != null){
                initCluster(cluster, numOfItems, items);
            }
            else {
                System.err.println("Failed to allocate memory for cluster nodes");
                cluster = null;
            }
        }
        else
            System.err.println("Failed to allocate memory for cluster");
        return cluster;
    }

    public static int printRootChildren(ClusterT cluster, int i, int nodesToDiscard) {
        ClusterNodeT node = cluster.nodes[i];
        int rootsFound = 0;
        if (node.type == AgglomerateConstants.A_MERGER) {
            for (int j = 0; j < 2; ++j) {
                int t = node.merged[j];
                if (t < nodesToDiscard) {
                    printClusterItems(cluster, t);
                    ++rootsFound;
                }
            }
        }
        return rootsFound;
    }

    public static void getKClusters(ClusterT cluster, int k) {
        if (k < 1)
            return;
        if (k > cluster.numOfItems)
            k = cluster.numOfItems;

        int i = cluster.numOfNodes - 1;
        int rootsFound = 0;
        int nodesToDiscard = cluster.numOfNodes - k + 1;
        while (k > 0) {
            if (i < nodesToDiscard) {
                printClusterItems(cluster, i);
                rootsFound = 1;
            }
            else {
                rootsFound = printRootChildren(cluster, i, nodesToDiscard);
            }
            k -= rootsFound;
            --i;
        }
    }

    public static void printCluster(ClusterT cluster) {
        for (int i = 0; i < cluster.numOfNodes; ++i)
            printClusterNode(cluster, i);
    }

    public static void setLinkage(String linkageType){
        linkageString = linkageType;
    }

    public static ItemT[] processInput(ItemT[] items, String absoluteFileName) {
        int numOfItems = 0;
        Map<String, BitSet> readInput = null;
        FileInputStream fileInputStream = null;
        ObjectInputStream ois = null;
        try{
            fileInputStream = new FileInputStream(absoluteFileName);
            ois = new ObjectInputStream(fileInputStream);
            readInput  = (Map<String, BitSet>)ois.readObject();
            numOfItems = readInput.size();
            items = new ItemT[numOfItems];
            int i =0;
            for (Map.Entry<String, BitSet> entry : readInput.entrySet()) {
                ItemT t = new ItemT();
                t.label = entry.getKey().trim();
                t.coord = entry.getValue();
                items[i]= t;
                i++;
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        catch(ClassNotFoundException cf){
            cf.printStackTrace();
        }
        finally {
            try {
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return items;
    }

    public static ClusterNodeT[] getTopKNodes(ClusterT cluster, int k){
        if (k < 1)
            return null;
        if (k > cluster.numOfItems)
            k = cluster.numOfItems;
			
        ClusterNodeT[] nodes = new ClusterNodeT[k];
        int i = cluster.numOfNodes - 1;
        int nodesToDiscard = cluster.numOfNodes - k + 1;
		int pos=0;
		
        while (k > 0) {
	    int rootsFound = 0;
            if (i < nodesToDiscard) {
                printClusterItems(cluster, i);
                nodes[pos++] = cluster.nodes[i];
                rootsFound++;
            }
            else {
			
                ClusterNodeT node = cluster.nodes[i];
				
                if (node.type == AgglomerateConstants.A_MERGER) {
                    for (int j = 0; j < 2; ++j) {
                        int t = node.merged[j];
                        if (t < nodesToDiscard) {
                            nodes[pos++] = cluster.nodes[t];
                            rootsFound++;
                        }
                    }
                }
				
            }
            k -= rootsFound;
            --i;
        }
        return nodes;
    }
	
    public static ClusterNodeT getNearestCluster(ClusterT cluster, ClusterNodeT[] topClusters, BitSet bitVector){
	
        ClusterNodeT similarNode = new ClusterNodeT();
        double distance = Double.MAX_VALUE;
		
        for(ClusterNodeT node : topClusters){
            if (node==null) {continue;}
            double d = euclideanDistance(bitVector, node.centroid);
			
            if(distance > d){
                distance = d;
                similarNode = node;
            }
        }
		
        return similarNode;
    }
	
    public static ClusterNodeT getNearestItem(ClusterT cluster, ClusterNodeT[] topClusters, BitSet bitVector){
        ClusterNodeT startNode = getNearestCluster(cluster,topClusters,bitVector);
        return similarNodeTraversal(cluster,startNode,bitVector);
    }
	
    public static ClusterNodeT similarNodeTraversal(ClusterT cluster, ClusterNodeT simNode, BitSet bitVector){
	
        ClusterNodeT similarNode = null;
        double distance = Double.MAX_VALUE;
        if(simNode.type == AgglomerateConstants.LEAF_NODE){
            return simNode;
        }
        else{
            if(simNode == null){
                System.out.println("Error!! Node uninitialized");
            }
            else{
                for(int i=0; i<simNode.merged.length; i++){
                    double d = euclideanDistance(bitVector, cluster.nodes[simNode.merged[i]].centroid);
                    if(distance>d){
                        distance = d;
                        similarNode = cluster.nodes[simNode.merged[i]];
                    }
                }
            }
            return similarNodeTraversal(cluster, similarNode, bitVector);
        }
    }
	
    public static void main(String[] args) {
        /* Moved this part of code to Execute.java */
        /*
        ItemT[] items = null;
        String dir = System.getProperty("user.dir")+"/"+ Constants.PACKAGE_PREFIX ;
        System.out.println("File being read : "+dir+ Constants.BIT_VECTOR_FILE+ Constants.OUTPUT_FORMAT);
        items = processInput(items, dir+ Constants.BIT_VECTOR_FILE+ Constants.OUTPUT_FORMAT);
        int numOfItems = items.length;
        setLinkage("c");
        if (numOfItems > 0) {
            ClusterT cluster = agglomerate(numOfItems, items);
            if (cluster != null) {
                System.out.println("CLUSTER HIERARCHY \n--------------------");
                printCluster(cluster);
                int k = 1;//Integer.parseInt(argv[2]);
                System.out.println(k + " CLUSTERS \n--------------------");
                getKClusters(cluster, k);
            }
        }
        */
    }

}