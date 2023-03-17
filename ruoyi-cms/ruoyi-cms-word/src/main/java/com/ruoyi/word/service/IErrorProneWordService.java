package com.ruoyi.word.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.word.domain.CmsErrorProneWord;

public interface IErrorProneWordService extends IService<CmsErrorProneWord> {

	/**
	 * 易错词替换
	 * 
	 * @param str
	 * @return
	 */
	String replaceErrorProneWords(String text);

	/**
	 * 易错词集合
	 * 
	 * @return
	 */
	Map<String, String> getErrorProneWords();
}
