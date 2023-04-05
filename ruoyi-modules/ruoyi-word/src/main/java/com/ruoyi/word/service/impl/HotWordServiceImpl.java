package com.ruoyi.word.service.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.word.WordConstants;
import com.ruoyi.word.domain.HotWord;
import com.ruoyi.word.domain.HotWordGroup;
import com.ruoyi.word.mapper.HotWordGroupMapper;
import com.ruoyi.word.mapper.HotWordMapper;
import com.ruoyi.word.service.IHotWordService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotWordServiceImpl extends ServiceImpl<HotWordMapper, HotWord> implements IHotWordService {

	private static final String CACHE_PREFIX = "hotword:";

	private final RedisCache redisCache;

	private final HotWordGroupMapper hotWordGroupMapper;

	@Override
	public Map<String, HotWordCache> getHotWords(String groupCode) {
		return this.redisCache.getCacheObject(CACHE_PREFIX + groupCode, () -> {
			Optional<HotWordGroup> groupOpt = new LambdaQueryChainWrapper<>(this.hotWordGroupMapper).eq(HotWordGroup::getCode, groupCode).oneOpt();
			if (!groupOpt.isPresent()) {
				return null;
			}
			return this.lambdaQuery().eq(HotWord::getGroupId, groupOpt.get().getGroupId()).list().stream().collect(
					Collectors.toMap(HotWord::getWord, w -> new HotWordCache(w.getWord(), w.getUrl(), w.getUrlTarget())));
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
			Map<String, HotWordCache> hotWords = this.getHotWords(groupCode);
			for (Iterator<Entry<String, HotWordCache>> iterator = hotWords.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, HotWordCache> e = iterator.next();
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
