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
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.word.domain.CmsTagWord;
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
@SaAdminCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/tagword")
public class TagWordController extends BaseRestController {

	private final ISiteService siteService;

	private final ITagWordService tagWordService;

	@GetMapping
	public R<?> getPageList(@RequestParam("groupId") @Min(1) Long groupId,
			@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<CmsTagWord> page = this.tagWordService.lambdaQuery().eq(CmsTagWord::getGroupId, groupId)
				.like(StringUtils.isNotEmpty(query), CmsTagWord::getWord, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		page.getRecords().forEach(tag -> {
			if (StringUtils.isNotEmpty(tag.getLogo())) {
				tag.setSrc(InternalUrlUtils.getActualPreviewUrl(tag.getLogo()));
			}
		});
		return this.bindDataTable(page);
	}

	@PostMapping
	public R<?> add(@RequestBody CmsTagWord tagWord) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		tagWord.setSiteId(site.getSiteId());
		tagWord.createBy(StpAdminUtil.getLoginUser().getUsername());
		this.tagWordService.addTagWord(tagWord);
		return R.ok();
	}

	@PutMapping
	public R<?> edit(@RequestBody CmsTagWord tagWord) {
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
