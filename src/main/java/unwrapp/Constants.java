package unwrapp;

/**
 * Created by Shubham Mittal on 5/17/17.
 * @edited : swati.arora
 */

// List of constants used
public final class Constants {

    /* Add constants alphabetically */
    public static final String ACTIVITY = "<activity";
    public static final String ANDROID_NAME = "android:name";
    public static final String APK_EXTRACTED_FOLDER = "extracted/"; /* Folder to store extracted apk's */
    public static final String APK_NAMES_FILE = "apknames.txt";
    public static final String ARFF_FORMAT = ".arff";
    public static final String BB_FOLDER = "BBFile/";   /* Folder containing BB File */
    public static final String BENIGN_LABEL = "benign";
    public static final String BIT_VECTOR_FILE = "bitVectors";
    public static final String COMMA_DELIMITER = ", ";
    public static final String CONST_STRING = "const-string";
    public static final String CSV = ".csv";
    public static final String END_METHOD = ".end method";
    public static final String FEATURE_FOLDER = "features/";
    public static final String FEATURE_MATRIX = "feature_mat";
    public static final String FILE_HEADER = ".class";
    public static final String FV_FOLDER = "FVFile/";     /* Folder to store serialized information of feature vectors */
    public static final String LABELS = "Labels";
    public static final String METHOD = ".method";
    public static final String MAC_STORE = ".DS_Store";     /* Mac Store File */
    public static final String MALWARE = "malware/";
    public static final String MALWARE_LABEL = "malware";
    public static final String MANIFEST_FILE = "AndroidManifest.xml";
    public static final String NEWLINE_CHARACTER = "\n";
    public static final String ORIGINAL = "original/";
    public static final String OUTPUT_FORMAT = ".out";
    public static final String PACKAGE_PREFIX = "src/main/resources/";
    public static final String PERMISSIONS_FILE = "PermissionList";
    public static final String PERMISSIONS_FOLDER = "permissions/";
    public static final String PERMISSION_OUT = ".perm";
    public static final String PERMISSION_RESULT_FILE = "permissionResults.txt";
    public static final String RECEIVER = "<receiver";
    public static final String RESULTS = "results/";
    public static final String SMALI = "smali";
    public static final String TOP_FEATURES = "top_features";
    public static final String TEST_APK_DIR = "testApk/";
    public static final String TRIMMED_FEATURE_MATRIX = "trimmed_feature_mat";
    public static final String TRACE_BACK_FILE = "traceback";     /* Trace back file */
    public static final String TRACE_FILE_OUTPUT_FORMAT = ".txt";     /* Trace back file extension */
    public static final String USES_PERMISSION = "<uses-permission";     /* Mac Store File */
}

final class AgglomerateConstants{
    public static final int NOT_USED = 0;       /* node is currently not used */
    public static final int LEAF_NODE = 1;      /* node contains a leaf node */
    public static final int A_MERGER = 2;       /* node contains a merged pair of root clusters */
    public static final int MAX_LABEL_LEN = 16;
    public static final String AVERAGE_LINKAGE = "a";     /* choose average distance */
    public static final String CENTROID_LINKAGE = "t";     /* choose distance between cluster centroids */
    public static final String COMPLETE_LINKAGE = "c";     /* choose maximum distance */
    public static final String SINGLE_LINKAGE = "s";       /* choose minimum distance */
}