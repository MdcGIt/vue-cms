package com.ruoyi.vote.controller.front;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.vote.domain.Vote;
import com.ruoyi.vote.domain.vo.VoteVO;
import com.ruoyi.vote.service.IVoteApiService;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/vote")
public class VoteApiController extends BaseRestController {

	private final IVoteApiService voteApiService;

	@GetMapping("/{voteId}")
	public R<?> getVoteDetail(@PathVariable @Min(1) Long voteId) {
		VoteVO vote = this.voteApiService.getVote(voteId);
		return R.ok(vote);
	}

	@GetMapping("/result/{voteId}")
	public R<?> getVoteResult(@PathVariable @Min(1) Long voteId) {

		return null;
	}

	@PostMapping
	public R<?> doVote(@RequestBody Vote vote) {
		
		return null;
	}
}
