package com.example.dockerapp.controller;

import com.example.dockerapp.model.Item;
import com.example.dockerapp.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @PostMapping
    public Item addItem(@RequestBody Item item) {
        return itemRepository.save(item);
    }

    @GetMapping
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @GetMapping("/gerb")
    public ResponseEntity<byte[]> getGerb() {
        try {
            // Загрузим файл герба из ресурсов
            InputStream inputStream = new ClassPathResource("static/MIREA_Gerb_Colour.png").getInputStream();

            // Чтение изображения как байты
            byte[] imageBytes = inputStream.readAllBytes();

            // Установка заголовков и возврат изображения
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "image/png");

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить герб", e);
        }
    }
}
