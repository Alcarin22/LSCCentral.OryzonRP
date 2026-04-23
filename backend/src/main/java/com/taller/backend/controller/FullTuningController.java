package com.taller.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taller.backend.entity.FullTuning;
import com.taller.backend.repository.FullTuningRepository;

@RestController
@RequestMapping("/api/full-tuning")
public class FullTuningController {

    private final FullTuningRepository fullTuningRepository;

    public FullTuningController(FullTuningRepository fullTuningRepository) {
        this.fullTuningRepository = fullTuningRepository;
    }

    @GetMapping
    public List<FullTuning> listarFullTuning() {
        return fullTuningRepository.findAll();
    }
}