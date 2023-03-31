package com.ruoyi.word.service.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.word.domain.ErrorProneWord;
import com.ruoyi.word.exception.WordErrorCode;
import com.ruoyi.word.mapper.ErrorProneWordMapper;
import com.ruoyi.word.service.IErrorProneWordService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ErrorProneWordServiceImpl extends ServiceImpl<ErrorProneWordMapper, ErrorProneWord>
		implements IErrorProneWordService {

	private static final String CACHE_KEY = "cms:err_prone_word";

	private final RedisCache redisCache;

	/**
	 * 查找置顶文本中的易错词
	 * 
	 * @param text
	 * @return
	 */
	@Override
	public Map<String, String> findErrorProneWords(String text) {
		return this.getErrorProneWords().entrySet().stream().filter(e -> text.indexOf(e.getKey()) > -1)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

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
		Map<String, String> cacheMap = this.redisCache.getCacheMap(CACHE_KEY, () -> this.list().stream()
				.collect(Collectors.toMap(ErrorProneWord::getWord, ErrorProneWord::getReplaceWord)));
		return cacheMap;
	}

	@Override
	public void addErrorProneWord(ErrorProneWord word) {
		Long count = this.lambdaQuery().eq(ErrorProneWord::getWord, word.getWord()).count();
		Assert.isTrue(count == 0, () -> WordErrorCode.CONFLIECT_ERROR_PRONE_WORD.exception(word.getWord()));

		word.setWordId(IdUtils.getSnowflakeId());
		this.save(word);
	}

	@Override
	public void updateErrorProneWord(ErrorProneWord word) {
		ErrorProneWord db = this.getById(word.getWordId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("wordId", word.getWordId()));

		Long count = this.lambdaQuery().eq(ErrorProneWord::getWord, word.getWord())
				.ne(ErrorProneWord::getWordId, word.getWordId()).count();
		Assert.isTrue(count == 0, () -> WordErrorCode.CONFLIECT_ERROR_PRONE_WORD.exception(word.getWord()));

		db.setWord(word.getWord());
		db.setReplaceWord(word.getReplaceWord());
		db.setRemark(word.getRemark());
		db.updateBy(word.getUpdateBy());
		this.updateById(db);
	}
}
