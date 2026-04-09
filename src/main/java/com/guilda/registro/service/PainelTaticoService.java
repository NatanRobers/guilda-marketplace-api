package com.guilda.registro.service;

import com.guilda.registro.domain.aventura.PainelTaticoMissao;
import com.guilda.registro.repository.PainelTaticoRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class PainelTaticoService {

    private final PainelTaticoRepository repository;

    public PainelTaticoService(PainelTaticoRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "topMissoes")
    public List<PainelTaticoMissao> buscarTopMissoesRecentes() {
        OffsetDateTime limiteData = OffsetDateTime.now().minusDays(15);
        return repository.findByUltimaAtualizacaoAfterOrderByIndiceProntidaoDesc(limiteData, PageRequest.of(0, 10));
    }
}