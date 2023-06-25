package com.ruoyi.vote.controller.front;

import com.ruoyi.system.annotation.IgnoreDemoMode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.system.validator.LongId;
import com.ruoyi.vote.domain.dto.VoteSubmitDTO;
import com.ruoyi.vote.domain.vo.VoteVO;
import com.ruoyi.vote.service.IVoteApiService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/vote")
public class VoteApiController extends BaseRestController {

	private final IVoteApiService voteApiService;

	@GetMapping("/{voteId}")
	public R<?> getVoteDetail(@PathVariable @LongId Long voteId) {
		VoteVO vote = this.voteApiService.getVote(voteId);
		return R.ok(vote);
	}

	@GetMapping("/result/{voteId}")
	public R<?> getVoteResult(@PathVariable @LongId Long voteId) {
		VoteVO vote = this.voteApiService.getVote(voteId);
		return R.ok(vote);
	}

	@IgnoreDemoMode
	@PostMapping
	public R<?> submitVote(@RequestBody @Validated VoteSubmitDTO dto, HttpServletRequest request) {
		dto.setIp(ServletUtils.getIpAddr(request));
		dto.setUserAgent(ServletUtils.getUserAgent(request));
		this.voteApiService.submitVote(dto);
		return R.ok();
	}
}
