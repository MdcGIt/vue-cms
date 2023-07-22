package com.ruoyi.contentcore.service.impl;

import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.vo.ContentDynamicDataVO;
import com.ruoyi.contentcore.service.IContentService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 内容动态数据服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentDynamicDataService {

    /**
     * 内容浏览次数缓存KEY
     */
    private static final String CONTENT_DYNAMIC_DATA_CACHE = "cms:content:dynamic";

    private final IContentService contentService;

    private final RedisCache redisCache;

    private final RedissonClient redissonClient;

    private static final ConcurrentHashMap<Long, ContentDynamicDataVO> dynamicUpdates = new ConcurrentHashMap<>();

    public void increaseFavoriteCount(Long contentId) {
        this.updateContentDynamicData(contentId, ContentDynamicDataVO.DynamicDataType.Favorite, true);
    }

    public void decreaseFavoriteCount(Long contentId) {
        this.updateContentDynamicData(contentId, ContentDynamicDataVO.DynamicDataType.Favorite, false);
    }

    public void increaseCommentCount(Long contentId) {
        this.updateContentDynamicData(contentId, ContentDynamicDataVO.DynamicDataType.Comment, true);
    }

    public void decreaseCommentCount(Long contentId) {
        this.updateContentDynamicData(contentId, ContentDynamicDataVO.DynamicDataType.Comment, false);
    }

    public void increaseLikeCount(Long contentId) {
        this.updateContentDynamicData(contentId, ContentDynamicDataVO.DynamicDataType.Like, true);
    }

    public void decreaseLikeCount(Long contentId) {
        this.updateContentDynamicData(contentId, ContentDynamicDataVO.DynamicDataType.Like, false);
    }

    public void increaseViewCount(Long contentId) {
        this.updateContentDynamicData(contentId, ContentDynamicDataVO.DynamicDataType.View, true);
    }

    public ContentDynamicDataVO getContentDynamicData(Long contentId) {
        ContentDynamicDataVO data = this.redisCache.getCacheMapValue(CONTENT_DYNAMIC_DATA_CACHE, contentId.toString());
        if (data == null) {
            RLock lock = redissonClient.getLock("cms:content:dynamic:" + contentId);
            lock.lock();
            try {
                data = this.redisCache.getCacheMapValue(CONTENT_DYNAMIC_DATA_CACHE, contentId.toString());
                if (data == null) {
                    CmsContent content = this.contentService.getById(contentId);
                    if (content == null) {
                        return null;
                    }
                    data = new ContentDynamicDataVO(content);
                    this.redisCache.setCacheMapValue(CONTENT_DYNAMIC_DATA_CACHE, contentId.toString(), data);
                }
            } finally {
                lock.unlock();
            }
        }
        return data;
    }

    /**
     * 更新内容动态数据缓存
     *
     * @param contentId
     * @param type
     * @return
     */
    private void updateContentDynamicData(Long contentId, ContentDynamicDataVO.DynamicDataType type, boolean increase) {
        if (!IdUtils.validate(contentId)) {
            return;
        }
        ContentDynamicDataVO data = this.getContentDynamicData(contentId);
        if (increase) {
            data.increase(type);
        } else {
            data.decrease(type);
        }
        this.redisCache.setCacheMapValue(CONTENT_DYNAMIC_DATA_CACHE, contentId.toString(), data);
        this.dynamicUpdates.put(contentId, data);
    }

    /**
     * 每5分钟更新记录的动态数据缓存到数据库
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 300000)
    public void saveDynamicDataToDB() {
        this.dynamicUpdates.keys().asIterator().forEachRemaining(contentId -> {
            ContentDynamicDataVO data = this.dynamicUpdates.remove(contentId);
            if (data != null) {
                this.contentService.lambdaUpdate().set(CmsContent::getFavoriteCount, data.getFavorites())
                        .set(CmsContent::getLikeCount, data.getLikes())
                        .set(CmsContent::getCommentCount, data.getComments())
                        .set(CmsContent::getViewCount, data.getViews())
                        .eq(CmsContent::getContentId, data.getContentId())
                        .update();
            }
        });
    }

    @PreDestroy
    public void preDestroy() {
        log.info("Update content dynamic data to database.");
        this.saveDynamicDataToDB();
    }
}
