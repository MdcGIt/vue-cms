package com.ruoyi.search.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.IP2RegionUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.search.domain.SearchLog;
import com.ruoyi.search.service.ISearchLogService;
import com.ruoyi.system.security.AdminUserType;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/search/log")
public class SearchLogController extends BaseRestController {

	private final ISearchLogService searchLogService;
	
	@Priv(type = AdminUserType.TYPE)
	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<SearchLog> page = this.searchLogService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), SearchLog::getWord, query)
				.orderByDesc(SearchLog::getLogId)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Log(title = "删除检索日志", businessType = BusinessType.DELETE)
	@Priv(type = AdminUserType.TYPE)
	@DeleteMapping
	public R<?> delete(@RequestBody @NotEmpty List<String> logIds) {
		this.searchLogService.removeByIds(logIds);
		return R.ok();
	}
}