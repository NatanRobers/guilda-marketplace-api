package com.guilda.registro.controller;

import com.guilda.registro.domain.aventura.PainelTaticoMissao;
import com.guilda.registro.service.PainelTaticoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/missoes")
public class PainelTaticoController {

    private final PainelTaticoService service;

    public PainelTaticoController(PainelTaticoService service) {
        this.service = service;
    }

    @GetMapping("/top15dias")
    public List<PainelTaticoMissao> getTop15Dias() {
        return service.buscarTopMissoesRecentes();
    }
}