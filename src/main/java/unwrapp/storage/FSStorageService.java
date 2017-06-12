package unwrapp.storage;

/**
 * Created by Shubham Mittal on 6/07/17.
 */


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class FSStorageService implements StorageService {

    private final Path rootLocation;
    private List<ApkObj> apkSimilarList = null;

    @Autowired
    public FSStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
            }
            Files.copy(file.getInputStream(), this.rootLocation.resolve(file.getOriginalFilename()));
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public void storeSortedApkList(List<String> sortedList) {
        apkSimilarList = new ArrayList<>();

        int iter=0;
        for (String str: sortedList) {
            if (iter++==5){break;}
            String[] strArr = str.split(" ");
            ApkObj apkObj = new ApkObj(iter, strArr[0].substring(0, strArr[0].lastIndexOf(".")), 1 - Float.parseFloat(strArr[1]));
            apkSimilarList.add(apkObj);
        }

        Collections.sort(apkSimilarList, new Comparator<ApkObj>() {
            @Override
            public int compare(ApkObj o1, ApkObj o2) {
                Float val2 = o2.apkScore;
                Float val1 = o1.apkScore;
                return val1.compareTo(val2);
            }
        });
    }

    @Override
    public List<ApkObj> getApkObject() {
        return apkSimilarList;
    }


    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectory(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
