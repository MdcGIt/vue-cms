package com.ruoyi.search.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.search.domain.DictWord;
import com.ruoyi.search.domain.dto.DictWordDTO;
import com.ruoyi.search.exception.SearchErrorCode;
import com.ruoyi.search.mapper.DictWordMapper;
import com.ruoyi.search.service.IDictWordService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DictWordServiceImpl extends ServiceImpl<DictWordMapper, DictWord> implements IDictWordService {

	private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	private static final String MODIFY_CACHE_KEY = "search:dict:modify:";

	private final RedisCache redisCache;

	@Override
	public void batchAddDictWords(DictWordDTO dto) {
		List<DictWord> dictWords = new ArrayList<>();
		for (String word : dto.getWords()) {
			if (StringUtils.isBlank(word)) {
				continue;
			}
			Long count = this.lambdaQuery().eq(DictWord::getWord, word.trim()).count();
			Assert.isTrue(count == 0, () -> SearchErrorCode.DICT_WORD_EXISTS.exception(word));
			
			DictWord dictWord = new DictWord();
			dictWord.setWordId(IdUtils.getSnowflakeId());
			dictWord.setWordType(dto.getWordType());
			dictWord.setWord(word);
			dictWord.createBy(dto.getOperator().getUsername());
			dictWords.add(dictWord);
		}
		this.saveBatch(dictWords);
		this.redisCache.setCacheObject(MODIFY_CACHE_KEY + dto.getWordType(), LocalDateTime.now().format(FORMATTER));
	}

	@Override
	public void deleteDictWord(List<Long> dictWordIds) {
		List<DictWord> list = this.listByIds(dictWordIds);
		Set<String> wordTypes = new HashSet<>();
		for (DictWord dictWord : list) {
			wordTypes.add(dictWord.getWordType());
		}
		this.removeByIds(dictWordIds);
		wordTypes.forEach(wordType -> this.redisCache.setCacheObject(MODIFY_CACHE_KEY + wordType,
				LocalDateTime.now().format(FORMATTER)));
	}

	@Override
	public String getLastModified(String wordType) {
		return this.redisCache.getCacheObject(MODIFY_CACHE_KEY + wordType);
	}
}