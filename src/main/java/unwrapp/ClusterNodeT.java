package unwrapp;

import java.util.BitSet;

public class ClusterNodeT{
    int type;           /* type of the cluster node */
    boolean isRoot;         /* true if cluster hasn't merged with another */
    int height;         /* height of node from the bottom */
    BitSet centroid;    /* centroid of this cluster */
    String label;        /* label of a leaf node */
    int[] merged;        /* indexes of root clusters merged */
    int numOfItems;     /* number of leaf nodes inside new cluster */
    int[] items;        /* array of leaf nodes indices inside merged clusters */
    NeighbourT neighbours; /* sorted linked list of distances to roots */
}