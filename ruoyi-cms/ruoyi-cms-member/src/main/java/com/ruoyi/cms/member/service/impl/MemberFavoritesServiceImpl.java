package com.ruoyi.cms.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.cms.member.domain.CmsMemberFavorites;
import com.ruoyi.cms.member.exception.CmsMemberErrorCode;
import com.ruoyi.cms.member.mapper.CmsMemberFavoritesMapper;
import com.ruoyi.cms.member.service.IMemberFavoritesService;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.impl.ContentDynamicDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 会员内容收藏服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class MemberFavoritesServiceImpl extends ServiceImpl<CmsMemberFavoritesMapper, CmsMemberFavorites>
        implements IMemberFavoritesService {

    private final IContentService contentService;

    private final ContentDynamicDataService contentDynamicDataService;

    @Override
    public void deleteMemberFavorites(List<Long> dataIds) {
        List<CmsMemberFavorites> list = this.listByIds(dataIds);
        this.removeByIds(list);
        list.forEach(fav -> contentDynamicDataService.decreaseFavoriteCount(fav.getContentId()));
    }

    @Override
    public void favoriteContent(Long memberId, Long contentId) {
        Long count = this.lambdaQuery()
                .eq(CmsMemberFavorites::getMemberId, memberId)
                .eq(CmsMemberFavorites::getContentId, contentId)
                .count();
        Assert.isTrue(count == 0, CmsMemberErrorCode.FAVORITED::exception);

        CmsContent content = this.contentService.getById(contentId);
        Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

        CmsMemberFavorites favorites = new CmsMemberFavorites();
        favorites.setLogId(IdUtils.getSnowflakeId());
        favorites.setSiteId(content.getSiteId());
        favorites.setMemberId(memberId);
        favorites.setContentId(contentId);
        favorites.setCreateTime(LocalDateTime.now());
        this.save(favorites);
        // 内容收藏数+1
        this.contentDynamicDataService.increaseFavoriteCount(contentId);
    }

    @Override
    public void cancelFavoriteContent(Long memberId, Long contentId) {
        Optional<CmsMemberFavorites> opt = this.lambdaQuery()
                .eq(CmsMemberFavorites::getMemberId, memberId)
                .eq(CmsMemberFavorites::getContentId, contentId)
                .oneOpt();
        if (opt.isEmpty()) {
            return;
        }
        this.removeById(opt.get());
        // 内容收藏数-1
        this.contentDynamicDataService.decreaseFavoriteCount(contentId);
    }
}
