package com.ruoyi.vote.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.vote.domain.VoteSubject;
import com.ruoyi.vote.domain.VoteSubjectItem;
import com.ruoyi.vote.domain.dto.SaveSubjectItemsDTO;
import com.ruoyi.vote.service.IVoteSubjectItemService;
import com.ruoyi.vote.service.IVoteSubjectService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@SaAdminCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/vote/subject")
public class VoteSubjectController extends BaseRestController {

	private final IVoteSubjectService voteSubjectService;

	private final IVoteSubjectItemService voteSubjectItemService;

	@GetMapping
	public R<?> getVoteSubjects(@RequestParam @Min(1) Long voteId) {
		List<VoteSubject> subjects = this.voteSubjectService.getVoteSubjectList(voteId);
		return this.bindDataTable(subjects);
	}

	@GetMapping("/{subjectId}")
	public R<?> getVoteSubjectDetail(@PathVariable @Min(1) Long subjectId) {
		VoteSubject subject = this.voteSubjectService.getById(subjectId);
		Assert.notNull(subject, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("subjectId", subjectId));
		return R.ok(subject);
	}

	@PostMapping
	public R<?> add(@RequestBody VoteSubject voteSubject) {
		voteSubject.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		this.voteSubjectService.addVoteSubject(voteSubject);
		return R.ok();
	}

	@PutMapping
	public R<?> update(@RequestBody VoteSubject voteSubject) {
		voteSubject.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		this.voteSubjectService.updateVoteSubject(voteSubject);
		return R.ok();
	}

	@DeleteMapping
	public R<String> delete(@RequestBody @NotEmpty List<Long> subjectIds) {
		this.voteSubjectService.deleteVoteSubjects(subjectIds);
		return R.ok();
	}

	@GetMapping("/items/{subjectId}")
	public R<?> getSubjectItems(@PathVariable @Min(1) Long subjectId) {
		List<VoteSubjectItem> list = voteSubjectItemService.lambdaQuery().eq(VoteSubjectItem::getSubjectId, subjectId)
				.orderByAsc(VoteSubjectItem::getSortFlag).list();
		return this.bindDataTable(list);
	}

	@PostMapping("/items")
	public R<?> saveSubjectItems(@RequestBody SaveSubjectItemsDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.voteSubjectService.saveSubjectItems(dto);
		return R.ok();
	}
}