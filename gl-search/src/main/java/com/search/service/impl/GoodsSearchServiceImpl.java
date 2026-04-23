package com.search.service.impl;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.search.document.GoodsSearchDocument;
import com.search.dto.GoodsIndexRequest;
import com.search.dto.GoodsSearchItemVo;
import com.search.dto.GoodsSearchPageVo;
import com.search.repository.GoodsSearchRepository;
import com.search.service.GoodsSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodsSearchServiceImpl implements GoodsSearchService {

    private final GoodsSearchRepository goodsSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void indexGoods(GoodsIndexRequest request) {
        GoodsSearchDocument doc = new GoodsSearchDocument();
        BeanUtils.copyProperties(request, doc);
        doc.setPrice(request.getPrice() != null ? request.getPrice().doubleValue() : null);
        goodsSearchRepository.save(doc);
    }

    @Override
    public void removeGoods(String goodsId) {
        goodsSearchRepository.deleteById(goodsId);
    }

    @Override
    public GoodsSearchPageVo search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Query query = buildQuery(keyword);
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withPageable(pageable)
                .build();
        SearchHits<GoodsSearchDocument> hits = elasticsearchOperations.search(nativeQuery, GoodsSearchDocument.class);
        List<GoodsSearchItemVo> items = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toVo)
                .collect(Collectors.toList());
        return new GoodsSearchPageVo(hits.getTotalHits(), pageable.getPageNumber(), pageable.getPageSize(), items);
    }

    private Query buildQuery(String keyword) {
        Query onShelf = Query.of(q -> q.term(t -> t.field("status").value(FieldValue.of(1))));
        if (!StringUtils.hasText(keyword)) {
            return Query.of(q -> q.bool(b -> b.must(m -> m.matchAll(ma -> ma)).filter(onShelf)));
        }
        String keywordText = keyword.trim();
        Query text = Query.of(q -> q.multiMatch(m -> m
                .query(keywordText)
                .fields("skuName^2", "description", "type")));
        return Query.of(q -> q.bool(b -> b.must(text).filter(onShelf)));
    }

    private GoodsSearchItemVo toVo(GoodsSearchDocument goodsSearchDocument) {
        return new GoodsSearchItemVo(
                goodsSearchDocument.getGoodsId(),
                goodsSearchDocument.getMerchantId(),
                goodsSearchDocument.getSkuName(),
                goodsSearchDocument.getPicture(),
                goodsSearchDocument.getCategory(),
                goodsSearchDocument.getType(),
                goodsSearchDocument.getDescription(),
                goodsSearchDocument.getPrice(),
                goodsSearchDocument.getStatus());
    }
}
