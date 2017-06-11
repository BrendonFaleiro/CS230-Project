package unwrapp;

class ClusterT{
    int numOfItems; /* number of items that was clustered */
    int numOfClusters; /* current number of root clusters */
    int numOfNodes; /* number of leaf and merged clusters */
    ClusterNodeT[] nodes; /* leaf and merged clusters */
    double[][] distances; /* distance between leaves */
}