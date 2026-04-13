package com.guilda.registro.controller;

import com.guilda.registro.domain.aventura.Aventureiro;
import com.guilda.registro.domain.aventura.Missao;
import com.guilda.registro.domain.enums.PapelMissao;
import com.guilda.registro.repository.AventureiroRepository;
import com.guilda.registro.repository.MissaoRepository;
import com.guilda.registro.repository.ParticipacaoMissaoRepository;
import com.guilda.registro.service.ParticipacaoMissaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/participacoes")
public class ParticipacaoController {

    private final ParticipacaoMissaoService service;
    private final MissaoRepository missaoRepository;
    private final AventureiroRepository aventureiroRepository;
    private final ParticipacaoMissaoRepository participacaoRepository;

    public ParticipacaoController(
            ParticipacaoMissaoService service,
            MissaoRepository missaoRepository,
            AventureiroRepository aventureiroRepository,
            ParticipacaoMissaoRepository participacaoRepository
    ) {
        this.service = service;
        this.missaoRepository = missaoRepository;
        this.aventureiroRepository = aventureiroRepository;
        this.participacaoRepository = participacaoRepository;
    }

    public record ParticipacaoRequest(Long missaoId, Long aventureiroId, PapelMissao papel) {}

    @PostMapping
    public ResponseEntity<String> registrar(@RequestBody ParticipacaoRequest request) {

        Missao missao = missaoRepository.findById(request.missaoId())
                .orElseThrow(() -> new RuntimeException("Missão não encontrada na base de dados."));

        Aventureiro aventureiro = aventureiroRepository.findById(request.aventureiroId())
                .orElseThrow(() -> new RuntimeException("Aventureiro não encontrado na base de dados."));

        var participacao = service.registrarParticipacao(missao, aventureiro, request.papel());
        participacaoRepository.save(participacao);

        return ResponseEntity.ok("O aventureiro ingressou na missão com sucesso!");
    }
}