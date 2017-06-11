package unwrapp;

/**
 * Created by Shubham Mittal on 5/17/17.
 */

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class LegacyApkProcessor {

    public static void main(String[] args) {
        Instant start = Instant.now();

        // Extract All APKs
        File folder = new File(Constants.PACKAGE_PREFIX + Constants.ORIGINAL);
        String cmd = null, destDir;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(Constants.PACKAGE_PREFIX + Constants.APK_NAMES_FILE));    //Write ApkNames to apknames.txt
            int iter=0;
            for (File apkFile : folder.listFiles()) {
                String apkFileName=apkFile.toString().substring(apkFile.toString().lastIndexOf("/")+1, apkFile.toString().lastIndexOf("."));
                destDir = Constants.PACKAGE_PREFIX + Constants.APK_EXTRACTED_FOLDER + apkFileName;
                cmd = "java -jar " + Constants.PACKAGE_PREFIX + "apktool_2.2.2.jar d " + apkFile + " -o " + destDir;
                Process p = Runtime.getRuntime().exec(cmd);
                // Java -- perform batching ??
                p.waitFor();
                bw.write(apkFileName);
                bw.newLine();
                System.out.println("Finished Processing " + apkFile + ".apk");
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            System.out.println("Problem while running apktool");e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Problem with waiting");e.printStackTrace();
        }
        Instant check1 = Instant.now();
        System.out.println("APKs Extraction time: " + Duration.between(start, check1));

        // Parse Manifest.xml
        try {
            BufferedReader br = new BufferedReader(new FileReader(Constants.PACKAGE_PREFIX + Constants.APK_NAMES_FILE));
            String apkName;
            while ((apkName = br.readLine()) != null) {
                String pathToApk = Constants.PACKAGE_PREFIX + Constants.APK_EXTRACTED_FOLDER + apkName + "/", mline;

                //Read Manifest and extract activity list
                List<String> activityList = new ArrayList<String>();
                BufferedReader brManifest = new BufferedReader(new FileReader(pathToApk + Constants.MANIFEST_FILE));
                while ((mline = brManifest.readLine()) != null) {
                    if (mline.trim().startsWith(Constants.ACTIVITY)) {
                        //extract the activity name
                        String[] strArr = mline.split("[= ]+");
                        for (int i = 0; i < strArr.length; i++) {
                            if (strArr[i].contains(Constants.ANDROID_NAME)) {
                                String temp = strArr[i + 1].trim();
                                activityList.add(Constants.SMALI + "." + temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\"")));
                                break;
                            }
                        }
                    }
                }
                brManifest.close();

                Instant check2 = Instant.now();
                System.out.println("\nCheck 2 -- Manifest Processing Time: " + Duration.between(check1, check2));
                System.out.println("Check 2 -- Cumulative Time: " + Duration.between(start, check2));

                final String apk = apkName;
                List<List<String>> listOfLists = new ArrayList<>();
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
                                    List<String> opCodeList = new ArrayList<String>();

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
                                e.printStackTrace();
                            }
                        });

                Instant check3 = Instant.now();
                System.out.println("\nCheck 3 -- Activity Processing Time: " + Duration.between(check2, check3));
                System.out.println("Check 3 -- Cumulative Time: " + Duration.between(start, check3));

                //Serialize list
                FileOutputStream fileOutputStream = new FileOutputStream(Constants.PACKAGE_PREFIX + Constants.BB_FOLDER + apk + Constants.OUTPUT_FORMAT);
                ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
                oos.writeObject(listOfLists);
                oos.flush();
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}