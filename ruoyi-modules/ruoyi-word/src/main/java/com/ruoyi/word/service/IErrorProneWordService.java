package com.ruoyi.word.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.word.domain.ErrorProneWord;

public interface IErrorProneWordService extends IService<ErrorProneWord> {

	/**
	 * 查找置顶文本中的易错词
	 * 
	 * @param text
	 * @return
	 */
	Map<String, String> findErrorProneWords(String text);

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

	/**
	 * 添加易错词
	 * 
	 * @param errorProneWord
	 */
	void addErrorProneWord(ErrorProneWord errorProneWord);

	/**
	 * 修改易错词
	 * 
	 * @param errorProneWord
	 */
	void updateErrorProneWord(ErrorProneWord errorProneWord);
}