package com.ruoyi.vote.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.vote.core.IVoteItemType;
import com.ruoyi.vote.core.IVoteUserType;
import com.ruoyi.vote.domain.Vote;
import com.ruoyi.vote.priv.VotePriv;
import com.ruoyi.vote.service.IVoteService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/vote")
public class VoteController extends BaseRestController {

	private final IVoteService voteService;

	private final List<IVoteUserType> userTypes;

	private final List<IVoteItemType> itemTypes;

	@Priv(type = AdminUserType.TYPE, value = VotePriv.VIEW)
	@GetMapping
	public R<?> getPageList(@RequestParam(required = false) String title, @RequestParam(required = false) String status) {
		PageRequest pr = this.getPageRequest();
		Page<Vote> page = this.voteService.lambdaQuery().like(StringUtils.isNotEmpty(title), Vote::getTitle, title)
				.eq(StringUtils.isNotEmpty(status), Vote::getStatus, status)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Priv(type = AdminUserType.TYPE, value = VotePriv.VIEW)
	@GetMapping("/{voteId}")
	public R<?> getVoteDetail(@PathVariable @Min(1) Long voteId) {
		Vote vote = this.voteService.getById(voteId);
		Assert.notNull(vote, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("voteId", voteId));
		return R.ok(vote);
	}

	@SaAdminCheckLogin
	@Priv(type = AdminUserType.TYPE, value = VotePriv.VIEW)
	@GetMapping("/userTypes")
	public R<?> getVoteUserTypes() {
		List<Map<String, String>> list = this.userTypes.stream()
				.map(vut -> Map.of("id", vut.getId(), "name", I18nUtils.parse(vut.getName()))).toList();
		return R.ok(list);
	}

	@SaAdminCheckLogin
	@GetMapping("/item/types")
	public R<?> getVoteItemTypes() {
		List<Map<String, String>> list = this.itemTypes.stream()
				.map(vut -> Map.of("id", vut.getId(), "name", I18nUtils.parse(vut.getName()))).toList();
		return R.ok(list);
	}

	@Log(title = "新增问卷调查", businessType = BusinessType.INSERT)
	@Priv(type = AdminUserType.TYPE, value = VotePriv.ADD)
	@PostMapping
	public R<?> add(@RequestBody Vote vote) {
		vote.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		this.voteService.addVote(vote);
		return R.ok();
	}

	@Log(title = "编辑问卷调查", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = { VotePriv.ADD, VotePriv.EDIT })
	@PutMapping
	public R<?> update(@RequestBody Vote vote) {
		vote.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		this.voteService.updateVote(vote);
		return R.ok();
	}

	@Log(title = "删除问卷调查", businessType = BusinessType.DELETE)
	@Priv(type = AdminUserType.TYPE, value = VotePriv.DELETE)
	@DeleteMapping
	public R<String> delete(@RequestBody @NotEmpty List<Long> dictWordIds) {
		this.voteService.deleteVotes(dictWordIds);
		return R.ok();
	}
}
