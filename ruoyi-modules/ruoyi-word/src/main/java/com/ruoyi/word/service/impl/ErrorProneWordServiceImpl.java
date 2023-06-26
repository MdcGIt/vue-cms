package com.ruoyi.word.service.impl;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.word.domain.SensitiveWord;
import com.ruoyi.word.sensitive.ErrorProneWordProcessor;
import com.ruoyi.word.sensitive.SensitiveWordProcessor;
import com.ruoyi.word.sensitive.SensitiveWordType;
import org.springframework.boot.CommandLineRunner;
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
		implements IErrorProneWordService, CommandLineRunner {

	private final ErrorProneWordProcessor processor;

	private static final String CACHE_KEY = "err_prone_word:";

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
	public Map<String, String> check(String text) {
		return this.processor.listWords(text);
	}

	@Override
	public String replaceErrorProneWords(String text) {
		return this.processor.replace(text, ErrorProneWordProcessor.MatchType.MAX);
	}

	@Override
	public Map<String, String> getErrorProneWords() {
		Map<String, String> cacheMap = this.redisCache.getCacheObject(CACHE_KEY, () -> this.list().stream()
				.collect(Collectors.toMap(ErrorProneWord::getWord, ErrorProneWord::getReplaceWord)));
		return cacheMap;
	}

	@Override
	public void addErrorProneWord(ErrorProneWord word) {
		Long count = this.lambdaQuery().eq(ErrorProneWord::getWord, word.getWord()).count();
		Assert.isTrue(count == 0, () -> WordErrorCode.CONFLIECT_ERROR_PRONE_WORD.exception(word.getWord()));

		word.setWordId(IdUtils.getSnowflakeId());
		this.save(word);
		this.processor.addWord(word.getWord(), word.getReplaceWord());
	}

	@Override
	public void updateErrorProneWord(ErrorProneWord word) {
		ErrorProneWord db = this.getById(word.getWordId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("wordId", word.getWordId()));

		Long count = this.lambdaQuery().eq(ErrorProneWord::getWord, word.getWord())
				.ne(ErrorProneWord::getWordId, word.getWordId()).count();
		Assert.isTrue(count == 0, () -> WordErrorCode.CONFLIECT_ERROR_PRONE_WORD.exception(word.getWord()));

		String oldWord = db.getWord();

		db.setWord(word.getWord());
		db.setReplaceWord(word.getReplaceWord());
		db.setRemark(word.getRemark());
		db.updateBy(word.getUpdateBy());
		this.updateById(db);
		this.processor.removeWord(oldWord);
		this.processor.addWord(db.getWord(), db.getReplaceWord());
	}

	@Override
	public void run(String... args) {
		List<ErrorProneWord> wordList = this.lambdaQuery()
				.select(ErrorProneWord::getWord, ErrorProneWord::getReplaceWord).list();
		this.processor.addWords(wordList.stream().collect(Collectors
				.toMap(ErrorProneWord::getWord, ErrorProneWord::getReplaceWord)));
	}
}
