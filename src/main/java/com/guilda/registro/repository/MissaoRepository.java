package com.guilda.registro.repository;

import com.guilda.registro.domain.aventura.Missao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface MissaoRepository extends JpaRepository<Missao, Long>, JpaSpecificationExecutor<Missao> {

    // JPQL gigante de filtros removido! Usaremos Specification.

    interface RelatorioMissaoDTO {
        String getTitulo();
        String getStatus();
        String getNivelPerigo();
        Long getQuantidadeParticipantes();
        Double getTotalRecompensas();
    }

    @Query("SELECT m.titulo as titulo, " +
            "CAST(m.status AS string) as status, " +
            "CAST(m.nivelPerigo AS string) as nivelPerigo, " +
            "COUNT(p.aventureiro) as quantidadeParticipantes, " +
            "COALESCE(SUM(p.recompensaOuro), 0) as totalRecompensas " +
            "FROM Missao m LEFT JOIN ParticipacaoMissao p ON m.id = p.missao.id " +
            "WHERE (cast(:inicio as timestamp) IS NULL OR m.dataInicio >= :inicio) AND " +
            "(cast(:termino as timestamp) IS NULL OR m.dataTermino <= :termino) " +
            "GROUP BY m.id, m.titulo, m.status, m.nivelPerigo")
    Page<RelatorioMissaoDTO> gerarRelatorioMétricas(
            @Param("inicio") OffsetDateTime inicio,
            @Param("termino") OffsetDateTime termino,
            Pageable pageable);
}