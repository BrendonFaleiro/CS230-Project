package unwrapp.storage;

/**
 * Created by Shubham Mittal on 6/07/17.
 */


import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

public interface StorageService {

    void init();

    void store(MultipartFile file);

    void storeSortedApkList(List<String> list);

    List<ApkObj> getApkObject();

    void deleteAll();

}
