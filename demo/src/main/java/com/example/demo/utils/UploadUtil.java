package com.example.demo.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component

public class UploadUtil {


    public File handelUploadFile(MultipartFile uploadFile) {
        String dir = System.getProperty("user.dir");
        String folderPath = dir + "\\src\\main\\mapdata\\osm";
        File myUploadFolder = new File(folderPath);


        File savedFile = null;
        try {
            savedFile = new File(myUploadFolder, uploadFile.getOriginalFilename());
            uploadFile.transferTo(savedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(">>>Upload Osm file successfully!");
        return savedFile;
    }

}
