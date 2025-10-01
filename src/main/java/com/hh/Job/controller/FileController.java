package com.hh.Job.controller;

import com.hh.Job.domain.response.file.ResUploadFileDTO;
import com.hh.Job.service.FileService;
import com.hh.Job.util.annotation.APImessage;
import com.hh.Job.util.error.StorageException;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;


@RestController
@RequestMapping("/api/v1")
public class FileController {

    @Value("${com.hh.upload-file.base-uri}")
    private String baseUri;

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/files")
    @APImessage("upload single file")
    public ResponseEntity<ResUploadFileDTO> upLoadFile(@RequestParam(name = "file", required = false) MultipartFile file,
                                                       @RequestParam("folder") String folder) throws URISyntaxException, IOException {

        //validate
        if(file == null || file.isEmpty()) {
            throw new StorageException("file is empty, please up load a file");
        }

        String fileName = file.getOriginalFilename();
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png","pdf","doc","docx");

        boolean isValid = allowedExtensions.stream().anyMatch(item -> fileName.toLowerCase().endsWith(item));

        if(!isValid) {
            throw new StorageException("file extension not allowed" + allowedExtensions.toString());
        }
        //create a directory if not exist
        this.fileService.createDirectory(baseUri + folder);
        //store fil
        String uploadFile = this.fileService.store(file, folder);

        //
        ResUploadFileDTO resUploadFileDTO = new ResUploadFileDTO(uploadFile, Instant.now());

        return ResponseEntity.ok().body(resUploadFileDTO);

    }

    @GetMapping("/files")
    @APImessage("download a file")
    public ResponseEntity<Resource> downloadFile(@RequestParam(name = "fileName", required = false) String fileName,
                                                 @RequestParam(name = "folder", required = false) String folder
    )
            throws URISyntaxException, StorageException, FileNotFoundException {
        if(fileName == null || folder == null) {
            throw new StorageException("Missing required parameters : (fileName or folder)");
        }

        // check file exist
        long fileLength = this.fileService.getFileLength(fileName,folder);
        if(fileLength == 0) {
            throw new StorageException("file with name = " + fileName + " not found");
        }
        InputStreamResource resource = this.fileService.getResource(fileName,folder);
        return  ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(fileLength)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body((Resource) resource);
    }
}
