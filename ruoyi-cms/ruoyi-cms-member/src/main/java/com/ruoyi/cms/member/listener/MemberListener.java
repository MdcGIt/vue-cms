package com.ruoyi.cms.member.listener;

import com.ruoyi.cms.member.CmsMemberConstants;
import com.ruoyi.cms.member.impl.ContributeMemberStatData;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.listener.event.AfterContentDeleteEvent;
import com.ruoyi.contentcore.listener.event.AfterContentSaveEvent;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.impl.ContentDynamicDataService;
import com.ruoyi.member.listener.event.*;
import com.ruoyi.member.security.StpMemberUtil;
import com.ruoyi.member.service.IMemberStatDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberListener {

	private final IContentService contentService;

	private final ContentDynamicDataService contentDynamicDataService;

	private final IMemberStatDataService memberStatDataService;

	@EventListener
	public void afterContentSaveEvent(AfterContentSaveEvent event) {
		if (event.isAdd() && IdUtils.validate(event.getContent().getContentEntity().getContributorId())) {
			// 更新会员文章数量
			memberStatDataService.changeMemberStatData(StpMemberUtil.getLoginIdAsLong(), ContributeMemberStatData.TYPE, 1);
		}
	}

	@EventListener
	public void afterContentDeleteEvent(AfterContentDeleteEvent event) {
		if (IdUtils.validate(event.getContent().getContentEntity().getContributorId())) {
			// 更新会员文章数量
			memberStatDataService.changeMemberStatData(StpMemberUtil.getLoginIdAsLong(), ContributeMemberStatData.TYPE, -1);
		}
	}

	@EventListener
	public void beforeMemberFavorite(BeforeMemberFavoriteEvent event) {
		if (CmsMemberConstants.MEMBER_FAVORITES_DATA_TYPE.equals(event.getDataType())) {
			CmsContent content = this.contentService.getById(event.getDataId());
			Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("dataId", event.getDataId()));
		}
	}

	@EventListener
	public void afterMemberFavorite(AfterMemberFavoriteEvent event) {
		if (CmsMemberConstants.MEMBER_FAVORITES_DATA_TYPE.equals(event.getMemberFavorites().getDataType())) {
			// 内容收藏数+1
			this.contentDynamicDataService.increaseFavoriteCount(event.getMemberFavorites().getDataId());
		}
	}

	@EventListener
	public void afterCancelMemberFavorite(AfterMemberCancelFavoriteEvent event) {
		if (CmsMemberConstants.MEMBER_FAVORITES_DATA_TYPE.equals(event.getMemberFavorites().getDataType())) {
			// 内容收藏数-1
			this.contentDynamicDataService.decreaseFavoriteCount(event.getMemberFavorites().getDataId());
		}
	}

	@EventListener
	public void beforeMemberLike(BeforeMemberLikeEvent event) {
		if (CmsMemberConstants.MEMBER_LIKE_DATA_TYPE.equals(event.getDataType())) {
			CmsContent content = this.contentService.getById(event.getDataId());
			Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("dataId", event.getDataId()));
		}
	}

	@EventListener
	public void afterMemberLike(AfterMemberLikeEvent event) {
		if (CmsMemberConstants.MEMBER_LIKE_DATA_TYPE.equals(event.getMemberLike().getDataType())) {
			// 内容点赞数+1
			this.contentDynamicDataService.increaseLikeCount(event.getMemberLike().getDataId());
		}
	}

	@EventListener
	public void afterCancelMemberFavorite(AfterMemberCancelLikeEvent event) {
		if (CmsMemberConstants.MEMBER_LIKE_DATA_TYPE.equals(event.getMemberLike().getDataType())) {
			// 内容点赞数-1
			this.contentDynamicDataService.decreaseLikeCount(event.getMemberLike().getDataId());
		}
	}
}
