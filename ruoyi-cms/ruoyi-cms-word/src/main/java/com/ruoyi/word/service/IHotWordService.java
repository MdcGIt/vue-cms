package com.ruoyi.word.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.word.domain.CmsHotWord;

public interface IHotWordService extends IService<CmsHotWord> {

	/**
	 * 按指定热词分组处理内容中的热词
	 * 
	 * @param text
	 * @param groupIds
	 * @param target
	 * @param replacementTemplate
	 * @return
	 */
	String replaceHotWords(String text, Long[] groupIds, String target, String replacementTemplate);

	/**
	 * 按指定热词分组处理内容中的热词
	 * 
	 * @param text
	 * @param groupIds
	 * @param target
	 * @param replacementTemplate
	 * @return
	 */
	String replaceHotWords(String text, String[] groupCodes, String target, String replacementTemplate);
}
