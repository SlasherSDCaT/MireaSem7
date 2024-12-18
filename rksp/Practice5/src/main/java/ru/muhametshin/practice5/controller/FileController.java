package ru.muhametshin.practice5.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.muhametshin.practice5.entity.File;
import ru.muhametshin.practice5.repository.FileRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class FileController {

    private final FileRepository fileRepository;

    @GetMapping("/")
    public String listUploadedFiles(Model model){
        List<File> files = fileRepository.findAll();
        model.addAttribute("files", files);
        return "index";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename();
                File newFile = File.builder()
                        .fileName(originalFilename)
                        .fileSize(file.getSize())
                        .fileData(file.getBytes())
                        .build();
                fileRepository.save(newFile);
                return "redirect:/";
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return "redirect:/";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> serveFile(@PathVariable Long id) {
        Optional<File> fileOptional = fileRepository.findById(id);
        if (fileOptional.isPresent()) {
            File file = fileOptional.get();
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"");
            return new ResponseEntity<>(file.getFileData(), headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteFile(@PathVariable Long id) {
        fileRepository.deleteById(id);
        return "redirect:/";
    }
    
}
