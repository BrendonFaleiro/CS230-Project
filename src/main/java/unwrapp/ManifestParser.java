package unwrapp;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Shubham Mittal on 5/27/17.
 */
public class ManifestParser {
    public void parseManifest(final String apkName, final String sampledData) throws Exception {
        String pathToApk = Constants.PACKAGE_PREFIX + Constants.APK_EXTRACTED_FOLDER + sampledData + apkName + "/", mline, trimLine;

        //Read Manifest and extract activity list & permission list
        List<String> activityList = new ArrayList<>();
        Set<String> permissionSet = new HashSet<>();
        BufferedReader brManifest = new BufferedReader(new FileReader(pathToApk + Constants.MANIFEST_FILE));
        while ((mline = brManifest.readLine()) != null) {
            trimLine = mline.trim();
            if (trimLine.startsWith(Constants.ACTIVITY) || trimLine.startsWith(Constants.RECEIVER)) {
                //extract the activity name
                String[] strArr = mline.split("[= ]+");
                for (int i = 0; i < strArr.length; i++) {
                    if (strArr[i].contains(Constants.ANDROID_NAME)) {
                        String temp = strArr[i + 1].trim();
                        activityList.add(Constants.SMALI + "." + temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\"")));
                        break;
                    }
                }
            } else if (trimLine.startsWith(Constants.USES_PERMISSION)) {
                String[] strArr = mline.split("[= ]+");
                for (int i = 0; i < strArr.length; i++) {
                    if (strArr[i].contains(Constants.ANDROID_NAME)) {
                        String temp = strArr[i + 1].trim();
                        String permission = temp.substring(temp.lastIndexOf(".")+1, temp.lastIndexOf("\""));
                        permissionSet.add(permission);
                        break;
                    }
                }
            }
        }
        brManifest.close();

        final List<List<String>> listOfLists = new ArrayList<>();

        //Process the activity list to extract the opcode list
        activityList.parallelStream()
                .map(str -> pathToApk+str.replace(".","/"))
                .forEach((pathToActivity) -> {
                    try {
                        String activityLoc = pathToActivity + "." + Constants.SMALI;
                        if (Files.exists(Paths.get(activityLoc))) {
                            BufferedReader brActivity = new BufferedReader(new FileReader(activityLoc));

                            String tline, trimmedLine;
                            String[] strArr;
                            List<String> opCodeList = new ArrayList<>();

                            while ((tline = brActivity.readLine()) != null) {
                                trimmedLine = tline.trim();
                                strArr = trimmedLine.split(" ");
                                if (trimmedLine.startsWith(Constants.FILE_HEADER)) {
                                    opCodeList.add(Constants.FILE_HEADER + " " + strArr[strArr.length - 1].replace(";", ""));
                                } else if (trimmedLine.startsWith(Constants.METHOD)) {
                                    // Extract method name
                                    tline = strArr[strArr.length - 1];
                                    opCodeList.add(Constants.METHOD + " " + tline.substring(0, tline.indexOf("(")));

                                    //Continue extracting till the method end
                                    while ((tline = brActivity.readLine()) != null) {
                                        trimmedLine = tline.trim();
                                        if (trimmedLine.equals(Constants.END_METHOD)) {
                                            break;
                                        } else if (trimmedLine.startsWith(".") || trimmedLine.isEmpty()) {
                                            continue;
                                        }
                                        strArr = trimmedLine.split(" ");
                                        if (strArr[0].equals(Constants.CONST_STRING)) {
                                            opCodeList.add(strArr[0] + " " + strArr[strArr.length - 1]);
                                        } else {
                                            opCodeList.add(strArr[0]);
                                        }
                                    }
                                }
                            }
                            brActivity.close();
                            listOfLists.add(opCodeList);
                        }
                    } catch (IOException e) {
                        System.out.println("IOException while parsing Manifest !");
                        //e.printStackTrace();
                    }
                });

            /*Instant check3 = Instant.now();
            System.out.println("\nCheck 3 -- Activity Processing Time: " + Duration.between(check2, check3));
            System.out.println("Check 3 -- Cumulative Time: " + Duration.between(start, check3));*/

        if (listOfLists.isEmpty()) {
            System.out.println("No opcodes extracted for apk:: + " + apkName);
            return;
        } else {
            Utils.serialize(listOfLists, Constants.PACKAGE_PREFIX + Constants.BB_FOLDER + apkName + Constants.OUTPUT_FORMAT);
            System.out.println("Finished processing apk:" + apkName);
        }


        if (!permissionSet.isEmpty()) {
            for (String permission : permissionSet) {
                // Increment the global permission value
                if (Utils.globalPermMap.containsKey(permission)) {
                    if (permission.equals("BIND_AUTOFILL")) {
                        System.out.println("ASDAS");
                    }
                    Utils.globalPermMap.put(permission, Utils.globalPermMap.get(permission)+1);
                }
            }
            if (sampledData.equals(Constants.ORIGINAL)) {
                Utils.benignCount++;}
            else {
                Utils.malwareCount++;}
            int[] bitVector = createFeatureVec(permissionSet);
            Utils.globalFeatureMatrix.add(bitVector);
            // Write individual feature vectors
            Utils.serialize(bitVector, Constants.PACKAGE_PREFIX + Constants.PERMISSIONS_FOLDER + sampledData + apkName + Constants.PERMISSION_OUT);
        } else {
            System.out.println("No Permissions extracted for Apk:" + apkName);
        }
    }

    private int[] createFeatureVec(Set<String> permissionSet) {
        int[] bitVector = new int[Utils.globalPermMap.size()];
        for (String key : permissionSet) {
            if (Utils.permIndexMap.containsKey(key)) {
                bitVector[Utils.permIndexMap.get(key)] = 1;
            }
        }
        return bitVector;
    }
}
