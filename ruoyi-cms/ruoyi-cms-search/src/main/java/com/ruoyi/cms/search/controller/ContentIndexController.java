package com.ruoyi.cms.search.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import co.elastic.clients.elasticsearch.core.InfoResponse;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.search.SearchConsts;
import com.ruoyi.search.exception.SearchErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.cms.search.es.doc.ESContent;
import com.ruoyi.cms.search.permission.CmsSearchPriv;
import com.ruoyi.cms.search.service.ContentIndexService;
import com.ruoyi.cms.search.vo.ESContentVO;
import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IContent;
import com.ruoyi.contentcore.core.IContentType;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.ContentCoreUtils;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.validator.LongId;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@Priv(type = AdminUserType.TYPE, value = CmsSearchPriv.ContentIndexView)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/search")
public class ContentIndexController extends BaseRestController {

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IContentService contentService;

	private final ContentIndexService searchService;

	private final ElasticsearchClient esClient;

	private void checkElasticSearchEnabled() throws IOException {
		Assert.isTrue(this.searchService.isElasticSearchAvailable(), SearchErrorCode.ESConnectFail::exception);
	}

	@GetMapping("/contents")
	public R<?> selectDocumentList(@RequestParam(value = "query", required = false) String query,
								   @RequestParam(value = "onlyTitle", required = false ,defaultValue = "false") Boolean onlyTitle,
								   @RequestParam(value = "contentType", required = false) String contentType) throws ElasticsearchException, IOException {
		this.checkElasticSearchEnabled();
		PageRequest pr = this.getPageRequest();

		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		SearchResponse<ObjectNode> sr = esClient.search(s -> {
			s.index(ESContent.INDEX_NAME) // 索引
					.query(q ->
							q.bool(b -> {
								b.must(must -> must.term(tq -> tq.field("siteId").value(site.getSiteId())));
								if (StringUtils.isNotEmpty(contentType)) {
									b.must(must -> must.term(tq -> tq.field("contentType").value(contentType)));
								}
								if (StringUtils.isNotEmpty(query)) {
									if (onlyTitle) {
										b.must(must -> must
												.match(match -> match
														.analyzer(SearchConsts.IKAnalyzeType_Smart)
														.field("title")
														.query(query)
												)
										);
									} else {
										b.must(must -> must
												.multiMatch(match -> match
														.analyzer(SearchConsts.IKAnalyzeType_Smart)
														.fields("title^10", "fullText^1")
														.query(query)
												)
										);
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
			s.from((pr.getPageNumber() - 1) * pr.getPageSize()).size(pr.getPageSize());  // 分页
			return s;
		}, ObjectNode.class);
		List<ESContentVO> list = sr.hits().hits().stream().map(hit -> {
			ObjectNode source = hit.source();
			ESContentVO vo = JacksonUtils.getObjectMapper().convertValue(source, ESContentVO.class);
			vo.setHitScore(hit.score());
			vo.setPublishDateInstance(LocalDateTime.ofEpochSecond(vo.getPublishDate(), 0, ZoneOffset.UTC));
			vo.setCreateTimeInstance(LocalDateTime.ofEpochSecond(vo.getCreateTime(), 0, ZoneOffset.UTC));
			CmsCatalog catalog = this.catalogService.getCatalog(vo.getCatalogId());
			if (Objects.nonNull(catalog)) {
				vo.setCatalogName(catalog.getName());
			}
			hit.highlight().entrySet().forEach(e -> {
				if (e.getKey().equals("fullText")) {
					vo.setFullText(StringUtils.join(e.getValue().toArray(String[]::new)));
				} else if (e.getKey().equals("title")) {
					vo.setTitle(StringUtils.join(e.getValue().toArray(String[]::new)));
				}
			});
			return vo;
		}).toList();
		return this.bindDataTable(list, sr.hits().total().value());
	}

	@GetMapping("/content/{contentId}")
	public R<?> selectDocumentDetail(@PathVariable(value = "contentId") @LongId Long contentId) throws ElasticsearchException, IOException {
		this.checkElasticSearchEnabled();
		ESContent source = this.searchService.getContentDocDetail(contentId);
		return R.ok(source);
	}

	@Log(title = "删除索引", businessType = BusinessType.DELETE)
	@DeleteMapping("/contents")
	public R<?> deleteDocuments(@RequestBody @NotEmpty List<Long> contentIds) throws ElasticsearchException, IOException {
		this.checkElasticSearchEnabled();
		this.searchService.deleteContentDoc(contentIds);
		return R.ok();
	}

	@Log(title = "重建内容索引", businessType = BusinessType.UPDATE)
	@PostMapping("/build/{contentId}")
	public R<?> buildContentIndex(@PathVariable("contentId") @LongId Long contentId) throws IOException {
		this.checkElasticSearchEnabled();
		CmsContent content = this.contentService.getById(contentId);
		Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

		IContentType ct = ContentCoreUtils.getContentType(content.getContentType());
		IContent<?> icontent = ct.loadContent(content);
		this.searchService.createContentDoc(icontent);
		return R.ok();
	}

	@Log(title = "重建全站索引", businessType = BusinessType.UPDATE)
	@PostMapping("/rebuild")
	public R<?> rebuildAllIndex() throws IOException {
		this.checkElasticSearchEnabled();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		AsyncTask task = this.searchService.rebuildAll(site);
		return R.ok(task.getTaskId());
	}
}
