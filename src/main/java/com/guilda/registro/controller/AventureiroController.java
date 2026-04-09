package com.guilda.registro.controller;

import com.guilda.registro.dto.AventureiroResumoResponse;
import com.guilda.registro.domain.enums.ClasseAventureiro;
import com.guilda.registro.service.AventureiroService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/aventureiros")
public class AventureiroController {

    private final AventureiroService service;

    public AventureiroController(AventureiroService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<AventureiroResumoResponse>> listar(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) ClasseAventureiro classe,
            @RequestParam(required = false) Integer nivelMinimo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<AventureiroResumoResponse> response = service.buscarComFiltros(ativo, classe, nivelMinimo, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/busca")
    public ResponseEntity<Page<AventureiroResumoResponse>> buscarPorNome(
            @RequestParam String nome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("nome"));
        Page<AventureiroResumoResponse> response = service.buscarPorNome(nome, pageable);

        return ResponseEntity.ok(response);
    }
}