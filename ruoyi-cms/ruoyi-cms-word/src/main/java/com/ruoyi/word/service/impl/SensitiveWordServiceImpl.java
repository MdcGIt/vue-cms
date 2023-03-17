package com.ruoyi.word.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.word.WordConstants;
import com.ruoyi.word.domain.CmsSensitiveWord;
import com.ruoyi.word.mapper.CmsSensitiveWordMapper;
import com.ruoyi.word.sensitive.SensitiveWordProcessor;
import com.ruoyi.word.sensitive.SensitiveWordProcessor.MatchType;
import com.ruoyi.word.sensitive.SensitiveWordProcessor.ReplaceType;
import com.ruoyi.word.sensitive.SensitiveWordType;
import com.ruoyi.word.service.ISensitiveWordService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl extends ServiceImpl<CmsSensitiveWordMapper, CmsSensitiveWord> implements ISensitiveWordService {
	
	private final SensitiveWordProcessor processor;

	@PostConstruct
	private void initWordMap() {
		// 敏感词黑名单
		List<CmsSensitiveWord> blackList = this.list(new LambdaQueryWrapper<CmsSensitiveWord>().eq(CmsSensitiveWord::getType, SensitiveWordType.BLACK.name()));
		// 敏感词白名单
		List<CmsSensitiveWord> whiteList = this.list(new LambdaQueryWrapper<CmsSensitiveWord>().eq(CmsSensitiveWord::getType, SensitiveWordType.WHITE.name()));
		this.processor.init(blackList.stream().map(CmsSensitiveWord::getWord).collect(Collectors.toSet()), 
				whiteList.stream().map(CmsSensitiveWord::getWord).collect(Collectors.toSet()));
	}
	
	@Override
	public String replaceSensitiveWords(String text, String replacement) {
		if (StringUtils.isBlank(replacement)) {
			replacement = WordConstants.SENSITIVE_WORD_REPLACEMENT;
		}
		return this.processor.replace(text, MatchType.MAX, ReplaceType.WORD, replacement);
	}

	@Override
	public void addWord(CmsSensitiveWord word) {
		word.setWordId(IdUtils.getSnowflakeId());
		word.setCreateTime(LocalDateTime.now());
		this.save(word);
		
		this.processor.addWord(word.getWord(), SensitiveWordType.valueOf(word.getType()));
	}

	@Override
	public void editWord(CmsSensitiveWord word) {
		CmsSensitiveWord dbSensitiveWord = this.getById(word.getWordId());
		Assert.notNull(dbSensitiveWord, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("wordId", word.getWordId()));

		String oldWord = dbSensitiveWord.getWord();
		String oldType = dbSensitiveWord.getType();
		
		dbSensitiveWord.setWord(word.getWord());
		dbSensitiveWord.setReplaceWord(word.getReplaceWord());
		dbSensitiveWord.setType(word.getType());
		dbSensitiveWord.setRemark(word.getRemark());
		dbSensitiveWord.updateBy(word.getUpdateBy());
		this.updateById(word);

		if (!dbSensitiveWord.getWord().equals(oldWord)
				|| !dbSensitiveWord.getType().equals(oldType)) {
			this.processor.removeWord(oldWord);
			this.processor.addWord(dbSensitiveWord.getWord(), SensitiveWordType.valueOf(dbSensitiveWord.getType()));
		}
	}
	
	@Override
	public void deleteWord(List<Long> wordIds) {
		List<CmsSensitiveWord> list = this.listByIds(wordIds);
		this.removeByIds(wordIds);

		Set<String> words = list.stream().map(CmsSensitiveWord::getWord).collect(Collectors.toSet());
		this.processor.removeWords(words);
	}
}
