package com.ruoyi.search.service.impl;

import java.time.LocalDateTime;

import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.search.domain.SearchLog;
import com.ruoyi.search.domain.dto.SearchLogDTO;
import com.ruoyi.search.mapper.SearchLogMapper;
import com.ruoyi.search.service.ISearchLogService;

@RequiredArgsConstructor
@Service
public class SearchLogServiceImpl extends ServiceImpl<SearchLogMapper, SearchLog> implements ISearchLogService {

	private final AsyncTaskManager asyncTaskManager;

	@Override
	public void addSearchLog(SearchLogDTO dto) {
		asyncTaskManager.execute(() -> {
			SearchLog sLog = new SearchLog();
			sLog.setWord(dto.getWord());
			sLog.setIp(dto.getIp());
			sLog.setLogTime(dto.getLogTime());
			sLog.setUserAgent(dto.getUserAgent());
			sLog.setReferer(dto.getReferer());
			this.save(sLog);
		});
	}

	@Override
	public void addSearchLog(String query, HttpServletRequest request) {
		final String userAgent = ServletUtils.getUserAgent(request);
		final String ip = ServletUtils.getIpAddr(request);
		final String referer = ServletUtils.getReferer(request);
		final LocalDateTime logTime = LocalDateTime.now();
		asyncTaskManager.execute(() -> {
			SearchLog sLog = new SearchLog();
			sLog.setWord(query);
			sLog.setIp(ip);
			sLog.setLogTime(logTime);
			sLog.setUserAgent(userAgent);
			sLog.setReferer(referer);
			this.save(sLog);
		});
	}
}