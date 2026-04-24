package com.taller.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taller.backend.entity.Tuneo;
import com.taller.backend.repository.TuneoRepository;

@RestController
@RequestMapping("/api/tuneo")
public class TuneoController {

    private final TuneoRepository tuneoRepository;

    public TuneoController(TuneoRepository tuneoRepository) {
        this.tuneoRepository = tuneoRepository;
    }

    @GetMapping
    public List<Tuneo> listarTuneos() {
        return tuneoRepository.findAll();
    }
}