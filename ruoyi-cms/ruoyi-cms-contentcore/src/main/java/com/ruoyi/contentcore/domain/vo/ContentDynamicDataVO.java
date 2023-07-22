package com.ruoyi.contentcore.domain.vo;

import com.ruoyi.contentcore.domain.CmsContent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContentDynamicDataVO {

    private long contentId;

    private long favorites;

    private long likes;

    private long comments;

    private long views;

    private boolean updated;

    public ContentDynamicDataVO(CmsContent content) {
        this.contentId = content.getContentId();
        this.favorites = content.getFavoriteCount();
        this.likes = content.getLikeCount();
        this.comments = content.getCommentCount();
        this.views = content.getViewCount();
    }

    public void increase(DynamicDataType type) {
        switch(type) {
            case Favorite -> this.favorites++;
            case Like -> this.likes++;
            case Comment -> this.comments++;
            case View -> this.views++;
        }
        this.updated = true;
    }

    public void decrease(DynamicDataType type) {
        switch(type) {
            case Favorite -> this.favorites--;
            case Like -> this.likes--;
            case Comment -> this.comments--;
            case View -> this.views--;
        }
        this.updated = true;
    }

    public enum DynamicDataType {
        Favorite, Like, Comment, View
    }
}