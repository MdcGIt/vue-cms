package com.ruoyi.search.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.search.domain.SearchLog;
import com.ruoyi.search.domain.dto.SearchLogDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface ISearchLogService extends IService<SearchLog> {

	/**
	 * 添加搜索日志
	 * 
	 * @param dto
	 * @return
	 */
	void addSearchLog(SearchLogDTO dto);

	void addSearchLog(String source, String query, HttpServletRequest request);
}