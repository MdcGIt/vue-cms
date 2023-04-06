package com.ruoyi.search.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.search.domain.DictWord;
import com.ruoyi.search.domain.dto.DictWordDTO;
import com.ruoyi.search.service.IDictWordService;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/search/dict")
public class DictWordController extends BaseRestController {

	private final IDictWordService dictWordService;

	@SaAdminCheckLogin
	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<DictWord> q = new LambdaQueryWrapper<DictWord>().like(StringUtils.isNotEmpty(query),
				DictWord::getWord, query);
		Page<DictWord> page = this.dictWordService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		return this.bindDataTable(page);
	}

	@Log(title = "新增检索词", businessType = BusinessType.UPDATE)
	@SaAdminCheckLogin
	@PostMapping
	public R<?> add(@RequestBody DictWordDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.dictWordService.batchAddDictWords(dto);
		return R.ok();
	}

	@Log(title = "删除检索词", businessType = BusinessType.DELETE)
	@SaAdminCheckLogin
	@DeleteMapping
	public R<String> delete(@RequestBody @NotEmpty List<Long> dictWordIds) {
		this.dictWordService.removeByIds(dictWordIds);
		return R.ok();
	}

	/**
	 * 检查词库是否有变更
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/ik/{type}", method = RequestMethod.HEAD)
	public void checkDictNewest(@PathVariable("type") String type, HttpServletRequest request,
			HttpServletResponse response) {
		String lastModified = this.dictWordService.getLastModified(type);
		response.setHeader("Last-Modified", StringUtils.isEmpty(lastModified) ? "0" : lastModified);
	}

	/**
	 * IK热更词库API
	 * 
	 * @return 词库字符串，每行一个词
	 */
	@RequestMapping(value = "/ik/{type}", method = RequestMethod.GET, produces = { "text/html;charset=utf-8" })
	public String dictNewest(@PathVariable("type") String type) {
		String words = this.dictWordService.lambdaQuery().eq(DictWord::getWordType, type).list().stream()
				.map(DictWord::getWord).collect(Collectors.joining("\n"));
		return words;
	}
}
