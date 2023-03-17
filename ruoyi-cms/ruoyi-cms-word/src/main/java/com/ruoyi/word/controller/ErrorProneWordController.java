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
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.word.domain.CmsErrorProneWord;
import com.ruoyi.word.service.IErrorProneWordService;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 易错词前端控制器
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@SaAdminCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/errorproneword")
public class ErrorProneWordController extends BaseRestController {

	private final IErrorProneWordService errorProneWordService;

	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<CmsErrorProneWord> page = this.errorProneWordService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), CmsErrorProneWord::getWord, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@PostMapping
	public R<?> add(@RequestBody CmsErrorProneWord errorProneWord) {
		errorProneWord.setWordId(IdUtils.getSnowflakeId());
		errorProneWord.createBy(StpAdminUtil.getLoginUser().getUsername());
		this.errorProneWordService.save(errorProneWord);
		return R.ok();
	}

	@PutMapping
	public R<String> edit(@RequestBody CmsErrorProneWord errorProneWord) {
		CmsErrorProneWord dbErrorProneWord = this.errorProneWordService.getById(errorProneWord.getWordId());
		Assert.notNull(dbErrorProneWord,
				() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("wordId", errorProneWord.getWordId()));

		dbErrorProneWord.setWord(errorProneWord.getWord());
		dbErrorProneWord.setReplaceWord(errorProneWord.getReplaceWord());
		dbErrorProneWord.setRemark(errorProneWord.getRemark());
		dbErrorProneWord.updateBy(StpAdminUtil.getLoginUser().getUsername());
		this.errorProneWordService.updateById(errorProneWord);
		return R.ok();
	}

	@DeleteMapping
	public R<String> remove(@RequestBody @NotEmpty List<Long> errorProneWordIds) {
		this.errorProneWordService.removeByIds(errorProneWordIds);
		return R.ok();
	}
}
