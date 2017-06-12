package unwrapp;


/**
 * Created by Shubham Mittal on 6/07/17.
 */

import unwrapp.storage.StorageFileNotFoundException;
import unwrapp.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/display")
    public String listSimilarityScores(Model model) throws IOException {

        model.addAttribute("apkObject", storageService.getApkObject());

        return "display";
    }

    @GetMapping("/authors")
    public String listAuthors(Model model) throws IOException {
        return "authors";
    }

    @GetMapping("/upload")
    public String listUploadedFiles(Model model) throws IOException {
        return "uploadForm";
    }


    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        storageService.store(file);
        String fileName = file.getOriginalFilename();
        redirectAttributes.addFlashAttribute("apkName", fileName.substring(0,fileName.lastIndexOf(".")));

        try {
            ApkExtractor.main(new String[1]);
        } catch (Exception e) {
            System.out.println("Unable to call apkextractor function !");
        }

        String uploadedFileName = file.getOriginalFilename();
        System.out.println("File Uploaded: " + uploadedFileName);
        String trimmedFile = uploadedFileName.substring(0, uploadedFileName.lastIndexOf("."));
        System.out.println("ApkName: " + trimmedFile);
        List<String> sortedList = WebServiceUtil.execute(trimmedFile);
        storageService.storeSortedApkList(sortedList);
        return "redirect:/display";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
}