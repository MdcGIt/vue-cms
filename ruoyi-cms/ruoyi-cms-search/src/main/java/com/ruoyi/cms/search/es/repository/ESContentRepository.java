package com.ruoyi.cms.search.es.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.ruoyi.cms.search.es.doc.ESContent;

public interface ESContentRepository extends ElasticsearchRepository<ESContent, Long> {
	
}
