package com.guilda.registro.service;

import com.guilda.registro.domain.aventura.Aventureiro;
import com.guilda.registro.domain.aventura.Missao;
import com.guilda.registro.domain.aventura.ParticipacaoId;
import com.guilda.registro.domain.aventura.ParticipacaoMissao;
import com.guilda.registro.domain.enums.PapelMissao;
import com.guilda.registro.domain.enums.StatusMissao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;


@Service
public class ParticipacaoMissaoService {

    @Transactional
    public ParticipacaoMissao registrarParticipacao(Missao missao, Aventureiro aventureiro, PapelMissao papel) {

        if (Boolean.FALSE.equals(aventureiro.getAtivo())) {
            throw new IllegalArgumentException("Aventureiros inativos não podem participar de missões.");
        }

        if (missao.getStatus() == StatusMissao.CONCLUIDA || missao.getStatus() == StatusMissao.CANCELADA) {
            throw new IllegalArgumentException("A missão não está em um status válido para aceitar participantes.");
        }
        if (!missao.getOrganizacao().getId().equals(aventureiro.getOrganizacao().getId())) {
            throw new IllegalArgumentException("O aventureiro não pertence à organização desta missão.");
        }

        ParticipacaoMissao participacao = new ParticipacaoMissao();
        participacao.setId(new ParticipacaoId(missao.getId(), aventureiro.getId()));
        participacao.setMissao(missao);
        participacao.setAventureiro(aventureiro);
        participacao.setPapelMissao(papel);
        participacao.setDestaque(false);
        participacao.setRecompensaOuro(BigDecimal.ZERO);

        return participacao;
    }
}