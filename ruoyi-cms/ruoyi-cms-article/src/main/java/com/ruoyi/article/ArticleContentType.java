package com.ruoyi.article;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.article.domain.CmsArticleDetail;
import com.ruoyi.article.domain.dto.ArticleDTO;
import com.ruoyi.article.domain.vo.ArticleVO;
import com.ruoyi.article.mapper.CmsArticleDetailMapper;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IContent;
import com.ruoyi.contentcore.core.IContentType;
import com.ruoyi.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsPublishPipe;
import com.ruoyi.contentcore.domain.dto.PublishPipeProp;
import com.ruoyi.contentcore.domain.vo.ContentVO;
import com.ruoyi.contentcore.enums.ContentOpType;
import com.ruoyi.contentcore.fixed.dict.ContentAttribute;
import com.ruoyi.contentcore.mapper.CmsContentMapper;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IPublishPipeService;
import com.ruoyi.contentcore.util.InternalUrlUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component(IContentType.BEAN_NAME_PREFIX + ArticleContentType.ID)
@RequiredArgsConstructor
public class ArticleContentType implements IContentType {

    public final static String ID = "article";

    private final static String NAME = "{CMS.CONTENTCORE.CONTENT_TYPE." + ID + "}";

    private final CmsContentMapper contentMapper;

    private final CmsArticleDetailMapper articleMapper;

    private final ICatalogService catalogService;

    private final IPublishPipeService publishPipeService;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getComponent() {
        return "cms/article/editor";
    }

    @Override
    public IContent<?> newContent() {
        return new ArticleContent();
    }

    @Override
    public IContent<?> loadContent(CmsContent xContent) {
        ArticleContent articleContent = new ArticleContent();
        articleContent.setContentEntity(xContent);
        CmsArticleDetail articleDetail = this.articleMapper.selectById(xContent.getContentId());
        articleContent.setExtendEntity(articleDetail);
        return articleContent;
    }

    @Override
    public IContent<?> readRequest(HttpServletRequest request) throws IOException {
        ArticleDTO dto = JacksonUtils.from(request.getInputStream(), ArticleDTO.class);

        CmsContent contentEntity;
        if (dto.getOpType() == ContentOpType.UPDATE) {
            contentEntity = this.contentMapper.selectById(dto.getContentId());
            Assert.notNull(contentEntity,
                    () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", dto.getContentId()));
        } else {
            contentEntity = new CmsContent();
        }
        BeanUtils.copyProperties(dto, contentEntity);
        CmsCatalog catalog = this.catalogService.getCatalog(dto.getCatalogId());
        contentEntity.setSiteId(catalog.getSiteId());
        contentEntity.setAttributes(ContentAttribute.convertInt(dto.getAttributes()));
        // 发布通道配置
        Map<String, Map<String, Object>> publishPipProps = new HashMap<>();
        dto.getPublishPipeProps().forEach(prop -> {
            publishPipProps.put(prop.getPipeCode(), prop.getProps());
        });
        contentEntity.setPublishPipeProps(publishPipProps);

        CmsArticleDetail extendEntity = new CmsArticleDetail();
        BeanUtils.copyProperties(dto, extendEntity);

        ArticleContent content = new ArticleContent();
        content.setContentEntity(contentEntity);
        content.setExtendEntity(extendEntity);
        content.setParams(dto.getParams());
        if (content.hasExtendEntity() && StringUtils.isEmpty(extendEntity.getContentHtml())) {
            throw CommonErrorCode.NOT_EMPTY.exception("contentHtml");
        }
        return content;
    }

    @Override
    public ContentVO initEditor(Long catalogId, Long contentId) {
        CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
        Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalogId));
        List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(catalog.getSiteId());
        ArticleVO vo;
        if (IdUtils.validate(contentId)) {
            CmsContent contentEntity = this.contentMapper.selectById(contentId);
            Assert.notNull(contentEntity, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

            CmsArticleDetail extendEntity = this.articleMapper.selectById(contentId);
            vo = ArticleVO.newInstance(contentEntity, extendEntity);
            if (StringUtils.isNotEmpty(vo.getLogo())) {
                vo.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(vo.getLogo()));
            }
            // 发布通道模板数据
            List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(catalog.getSiteId(),
                    PublishPipePropUseType.Content, contentEntity.getPublishPipeProps());
            vo.setPublishPipeProps(publishPipeProps);
        } else {
            vo = new ArticleVO();
            vo.setContentId(IdUtils.getSnowflakeId());
            vo.setCatalogId(catalog.getCatalogId());
            vo.setContentType(ID);
            // 发布通道初始数据
            vo.setPublishPipe(publishPipes.stream().map(CmsPublishPipe::getCode).toArray(String[]::new));
            // 发布通道模板数据
            List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(catalog.getSiteId(),
                    PublishPipePropUseType.Content, null);
            vo.setPublishPipeProps(publishPipeProps);
        }
        vo.setCatalogName(catalog.getName());
        return vo;
    }

    @Override
    public void recover(Long contentId) {
        this.articleMapper.recoverById(contentId);
    }

    @Override
    public void deleteBackups(Long contentId) {
        this.articleMapper.deleteLogicDeletedById(contentId);
    }
}
