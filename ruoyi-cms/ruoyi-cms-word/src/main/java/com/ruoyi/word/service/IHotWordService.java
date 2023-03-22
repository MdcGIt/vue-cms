package com.ruoyi.word.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.word.domain.CmsHotWord;

public interface IHotWordService extends IService<CmsHotWord> {

	/**
	 * 缓存热词对象
	 */
	public record HotWord(String word, String url, String target) {
		
	}

	/**
	 * 获取指定分组热词集合
	 * 
	 * @param groupCode
	 * @return
	 */
	Map<String, HotWord> getHotWords(String groupCode);

	/**
	 * 按指定热词分组处理内容中的热词
	 * 
	 * @param text
	 * @param groupCodes
	 * @param target
	 * @param replacementTemplate
	 * @return
	 */
	String replaceHotWords(String text, String[] groupCodes, String target, String replacementTemplate);
}
