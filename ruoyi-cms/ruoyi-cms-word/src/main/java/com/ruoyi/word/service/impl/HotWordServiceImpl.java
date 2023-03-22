package com.ruoyi.word.service.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.word.WordConstants;
import com.ruoyi.word.domain.CmsHotWord;
import com.ruoyi.word.domain.CmsHotWordGroup;
import com.ruoyi.word.mapper.CmsHotWordGroupMapper;
import com.ruoyi.word.mapper.CmsHotWordMapper;
import com.ruoyi.word.service.IHotWordService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotWordServiceImpl extends ServiceImpl<CmsHotWordMapper, CmsHotWord> implements IHotWordService {

	private static final String CACHE_PREFIX = "cms:hot_word:";

	private final RedisCache redisCache;

	private final CmsHotWordGroupMapper hotWordGroupMapper;

	@Override
	public Map<String, HotWord> getHotWords(String groupCode) {
		return this.redisCache.getCacheMap(CACHE_PREFIX + groupCode, () -> {
			CmsHotWordGroup group = this.hotWordGroupMapper
					.selectOne(new LambdaQueryWrapper<CmsHotWordGroup>().eq(CmsHotWordGroup::getCode, groupCode));
			if (group == null) {
				return null;
			}
			return this.lambdaQuery().eq(CmsHotWord::getGroupId, group.getGroupId()).list().stream().collect(
					Collectors.toMap(CmsHotWord::getWord, w -> new HotWord(w.getWord(), w.getUrl(), w.getUrlTarget())));
		});
	}

	@Override
	public String replaceHotWords(String text, String[] groupCodes, String target, String replacementTemplate) {
		if (Objects.isNull(groupCodes) || groupCodes.length == 0) {
			return text;
		}
		if (StringUtils.isEmpty(replacementTemplate)) {
			replacementTemplate = WordConstants.HOT_WORD_REPLACEMENT;
		}
		for (String groupCode : groupCodes) {
			Map<String, HotWord> hotWords = this.getHotWords(groupCode);
			for (Iterator<Entry<String, HotWord>> iterator = hotWords.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, HotWord> e = iterator.next();
				if (StringUtils.isEmpty(target)) {
					target = e.getValue().target();
				}
				String replacement = StringUtils.messageFormat(replacementTemplate, e.getValue().url(),
						e.getValue().word(), target);
				text = StringUtils.replaceEx(text, e.getKey(), replacement);
			}
		}
		return text;
	}
}
