package com.ruoyi.search.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.search.domain.SearchLog;
import com.ruoyi.search.domain.dto.SearchLogDTO;
import com.ruoyi.search.mapper.SearchLogMapper;
import com.ruoyi.search.service.ISearchLogService;

@Service
public class SearchLogServiceImpl extends ServiceImpl<SearchLogMapper, SearchLog> implements ISearchLogService {

	@Override
	public void addSearchLog(SearchLogDTO dto) {
		new Thread(() -> {
			SearchLog sLog = new SearchLog();
			sLog.setLogTime(LocalDateTime.now());
			sLog.setWord(dto.getWord());
			sLog.setIp(dto.getIp());
			sLog.setUserAgent(dto.getUserAgent());
			sLog.setReferer(dto.getReferer());
			this.save(sLog);
		}).start();
	}
}