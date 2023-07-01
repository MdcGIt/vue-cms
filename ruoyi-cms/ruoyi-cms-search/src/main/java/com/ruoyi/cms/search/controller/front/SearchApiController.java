package com.ruoyi.cms.search.controller.front;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.ruoyi.cms.search.es.doc.ESContent;
import com.ruoyi.cms.search.vo.ESContentVO;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.search.service.ISearchLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cms/search")
public class SearchApiController extends BaseRestController {

	private final ICatalogService catalogService;

	private final ElasticsearchClient esClient;

	private final ISearchLogService logService;

	@GetMapping("/query")
	public R<?> selectDocumentList(
			@RequestParam(value = "sid") Long siteId,
			@RequestParam(value = "pp") String publishPipeCode,
			@RequestParam(value = "q") @Length(max = 50) String query,
			@RequestParam(value = "ot", required = false ,defaultValue = "false") Boolean onlyTitle,
			@RequestParam(value = "ct", required = false) String contentType,
			@RequestParam(value = "page", required = false, defaultValue = "0") Integer page) throws ElasticsearchException, IOException {
		int pageSize = 10;
		SearchResponse<ESContentVO> sr = esClient.search(s -> {
			s.index(ESContent.INDEX_NAME) // 索引
				.query(q -> 
					q.bool(b -> {
						b.must(must -> must.term(tq -> tq.field("siteId").value(siteId)));
						if (StringUtils.isNotEmpty(contentType)) {
							b.must(must -> must.term(tq -> tq.field("contentType").value(contentType)));
						}
						if (StringUtils.isNotEmpty(query)) {
							if (onlyTitle) {
								b.must(must -> must.simpleQueryString(sqs -> sqs.fields("title").query(query)));
							} else {
								b.must(must -> must.simpleQueryString(sqs -> sqs.fields("title^10", "fullText^1").query(query))); // title权重更高
							}
						}	
						return b;
					})
				);
			if (StringUtils.isNotEmpty(query)) {
				s.highlight(h ->  
					h.fields("title", f -> f.preTags("<font color='red'>").postTags("</font>"))
						.fields("fullText", f -> f.preTags("<font color='red'>").postTags("</font>")));
			}
			s.sort(sort -> sort.field(f -> f.field("_score").order(SortOrder.Desc)));
			s.sort(sort -> sort.field(f -> f.field("publishDate").order(SortOrder.Desc))); // 排序: _score:desc + publishDate:desc
//			s.source(source -> source.filter(f -> f.excludes("fullText"))); // 过滤字段
			s.from(page * pageSize).size(pageSize);  // 分页
			return s;
		}, ESContentVO.class);
		List<ESContentVO> list = sr.hits().hits().stream().map(hit -> {
			ESContentVO vo = hit.source();
			vo.set_publishDate(LocalDateTime.ofEpochSecond(vo.getPublishDate(), 0, ZoneOffset.UTC));
			vo.set_createTime(LocalDateTime.ofEpochSecond(vo.getCreateTime(), 0, ZoneOffset.UTC));
			CmsCatalog catalog = this.catalogService.getCatalog(vo.getCatalogId());
			if (Objects.nonNull(catalog)) {
				vo.setCatalogName(catalog.getName());
			}
			hit.highlight().entrySet().forEach(e -> {
				try {
					if (e.getKey().equals("fullText")) {
						vo.setFullText(StringUtils.join(e.getValue().toArray(String[]::new)));
					} else if (e.getKey().equals("title")) {
						vo.setTitle(StringUtils.join(e.getValue().toArray(String[]::new)));
					}
					vo.setLink(InternalUrlUtils.getActualUrl(vo.getLink(), publishPipeCode, false));
					vo.setLogo(InternalUrlUtils.getActualUrl(vo.getLogo(), publishPipeCode, false));
				} catch(Exception ex) {
					log.warn("Search api row parse failed: ", ex);
				}
			});
			return vo;
		}).toList();
		// 记录搜索日志
		this.logService.addSearchLog("site:" + siteId, query, ServletUtils.getRequest());
		return this.bindDataTable(list, sr.hits().total().value());
	}
}
