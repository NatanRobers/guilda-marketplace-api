package com.guilda.registro.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.guilda.registro.dto.ProdutoDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MarketplaceService {

    private final ElasticsearchClient esClient;
    private final String INDEX = "guilda_loja";

    public MarketplaceService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }


    public List<ProdutoDTO> buscaPorNome(String termo) throws IOException {
        return search(s -> s.query(q -> q.match(m -> m.field("nome").query(termo))));
    }

    public List<ProdutoDTO> buscaPorDescricao(String termo) throws IOException {
        return search(s -> s.query(q -> q.match(m -> m.field("descricao").query(termo))));
    }

    public List<ProdutoDTO> buscaFuzzyNome(String termo) throws IOException {
        return search(s -> s.query(q -> q.fuzzy(f -> f.field("nome").value(termo))));
    }

    public List<ProdutoDTO> buscaFraseExataDescricao(String frase) throws IOException {
        return search(s -> s.query(q -> q.matchPhrase(m -> m.field("descricao").query(frase))));
    }

    public List<ProdutoDTO> buscaMultiCampos(String termo) throws IOException {
        return search(s -> s.query(q -> q.multiMatch(m -> m.fields("nome", "descricao").query(termo))));
    }

    public List<ProdutoDTO> buscaComFiltroCategoria(String termo, String categoria) throws IOException {
        return search(s -> s.query(q -> q.bool(b -> b
                .must(m -> m.match(t -> t.field("descricao").query(termo)))
                .filter(f -> f.term(t -> t.field("categoria.keyword").value(categoria)))
        )));
    }

    public List<ProdutoDTO> buscaFaixaPreco(Double min, Double max) throws IOException {
        return search(s -> s.query(q -> q.range(r -> r.field("preco")
                .gte(co.elastic.clients.json.JsonData.of(min))
                .lte(co.elastic.clients.json.JsonData.of(max))
        )));
    }

    public List<ProdutoDTO> buscaAvancada(String categoria, String raridade, Double min, Double max) throws IOException {
        return search(s -> s.query(q -> q.bool(b -> b
                .must(m -> m.match(t -> t.field("categoria").query(categoria)))
                .filter(f -> f.term(t -> t.field("raridade.keyword").value(raridade)))
                .filter(f -> f.range(r -> r.field("preco")
                        .gte(co.elastic.clients.json.JsonData.of(min))
                        .lte(co.elastic.clients.json.JsonData.of(max))))
        )));
    }

    public SearchResponse<Void> agregarPorCategoria() throws IOException {
        return esClient.search(s -> s.index(INDEX).size(0)
                .aggregations("por_categoria", a -> a.terms(t -> t.field("categoria.keyword"))), Void.class);
    }

    public SearchResponse<Void> agregarPorRaridade() throws IOException {
        return esClient.search(s -> s.index(INDEX).size(0)
                .aggregations("por_raridade", a -> a.terms(t -> t.field("raridade.keyword"))), Void.class);
    }

    public SearchResponse<Void> agregarPrecoMedio() throws IOException {
        return esClient.search(s -> s.index(INDEX).size(0)
                .aggregations("preco_medio", a -> a.avg(avg -> avg.field("preco"))), Void.class);
    }

    public SearchResponse<Void> agregarFaixasPreco() throws IOException {
        return esClient.search(s -> s
                        .index(INDEX)
                        .size(0)
                        .aggregations("faixas_preco", a -> a
                                .range(r -> r
                                        .field("preco")
                                        .ranges(
                                                co.elastic.clients.elasticsearch._types.aggregations.AggregationRange.of(rg -> rg.to(String.valueOf(100.0))),
                                                co.elastic.clients.elasticsearch._types.aggregations.AggregationRange.of(rg -> rg.from(String.valueOf(100.0)).to(String.valueOf(300.0))),
                                                co.elastic.clients.elasticsearch._types.aggregations.AggregationRange.of(rg -> rg.from(String.valueOf(300.0)).to(String.valueOf(700.0))),
                                                co.elastic.clients.elasticsearch._types.aggregations.AggregationRange.of(rg -> rg.from(String.valueOf(700.0)))
                                        )
                                )
                        ),
                Void.class
        );
    }

    private List<ProdutoDTO> search(java.util.function.Function<co.elastic.clients.elasticsearch.core.SearchRequest.Builder, co.elastic.clients.elasticsearch.core.SearchRequest.Builder> fn) throws IOException {
        SearchResponse<ProdutoDTO> res = esClient.search(s -> fn.apply(s.index(INDEX)), ProdutoDTO.class);
        return res.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }
}