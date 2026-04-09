package com.guilda.registro.service;

import com.guilda.registro.domain.aventura.Missao;
import com.guilda.registro.domain.enums.NivelPerigo;
import com.guilda.registro.domain.enums.StatusMissao;
import com.guilda.registro.dto.MissaoDetalhadaResponse;
import com.guilda.registro.mapper.MissaoMapper;
import com.guilda.registro.repository.MissaoRepository;
import com.guilda.registro.repository.MissaoSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class MissaoService {

    private final MissaoRepository repository;
    private final MissaoMapper mapper;

    public MissaoService(MissaoRepository repository, MissaoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Page<MissaoDetalhadaResponse> listarMissoes(StatusMissao status, NivelPerigo perigo, OffsetDateTime inicio, OffsetDateTime termino, Pageable pageable) {
        var spec = MissaoSpecifications.comFiltros(status, perigo, inicio, termino);
        return repository.findAll(spec, pageable).map(mapper::toDetalhadaResponse);
    }

    public MissaoDetalhadaResponse buscarPorId(Long id) {
        Missao missao = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Missão não encontrada"));
        return mapper.toDetalhadaResponse(missao);
    }

    public Page<MissaoRepository.RelatorioMissaoDTO> gerarRelatorioMetricas(OffsetDateTime inicio, OffsetDateTime termino, Pageable pageable) {
        return repository.gerarRelatorioMétricas(inicio, termino, pageable);
    }
}