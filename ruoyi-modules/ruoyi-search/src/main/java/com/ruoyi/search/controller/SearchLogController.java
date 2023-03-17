package com.ruoyi.search.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.search.domain.SearchLog;
import com.ruoyi.search.service.ISearchLogService;
import com.ruoyi.system.security.SaAdminCheckLogin;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/search/log")
public class SearchLogController extends BaseRestController {

	private final ISearchLogService searchLogService;
	
	@SaAdminCheckLogin
	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<SearchLog> page = this.searchLogService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), SearchLog::getWord, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@SaAdminCheckLogin
	@DeleteMapping
	public R<?> delete(@RequestBody @NotEmpty List<String> logIds) {
		this.searchLogService.removeByIds(logIds);
		return R.ok();
	}
}