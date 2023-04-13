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
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.word.domain.SensitiveWord;
import com.ruoyi.word.permission.WordPriv;
import com.ruoyi.word.service.ISensitiveWordService;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 敏感词前端控制器
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
@RequiredArgsConstructor
@RestController
@RequestMapping("/word/sensitiveword")
public class SensitiveWordController extends BaseRestController {

	private final ISensitiveWordService sensitiveWordService;

	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<SensitiveWord> page = this.sensitiveWordService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), SensitiveWord::getWord, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@PostMapping
	public R<?> add(@RequestBody SensitiveWord sensitiveWord) {
		sensitiveWord.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		this.sensitiveWordService.addWord(sensitiveWord);
		return R.ok();
	}

	@PutMapping
	public R<?> edit(@RequestBody SensitiveWord sensitiveWord) {
		Assert.isTrue(IdUtils.validate(sensitiveWord.getWordId()),
				() -> CommonErrorCode.INVALID_REQUEST_ARG.exception("wordId"));

		sensitiveWord.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		this.sensitiveWordService.editWord(sensitiveWord);
		return R.ok();
	}

	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> sensitiveWordIds) {
		this.sensitiveWordService.deleteWord(sensitiveWordIds);
		return R.ok();
	}
}
