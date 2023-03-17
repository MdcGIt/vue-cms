package com.ruoyi.word.service.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.word.WordConstants;
import com.ruoyi.word.domain.CmsHotWord;
import com.ruoyi.word.mapper.CmsHotWordGroupMapper;
import com.ruoyi.word.mapper.CmsHotWordMapper;
import com.ruoyi.word.service.IHotWordService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotWordServiceImpl extends ServiceImpl<CmsHotWordMapper, CmsHotWord> implements IHotWordService {

	private final CmsHotWordGroupMapper hotWordGroupMapper;
	
	@Override
	public String replaceHotWords(String text, Long[] groupIds, String target, String replacementTemplate) {
		List<CmsHotWord> hotWords = this.getHotWords(Arrays.asList(groupIds));
		return this.invoke(text, hotWords, target, replacementTemplate);
	}

	@Override
	public String replaceHotWords(String text, String[] groupCodes, String target, String replacementTemplate) {
		List<CmsHotWord> hotWords = this.getHotWordsByCode(Arrays.asList(groupCodes));
		return this.invoke(text, hotWords, target, replacementTemplate);
	}
	
	private String invoke(String text, List<CmsHotWord> hotWords, String target, String replacementTemplate) {
		if (hotWords != null && hotWords.size() > 0) {
			if (StringUtils.isEmpty(replacementTemplate)) {
				replacementTemplate = WordConstants.HOT_WORD_REPLACEMENT;
			}
			for (CmsHotWord word : hotWords) {
				if (StringUtils.isEmpty(target)) {
					target = word.getUrlTarget();
				}
				String replacement = StringUtils.messageFormat(replacementTemplate, word.getUrl(), word.getWord(), target);
				text = text.replaceAll(word.getWord(), replacement);
			}
		}
		return text;
	}

	private List<CmsHotWord> getHotWords(List<Long> groupIds) {
		return this.list(new LambdaQueryWrapper<CmsHotWord>().in(CmsHotWord::getGroupId, groupIds));
	}

	private List<CmsHotWord> getHotWordsByCode(List<String> groupCodes) {
		List<Long> hotWordIds = this.hotWordGroupMapper.getHotWordIdsByCode(groupCodes);
		return this.getHotWords(hotWordIds);
	}
}
