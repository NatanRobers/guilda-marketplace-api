package com.guilda.registro.service;

import com.guilda.registro.domain.aventura.Aventureiro;
import com.guilda.registro.dto.AventureiroResumoResponse;
import com.guilda.registro.domain.enums.ClasseAventureiro;
import com.guilda.registro.mapper.AventureiroMapper;
import com.guilda.registro.repository.AventureiroRepository;
import com.guilda.registro.repository.AventureiroSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AventureiroService {

    private final AventureiroRepository repository;
    private final AventureiroMapper mapper;

    public AventureiroService(AventureiroRepository repository, AventureiroMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Page<AventureiroResumoResponse> buscarComFiltros(Boolean ativo, ClasseAventureiro classe, Integer nivelMinimo, Pageable pageable) {
        var spec = AventureiroSpecifications.comFiltros(ativo, classe, nivelMinimo);

        return repository.findAll(spec, pageable).map(mapper::toResumoResponse);
    }

    public Page<AventureiroResumoResponse> buscarPorNome(String nome, Pageable pageable) {
        return repository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(mapper::toResumoResponse);
    }
}