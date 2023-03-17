package com.ruoyi.word.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.SortUtils;
import com.ruoyi.word.domain.CmsTagWord;
import com.ruoyi.word.mapper.CmsTagWordMapper;
import com.ruoyi.word.service.ITagWordService;

@Service
public class TagWordServiceImpl extends ServiceImpl<CmsTagWordMapper, CmsTagWord> implements ITagWordService {

	@Override
	public void addTagWord(CmsTagWord tagWord) {
		boolean checkUnique = checkUnique(tagWord.getGroupId(), null, tagWord.getWord());
		Assert.isTrue(checkUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("word"));
		
    	tagWord.setWordId(IdUtils.getSnowflakeId());
    	tagWord.setSortFlag(SortUtils.getDefaultSortValue());
    	this.save(tagWord);
	}

	@Override
	public void editTagWord(CmsTagWord tagWord) {
		CmsTagWord dbTagWord = this.getById(tagWord.getWordId());
		Assert.notNull(dbTagWord, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("wordId", tagWord.getWordId()));

		boolean checkUnique = checkUnique(tagWord.getGroupId(), tagWord.getWordId(), tagWord.getWord());
		Assert.isTrue(checkUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("word"));

		dbTagWord.setGroupId(tagWord.getGroupId());
		dbTagWord.setWord(tagWord.getWord());
		dbTagWord.setLogo(tagWord.getLogo());
		dbTagWord.setRemark(tagWord.getRemark());
		dbTagWord.updateBy(tagWord.getUpdateBy());
		this.updateById(tagWord);
    }
	
	@Override
	@Transactional
	public void deleteTagWords(List<Long> tagWordIds) {
		this.removeByIds(tagWordIds);
	}
	
	private boolean checkUnique(Long groupId, Long wordId, String word) {
		return this.lambdaQuery().eq(CmsTagWord::getGroupId, groupId)
    			.ne(wordId != null && wordId > 0, CmsTagWord::getWordId, wordId)
    			.eq(CmsTagWord::getWord, word)
    			.count() == 0;
	}
}
