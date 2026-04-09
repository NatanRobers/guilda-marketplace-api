package com.guilda.registro.controller;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.guilda.registro.dto.ProdutoDTO;
import com.guilda.registro.service.MarketplaceService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/produtos")
public class MarketplaceController {

    private final MarketplaceService service;

    public MarketplaceController(MarketplaceService service) {
        this.service = service;
    }

    @GetMapping("/busca/nome")
    public List<ProdutoDTO> buscaPorNome(@RequestParam String termo) throws IOException {
        return service.buscaPorNome(termo);
    }

    @GetMapping("/busca/descricao")
    public List<ProdutoDTO> buscaPorDescricao(@RequestParam String termo) throws IOException {
        return service.buscaPorDescricao(termo);
    }

    @GetMapping("/busca/frase")
    public List<ProdutoDTO> buscaFraseExata(@RequestParam String termo) throws IOException {
        return service.buscaFraseExataDescricao(termo);
    }

    @GetMapping("/busca/fuzzy")
    public List<ProdutoDTO> buscaFuzzy(@RequestParam String termo) throws IOException {
        return service.buscaFuzzyNome(termo);
    }

    @GetMapping("/busca/multicampos")
    public List<ProdutoDTO> buscaMultiCampos(@RequestParam String termo) throws IOException {
        return service.buscaMultiCampos(termo);
    }

    @GetMapping("/busca/com-filtro")
    public List<ProdutoDTO> buscaComFiltro(
            @RequestParam String termo,
            @RequestParam String categoria) throws IOException {
        return service.buscaComFiltroCategoria(termo, categoria);
    }

    @GetMapping("/busca/faixa-preco")
    public List<ProdutoDTO> buscaFaixaPreco(
            @RequestParam Double min,
            @RequestParam Double max) throws IOException {
        return service.buscaFaixaPreco(min, max);
    }

    @GetMapping("/busca/avancada")
    public List<ProdutoDTO> buscaAvancada(
            @RequestParam String categoria,
            @RequestParam String raridade,
            @RequestParam Double min,
            @RequestParam Double max) throws IOException {
        return service.buscaAvancada(categoria, raridade, min, max);
    }

    @GetMapping("/agregacoes/por-categoria")
    public SearchResponse<Void> agregarPorCategoria() throws IOException {
        return service.agregarPorCategoria();
    }

    @GetMapping("/agregacoes/por-raridade")
    public SearchResponse<Void> agregarPorRaridade() throws IOException {
        return service.agregarPorRaridade();
    }

    @GetMapping("/agregacoes/preco-medio")
    public SearchResponse<Void> agregarPrecoMedio() throws IOException {
        return service.agregarPrecoMedio();
    }

    @GetMapping("/agregacoes/faixas-preco")
    public SearchResponse<Void> agregarFaixasPreco() throws IOException {
        return service.agregarFaixasPreco();
    }
}