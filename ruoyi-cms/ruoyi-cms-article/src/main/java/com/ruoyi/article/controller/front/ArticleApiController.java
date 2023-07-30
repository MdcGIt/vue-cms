package com.ruoyi.article.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.article.domain.CmsArticleDetail;
import com.ruoyi.article.domain.vo.ArticleApiVO;
import com.ruoyi.article.service.IArticleService;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.vo.ContentApiVO;
import com.ruoyi.contentcore.domain.vo.ContentDynamicDataVO;
import com.ruoyi.contentcore.domain.vo.ContentVO;
import com.ruoyi.contentcore.fixed.dict.ContentAttribute;
import com.ruoyi.contentcore.fixed.dict.ContentStatus;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.impl.ContentDynamicDataService;
import com.ruoyi.contentcore.util.CatalogUtils;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 内容数据API接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cms/article")
public class ArticleApiController extends BaseRestController {

    private final ICatalogService catalogService;

    private final IContentService contentService;

    private final IArticleService articleService;

    @GetMapping("/list")
    public R<List<ArticleApiVO>> getContentList(
            @RequestParam("sid") Long siteId,
            @RequestParam(value = "cid", required = false, defaultValue = "0") Long catalogId,
            @RequestParam(value = "lv", required = false, defaultValue = "Root") String level,
            @RequestParam(value = "attrs", required = false) String hasAttributes,
            @RequestParam(value = "no_attrs", required = false) String noAttributes,
            @RequestParam(value = "st", required = false, defaultValue = "Recent") String sortType,
            @RequestParam(value = "ps", required = false, defaultValue = "16") Integer pageSize,
            @RequestParam(value = "pn", required = false, defaultValue = "1") Long pageNumber,
            @RequestParam(value = "pp") String publishPipeCode,
            @RequestParam(value = "preview", required = false, defaultValue = "false") Boolean preview,
            @RequestParam(value = "text", required = false, defaultValue = "false") Boolean text
    ) {
        if (!"Root".equalsIgnoreCase(level) && !IdUtils.validate(catalogId)) {
            return R.fail("The parameter cid is required where lv is `Root`.");
        }
        LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<>();
        q.eq(CmsContent::getSiteId, siteId).eq(CmsContent::getStatus, ContentStatus.PUBLISHED);
        if (!"Root".equalsIgnoreCase(level)) {
            CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
            if (Objects.isNull(catalog)) {
                return R.fail("Catalog not found: " + catalogId);
            }
            if ("Current".equalsIgnoreCase(level)) {
                q.eq(CmsContent::getCatalogId, catalog.getCatalogId());
            } else if ("Child".equalsIgnoreCase(level)) {
                q.likeRight(CmsContent::getCatalogAncestors, catalog.getAncestors() + CatalogUtils.ANCESTORS_SPLITER);
            } else if ("CurrentAndChild".equalsIgnoreCase(level)) {
                q.likeRight(CmsContent::getCatalogAncestors, catalog.getAncestors());
            }
        }
        if (StringUtils.isNotEmpty(hasAttributes)) {
            int attrTotal = ContentAttribute.convertInt(hasAttributes.split(","));
            q.apply(attrTotal > 0, "attributes&{0}={1}", attrTotal, attrTotal);
        }
        if (StringUtils.isNotEmpty(noAttributes)) {
            String[] contentAttrs = noAttributes.split(",");
            int attrTotal = ContentAttribute.convertInt(contentAttrs);
            for (String attr : contentAttrs) {
                int bit = ContentAttribute.bit(attr);
                q.apply(bit > 0, "attributes&{0}<>{1}", attrTotal, bit);
            }
        }
        if ("Recent".equalsIgnoreCase(sortType)) {
            q.orderByDesc(CmsContent::getPublishDate);
        } else {
            q.orderByDesc(Arrays.asList(CmsContent::getTopFlag, CmsContent::getSortFlag));
        }

        Page<CmsContent> pageResult = this.contentService.page(new Page<>(pageNumber, pageSize, false), q);
        if (pageResult.getRecords().isEmpty()) {
            return R.ok(List.of());
        }
        List<Long> contentIds = pageResult.getRecords().stream().map(CmsContent::getContentId).toList();
        Map<Long, CmsArticleDetail> articleDetails = this.articleService.listByIds(contentIds)
                .stream().collect(Collectors.toMap(c -> c.getContentId(), c -> c));

        Map<Long, CmsCatalog> loadedCatalogs = new HashMap<>();
        List<ArticleApiVO> list = new ArrayList<>();
        pageResult.getRecords().forEach(c -> {
            ArticleApiVO dto = ArticleApiVO.newInstance(c);
            CmsCatalog catalog = loadedCatalogs.get(c.getCatalogId());
            if (Objects.isNull(catalog)) {
                catalog = this.catalogService.getCatalog(c.getCatalogId());
                loadedCatalogs.put(catalog.getCatalogId(), catalog);
            }
            dto.setCatalogName(catalog.getName());
            dto.setCatalogLink(catalogService.getCatalogLink(catalog, 1, publishPipeCode, preview));
            dto.setLink(this.contentService.getContentLink(c, 1, publishPipeCode, preview));
            dto.setLogoSrc(InternalUrlUtils.getActualUrl(c.getLogo(), publishPipeCode, preview));
            CmsArticleDetail articleDetail = articleDetails.get(c.getContentId());
            if (Objects.nonNull(articleDetail)) {
                 dto.setContentHtml(articleDetail.getContentHtml());
            }
            list.add(dto);
        });
        return R.ok(list);
    }

    /**
     * 按浏览量排序获取数据
     */
    public R<ContentVO> getHotContentList() {

        return R.ok();
    }
}
