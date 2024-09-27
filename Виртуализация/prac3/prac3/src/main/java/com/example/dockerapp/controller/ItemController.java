package com.example.dockerapp.controller;

import com.example.dockerapp.model.Item;
import com.example.dockerapp.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public byte[] getGerb() {
        try {
            return this.getClass().getResourceAsStream("/static/mirea_gerb.png").readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось загрузить герб");
        }
    }
}
