package com.ruoyi.vote.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.vote.domain.Vote;
import com.ruoyi.vote.service.IVoteService;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/vote")
public class VoteController extends BaseRestController {

	private final IVoteService voteService;

	@SaAdminCheckLogin
	@GetMapping
	public R<?> getPageList(@RequestParam(required = false) String title) {
		PageRequest pr = this.getPageRequest();
		Page<Vote> page = this.voteService.lambdaQuery().like(Vote::getTitle, title)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Priv(type = AdminUserType.TYPE, value = "vote:mgr.add")
	@PostMapping
	public R<?> add(@RequestBody Vote vote) {
		vote.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		this.voteService.addVote(vote);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = { "vote:mgr.add", "vote:mgr.edit" })
	@PostMapping
	public R<?> update(@RequestBody Vote vote) {
		vote.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		this.voteService.updateVote(vote);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = "vote:mgr.delete")
	@DeleteMapping
	public R<String> delete(@RequestBody @NotEmpty List<Long> dictWordIds) {
		this.voteService.deleteVotes(dictWordIds);
		return R.ok();
	}
}
