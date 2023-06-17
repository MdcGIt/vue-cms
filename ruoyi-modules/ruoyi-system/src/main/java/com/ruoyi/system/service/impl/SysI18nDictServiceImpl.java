package com.ruoyi.system.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.I18nMessageSource;
import com.ruoyi.system.domain.SysI18nDict;
import com.ruoyi.system.mapper.SysI18nDictMapper;
import com.ruoyi.system.service.ISysI18nDictService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysI18nDictServiceImpl extends ServiceImpl<SysI18nDictMapper, SysI18nDict> implements ISysI18nDictService {

    private final static String CACHE_PREFIX = "i18n:";

    private final RedisCache redisCache;

    @Override
    public String getLangValue(String languageTag, String langKey) {
        return redisCache.getCacheMapValue(CACHE_PREFIX + languageTag, langKey);
    }

    @Override
    public List<SysI18nDict> listByLangKey(String langKey) {
        return this.list(new LambdaQueryWrapper<SysI18nDict>().eq(SysI18nDict::getLangKey, langKey));
    }

    @Override
    public void insertI18nDict(SysI18nDict dict) {
        Assert.isTrue(this.checkUnique(dict), () -> CommonErrorCode.DATA_CONFLICT.exception(dict.getLangTag() + ":" + dict.getLangKey()));

        dict.setDictId(IdUtils.getSnowflakeId());
        this.save(dict);
        redisCache.setCacheMapValue(CACHE_PREFIX + dict.getLangTag(), dict.getLangKey(), dict.getLangValue());
    }

    @Override
    public void updateI18nDict(SysI18nDict dict) {
        SysI18nDict db = this.getById(dict.getDictId());
        Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("dictId", dict.getDictId()));
        Assert.isTrue(this.checkUnique(dict), () -> CommonErrorCode.DATA_CONFLICT.exception(dict.getLangTag() + ":" + dict.getLangKey()));

        this.updateById(dict);
        redisCache.setCacheMapValue(CACHE_PREFIX + dict.getLangTag(), dict.getLangKey(), dict.getLangValue());
    }

    @Override
    public void deleteI18nDictByIds(List<Long> dictIds) {
        List<SysI18nDict> list = this.listByIds(dictIds);
        this.removeBatchByIds(list);

        list.forEach(dict -> {
            redisCache.deleteCacheMapValue(CACHE_PREFIX + dict.getLangTag(), dict.getLangKey());
        });
    }

    private boolean checkUnique(SysI18nDict dict) {
        long count = this.count(new LambdaQueryWrapper<SysI18nDict>()
                .eq(SysI18nDict::getLangTag, dict.getLangTag())
                .eq(SysI18nDict::getLangKey, dict.getLangKey())
                .ne(IdUtils.validate(dict.getDictId()), SysI18nDict::getDictId, dict.getDictId()));
        return count == 0;
    }

    @Override
    public void batchSaveI18nDicts(List<SysI18nDict> dicts) {
        if (CollectionUtils.isEmpty(dicts)) {
            return;
        }
        for (SysI18nDict dict : dicts) {
            boolean checkUnique = this.checkUnique(dict);
            Assert.isTrue(checkUnique, () -> CommonErrorCode.DATA_CONFLICT.exception(dict.getLangTag() + ":" + dict.getLangKey()));
            if (IdUtils.validate(dict.getDictId())) {
                dict.setDictId(IdUtils.getSnowflakeId());
            }
        }
        this.saveOrUpdateBatch(dicts);

        dicts.forEach(dict -> {
            redisCache.setCacheMapValue(CACHE_PREFIX + dict.getLangTag(), dict.getLangKey(), dict.getLangValue());
        });
    }

    @Override
    public void loadMessages(I18nMessageSource messageSource) throws IOException {
        long s = System.currentTimeMillis();
        // 读取配置文件数据
        this.loadMessagesFromResources(messageSource);
        // 加载数据库数据，如果与配置文件重复则直接覆盖掉
        this.loadMessagesFromDB();
        log.debug("Load i18n messages cost: {}ms", System.currentTimeMillis() - s);
    }

    private void loadMessagesFromDB() {
        Map<String, Map<String, String>> map = this.list().stream().collect(Collectors.groupingBy(
                SysI18nDict::getLangTag, Collectors.toMap(SysI18nDict::getLangKey, SysI18nDict::getLangValue)));
        map.entrySet().forEach(e -> {
            e.getValue().entrySet().forEach(kv -> {
                redisCache.setCacheMapValue(CACHE_PREFIX + e.getKey(), kv.getKey(), kv.getValue());
            });
        });
    }

    private void loadMessagesFromResources(I18nMessageSource messageSource) throws IOException {
        if (StringUtils.isEmpty(messageSource.getBasename())) {
            return;
        }
        String target = messageSource.getBasename().replace('.', '/');
        Resource[] resources = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader())
                .getResources("classpath*:" + target + "*.properties");
        for (Resource resource : resources) {
            String langTag = messageSource.getDefaultLocale().toLanguageTag();
            if (resource.getFilename().indexOf("_") > 0) {
                langTag = resource.getFilename().substring(resource.getFilename().indexOf("_") + 1,
                        resource.getFilename().lastIndexOf("."));
                langTag = StringUtils.replace(langTag, "_", "-");
            }
            try (InputStream is = resource.getInputStream()) {
                List<String> lines = IOUtils.readLines(is, messageSource.getEncoding());
                Map<String, String> map = lines.stream()
                        .filter(s -> StringUtils.isNotEmpty(s) && s.indexOf("=") > 0)
                        .collect(Collectors.toMap(
                                s -> StringUtils.substringBefore(s, "="),
                                s -> StringUtils.substringAfter(s, "=")
                        ));
                redisCache.setCacheMap(CACHE_PREFIX + langTag, map);
            }
        }
    }
}