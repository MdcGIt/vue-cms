package com.ruoyi.word.service.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.word.domain.CmsErrorProneWord;
import com.ruoyi.word.mapper.CmsErrorProneWordMapper;
import com.ruoyi.word.service.IErrorProneWordService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ErrorProneWordServiceImpl extends ServiceImpl<CmsErrorProneWordMapper, CmsErrorProneWord> implements IErrorProneWordService {

	@Override
	public String replaceErrorProneWords(String text) {
		Map<String, String> map = this.getErrorProneWords();
		for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, String> e = iterator.next();
			text = text.replaceAll(e.getKey(), e.getValue());
		}
		return text;
	}

	@Override
	public Map<String, String> getErrorProneWords() {
		// TODO 缓存
		return this.list().stream().collect(Collectors.toMap(CmsErrorProneWord::getWord, CmsErrorProneWord::getReplaceWord));
	} 
}
