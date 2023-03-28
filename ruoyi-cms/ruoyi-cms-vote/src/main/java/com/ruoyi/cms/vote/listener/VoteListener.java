package com.ruoyi.cms.vote.listener;

import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ruoyi.cms.vote.service.ICmsVoteService;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.vote.domain.Vote;
import com.ruoyi.vote.listener.BeforeVoteAddEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VoteListener {

	private final ISiteService siteService;
	
	private final ICmsVoteService cmsVoteService;

	@EventListener
	public void beforeVoteAdd(BeforeVoteAddEvent event) {
		Vote vote = event.getVote();
		CmsSite site = siteService.getCurrentSite(ServletUtils.getRequest());
		if (Objects.nonNull(site)) {
			vote.setSource(this.cmsVoteService.getVoteSource(site.getSiteId()));
		}
	}
}
