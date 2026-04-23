package com.search.repository;

import com.search.document.GoodsSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GoodsSearchRepository extends ElasticsearchRepository<GoodsSearchDocument, String> {
}
