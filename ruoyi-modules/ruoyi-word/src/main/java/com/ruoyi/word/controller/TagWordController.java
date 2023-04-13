package com.ruoyi.word.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.word.domain.TagWord;
import com.ruoyi.word.permission.WordPriv;
import com.ruoyi.word.service.ITagWordService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * TAG词前端控制器
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
@RequiredArgsConstructor
@RestController
@RequestMapping("/word/tagword")
public class TagWordController extends BaseRestController {

	private final ITagWordService tagWordService;

	@GetMapping
	public R<?> getPageList(@RequestParam("groupId") @Min(1) Long groupId,
			@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<TagWord> page = this.tagWordService.lambdaQuery().eq(TagWord::getGroupId, groupId)
				.like(StringUtils.isNotEmpty(query), TagWord::getWord, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@PostMapping
	public R<?> add(@RequestBody TagWord tagWord) {
		tagWord.createBy(StpAdminUtil.getLoginUser().getUsername());
		this.tagWordService.addTagWord(tagWord);
		return R.ok();
	}

	@PutMapping
	public R<?> edit(@RequestBody TagWord tagWord) {
		tagWord.updateBy(StpAdminUtil.getLoginUser().getUsername());
		this.tagWordService.editTagWord(tagWord);
		return R.ok();
	}

	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> tagWordIds) {
		this.tagWordService.deleteTagWords(tagWordIds);
		return R.ok();
	}
}
