package com.hh.Job.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileService {

    @Value("${com.hh.upload-file.base-uri}")
    private String baseUri;

    public void createDirectory(String folder) throws URISyntaxException {
        URI uri = new URI(folder);
        Path path = Paths.get(uri);
        File tmpDir = new File(path.toString());
        if(!tmpDir.isDirectory()){
            try{
                Files.createDirectories(tmpDir.toPath());
                System.out.println(">>> Create Directory Success, path = " + tmpDir.toPath());
            }catch(Exception e){
                e.printStackTrace();
            }
        }else{
            System.out.println(">>> Skip making directory, already exists ");
        }
    }

    public String store(MultipartFile file, String folder) throws URISyntaxException, IOException {
        String fileName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
        URI uri = new URI(baseUri +folder + "/" + fileName);
        Path path = Paths.get(uri);
        try(InputStream inputStream = file.getInputStream()){
            Files.copy(inputStream,path, StandardCopyOption.REPLACE_EXISTING);
        }
        return fileName;
    }

    public long getFileLength(String fileName,String folder) throws URISyntaxException {
        URI uri = new URI(baseUri +folder + "/" + fileName);
        Path path = Paths.get(uri);
        File tmpDir = new File(path.toString());

        // file klhong ton tai , hoac file la 1 director => return 0
        if(!tmpDir.exists() || tmpDir.isDirectory()){
            return  0;
        }
        return tmpDir.length();
    }

    public InputStreamResource getResource(String fileName,String folder) throws URISyntaxException, FileNotFoundException {
        URI uri = new URI(baseUri +folder + "/" + fileName);
        Path path = Paths.get(uri);
        File file = new File(path.toString());
        return new InputStreamResource(new FileInputStream(file));
    }
}
