package com.ruoyi.word.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.word.domain.CmsSensitiveWord;

public interface ISensitiveWordService extends IService<CmsSensitiveWord> {
	
	/**
	 * 替换敏感词
	 * 
	 * @param text
	 * @param replaceStr
	 * @return
	 */
	String replaceSensitiveWords(String text, String replacement);

	/**
	 * 添加敏感词
	 * 
	 * @param word
	 * @return
	 */
	void addWord(CmsSensitiveWord word);

	/**
	 * 编辑敏感词
	 * 
	 * @param sensitiveWord
	 * @return
	 */
	void editWord(CmsSensitiveWord sensitiveWord);

	/**
	 * 删除敏感词
	 * 
	 * @param wordIds
	 * @return
	 */
	void deleteWord(List<Long> wordIds);

}
