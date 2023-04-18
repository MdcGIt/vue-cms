package com.ruoyi.search.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.search.domain.IndexModel;
import com.ruoyi.search.domain.dto.SearchModelDTO;
import com.ruoyi.search.service.IIndexModelService;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@SaAdminCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/search/model")
public class IndexModelController extends BaseRestController {

	private final IIndexModelService indexModelService;

	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<IndexModel> page = this.indexModelService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), IndexModel::getName, query).or()
				.like(StringUtils.isNotEmpty(query), IndexModel::getCode, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Log(title = "新增索引模型", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> addIndexModel(@RequestBody @Validated SearchModelDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.indexModelService.addIndexModel(dto);
		return R.ok();
	}

	@Log(title = "删除索引模型", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> deleteIndexModel(@RequestBody @NotEmpty List<Long> modelIds) {
		this.indexModelService.deleteIndexModel(modelIds);
		return R.ok();
	}
}