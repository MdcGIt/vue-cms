package com.ruoyi.generator.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.generator.domain.GenTable;
import com.ruoyi.generator.domain.GenTableColumn;
import com.ruoyi.generator.service.IGenTableColumnService;
import com.ruoyi.generator.service.IGenTableService;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 代码生成 操作处理
 * 
 * @author ruoyi
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/tool/gen")
public class GenController extends BaseRestController {
	
	private final IGenTableService genTableService;

	private final IGenTableColumnService genTableColumnService;

	/**
	 * 查询代码生成列表
	 */
	@Priv(type = AdminUserType.TYPE, value = "tool:gen:list")
	@GetMapping("/list")
	public R<?> genList(GenTable genTable) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<GenTable> q =  new LambdaQueryWrapper<GenTable>()
			.like(StringUtils.isNotEmpty(genTable.getTableName()), GenTable::getTableName, genTable.getTableName())
			.like(StringUtils.isNotEmpty(genTable.getTableComment()), GenTable::getTableComment, genTable.getTableComment())
			.ge(Objects.nonNull(genTable.getParams().get("beginTime")), GenTable::getCreateTime, genTable.getParams().get("beginTime"))
			.le(Objects.nonNull(genTable.getParams().get("endTime")), GenTable::getCreateTime, genTable.getParams().get("endTime"));
		Page<GenTable> page = genTableService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return bindDataTable(page);
	}

	/**
	 * 修改代码生成业务
	 */
	@Priv(type = AdminUserType.TYPE, value = "tool:gen:query")
	@GetMapping(value = "/{tableId}")
	public R<?> getInfo(@PathVariable Long tableId) {
		GenTable table = genTableService.selectGenTableById(tableId);
		List<GenTable> tables = genTableService.selectGenTableAll();
		List<GenTableColumn> list = genTableColumnService.selectGenTableColumnListByTableId(tableId);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("info", table);
		map.put("rows", list);
		map.put("tables", tables);
		return R.ok(map);
	}

	/**
	 * 查询数据库列表
	 */
	@Priv(type = AdminUserType.TYPE, value = "tool:gen:list")
	@GetMapping("/db/list")
	public R<?> dataList(GenTable genTable) {
		List<GenTable> list = genTableService.selectDbTableList(genTable);
		return bindDataTable(list);
	}

	/**
	 * 查询数据表字段列表
	 */
	@Priv(type = AdminUserType.TYPE, value = "tool:gen:list")
	@GetMapping(value = "/column/{tableId}")
	public R<?> columnList(Long tableId) {
		List<GenTableColumn> list = genTableColumnService.selectGenTableColumnListByTableId(tableId);
		return this.bindDataTable(list);
	}

	/**
	 * 导入表结构（保存）
	 */
	@Priv(type = AdminUserType.TYPE, value = "tool:gen:import")
	@Log(title = "代码生成", businessType = BusinessType.IMPORT)
	@PostMapping("/importTable")
	public R<?> importTableSave(String tables) {
		String[] tableNames = StringUtils.split(tables, StringUtils.C_COMMA);
		// 查询表信息
		List<GenTable> tableList = genTableService.selectDbTableListByNames(tableNames);
		genTableService.importGenTable(tableList, StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	/**
	 * 修改保存代码生成业务
	 */
	@Priv(type = AdminUserType.TYPE, value = "tool:gen:edit")
	@Log(title = "代码生成", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> editSave(@Validated @RequestBody GenTable genTable) {
		genTableService.validateEdit(genTable);
		genTableService.updateGenTable(genTable);
		return R.ok();
	}

	/**
	 * 删除代码生成
	 */
	@Priv(type = AdminUserType.TYPE, value = "tool:gen:remove")
	@Log(title = "代码生成", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody List<Long> tableIds) {
		this.genTableService.deleteGenTableByIds(tableIds);
		return R.ok();
	}

	/**
	 * 预览代码
	 */
	@Priv(type = AdminUserType.TYPE, value = "tool:gen:preview")
	@GetMapping("/preview/{tableId}")
	public R<?> preview(@PathVariable("tableId") Long tableId) throws IOException {
		Map<String, String> dataMap = genTableService.previewCode(tableId);
		return R.ok(dataMap);
	}

	/**
	 * 生成代码（下载方式）
	 */
	@Priv(type = AdminUserType.TYPE, value = "tool:gen:code")
	@Log(title = "代码生成", businessType = BusinessType.GENCODE)
	@GetMapping("/download/{tableName}")
	public void download(HttpServletResponse response, @PathVariable("tableName") String tableName) throws IOException {
		byte[] data = genTableService.downloadCode(tableName);
		genCode(response, data);
	}

	/**
	 * 生成代码（自定义路径）
	 */
	@Priv(type = AdminUserType.TYPE, value = "tool:gen:code")
	@Log(title = "代码生成", businessType = BusinessType.GENCODE)
	@GetMapping("/genCode/{tableName}")
	public R<?> genCode(@PathVariable("tableName") String tableName) {
		genTableService.generatorCode(tableName);
		return R.ok();
	}

	/**
	 * 同步数据库
	 */
	@Priv(type = AdminUserType.TYPE, value = "tool:gen:edit")
	@Log(title = "代码生成", businessType = BusinessType.UPDATE)
	@GetMapping("/synchDb/{tableName}")
	public R<?> synchDb(@PathVariable("tableName") String tableName) {
		genTableService.synchDb(tableName);
		return R.ok();
	}

	/**
	 * 批量生成代码
	 */
	@Priv(type = AdminUserType.TYPE, value = "tool:gen:code")
	@Log(title = "代码生成", businessType = BusinessType.GENCODE)
	@GetMapping("/batchGenCode")
	public void batchGenCode(HttpServletResponse response, String tables) throws IOException {
		String[] tableNames = StringUtils.split(tables, StringUtils.C_COMMA);
		byte[] data = genTableService.downloadCode(tableNames);
		genCode(response, data);
	}

	/**
	 * 生成zip文件
	 */
	private void genCode(HttpServletResponse response, byte[] data) throws IOException {
		response.reset();
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
		response.setHeader("Content-Disposition", "attachment; filename=\"ruoyi.zip\"");
		response.addHeader("Content-Length", "" + data.length);
		response.setContentType("application/octet-stream; charset=UTF-8");
		IOUtils.write(data, response.getOutputStream());
	}
}