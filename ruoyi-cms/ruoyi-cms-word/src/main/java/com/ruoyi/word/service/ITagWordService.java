package com.ruoyi.word.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.word.domain.CmsTagWord;

public interface ITagWordService extends IService<CmsTagWord> {

	/**
	 * 添加TAG词
	 * 
	 * @param tagWord
	 * @return
	 */
	void addTagWord(CmsTagWord tagWord);

	/**
	 * 编辑TAG词
	 * 
	 * @param tagWord
	 * @return
	 */
	void editTagWord(CmsTagWord tagWord);

	/**
	 * 删除TAG词
	 * 
	 * @param tagWordIds
	 * @return
	 */
	void deleteTagWords(List<Long> tagWordIds);
}
