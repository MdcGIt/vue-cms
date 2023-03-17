package com.ruoyi.cms.search.service;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.cms.search.es.doc.ESContent;
import com.ruoyi.cms.search.es.repository.ESContentRepository;
import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IContent;
import com.ruoyi.contentcore.core.IContentType;
import com.ruoyi.contentcore.core.impl.InternalDataType_Content;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.enums.ContentCopyType;
import com.ruoyi.contentcore.fixed.dict.ContentStatus;
import com.ruoyi.contentcore.properties.EnableIndexProperty;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.util.ContentCoreUtils;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.system.fixed.dict.YesOrNo;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ContentIndexService {

	private final ICatalogService catalogService;

	private final IContentService contentService;

	private final ESContentRepository contentRepository;

	private final AsyncTaskManager asyncTaskManager;

	private final ElasticsearchClient esClient;

	/**
	 * 创建内容索引
	 */
	public void createContentIndex(IContent<?> content) {
		// 判断栏目/站点配置是否生成索引
		String enableIndex = EnableIndexProperty.getValue(content.getCatalog().getConfigProps(),
				content.getSite().getConfigProps());
		if (YesOrNo.isNo(enableIndex)) {
			return;
		}
		this.contentRepository.save(newESContent(content));
	}

	private ESContent newESContent(IContent<?> content) {
		ESContent esContent = new ESContent();
		esContent.setContentId(content.getContentEntity().getContentId());
		esContent.setContentType(content.getContentEntity().getContentType());
		esContent.setSiteId(content.getContentEntity().getSiteId());
		esContent.setCatalogId(content.getContentEntity().getCatalogId());
		esContent.setCatalogAncestors(content.getContentEntity().getCatalogAncestors());
		esContent.setAuthor(content.getContentEntity().getAuthor());
		esContent.setEditor(content.getContentEntity().getEditor());
		esContent.setKeywords(StringUtils.join(content.getContentEntity().getKeywords()));
		esContent.setTags(StringUtils.join(content.getContentEntity().getTags()));
		esContent.setCreateTime(content.getContentEntity().getCreateTime().toEpochSecond(ZoneOffset.UTC));
		esContent.setLogo(content.getContentEntity().getLogo());
		esContent.setStatus(content.getContentEntity().getStatus());
		esContent.setPublishDate(content.getContentEntity().getPublishDate().toEpochSecond(ZoneOffset.UTC));
		esContent.setLink(InternalUrlUtils.getInternalUrl(InternalDataType_Content.ID, esContent.getContentId()));
		esContent.setTitle(content.getContentEntity().getTitle());
		esContent.setFullText(content.getFullText());
		return esContent;
	}

	/**
	 * 删除内容索引
	 * @throws IOException 
	 * @throws ElasticsearchException 
	 */
	public void deleteContentIndex(List<Long> contentIds) throws ElasticsearchException, IOException {
		List<BulkOperation> bulkOperationList = contentIds.stream().map(contentId -> 
			BulkOperation.of(b -> 
				b.delete(dq -> 
					dq.index(ESContent.INDEX_NAME).id(contentId.toString())
				)
			)
		).toList();
		this.esClient.bulk(bulk -> bulk.operations(bulkOperationList));
	}

	/**
	 * 重建指定站点所有内容索引
	 * 
	 * @return
	 */
	public AsyncTask rebuildAllIndex(CmsSite site) {
		AsyncTask asyncTask = new AsyncTask() {

			@Override
			public void run0() throws Exception {
				int pageSize = 200;
				LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
						.eq(CmsContent::getSiteId, site.getSiteId())
						.ne(CmsContent::getCopyType, ContentCopyType.Mapping.value())
						.eq(CmsContent::getStatus, ContentStatus.PUBLISHED);
				long total = contentService.count(q);
				int count = 1;
				for (int i = 0; i * pageSize < total; i++) {
					List<BulkOperation> bulkOperationList = new ArrayList<>(pageSize);
					Page<CmsContent> page = contentService.page(new Page<>(i, pageSize, false), q);
					for (CmsContent xContent : page.getRecords()) {
						// 判断栏目/站点配置是否生成索引
						String enableIndex = EnableIndexProperty.getValue(
								catalogService.getCatalog(xContent.getCatalogId()).getConfigProps(),
								site.getConfigProps());
						if (YesOrNo.isYes(enableIndex)) {
							IContentType contentType = ContentCoreUtils.getContentType(xContent.getContentType());
							IContent<?> icontent = contentType.loadContent(xContent);
							this.setPercent((int) (count++ * 100 / total));
							BulkOperation bulkOperation = BulkOperation
									.of(b -> b.create(co -> co.index(ESContent.INDEX_NAME)
											.id(xContent.getContentId().toString()).document(newESContent(icontent))));
							bulkOperationList.add(bulkOperation);
						}
						this.checkInterrupt(); // 允许中断
					}
					// 批量新增索引
					try {
						esClient.bulk(bulk -> bulk.operations(bulkOperationList));
					} catch (ElasticsearchException | IOException e) {
						this.addErrorMessage(e.getMessage());
						e.printStackTrace();
					}
				}
				this.setProgressInfo(100, "重建全站索引完成");
			}
		};
		asyncTask.setTaskId("RebuildAllContentIndex");
		asyncTask.setType("ContentCore");
		this.asyncTaskManager.execute(asyncTask);
		return asyncTask;
	}
}
