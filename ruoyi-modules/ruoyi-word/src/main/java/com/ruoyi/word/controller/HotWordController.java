package com.ruoyi.word.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.word.domain.HotWord;
import com.ruoyi.word.permission.WordPriv;
import com.ruoyi.word.service.IHotWordService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 *  热词词前端控制器
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
@RequiredArgsConstructor
@RestController
@RequestMapping("/word/hotword")
public class HotWordController extends BaseRestController {
    
	private final IHotWordService hotWordService;
    
    @GetMapping
    public R<?> getPageList(@RequestParam("groupId") @Min(1) Long groupId,
    		@RequestParam(value = "query", required = false) String query) {
    	PageRequest pr = this.getPageRequest();
    	Page<HotWord> page = this.hotWordService.lambdaQuery().eq(HotWord::getGroupId, groupId)
				.like(StringUtils.isNotEmpty(query), HotWord::getWord, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
    	return this.bindDataTable(page);
    }

	@PostMapping
	public R<?> add(@RequestBody HotWord hotWord) {
		boolean checkUnique = checkUnique(hotWord.getGroupId(), null, hotWord.getWord());
		Assert.isTrue(checkUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("word: " + hotWord.getWord()));
		
    	hotWord.setWordId(IdUtils.getSnowflakeId());
    	hotWord.createBy(StpAdminUtil.getLoginUser().getUsername());
    	this.hotWordService.save(hotWord);
    	return R.ok();
	}

	@PutMapping
	public R<String> edit(@RequestBody HotWord hotWord) {
		HotWord dbHotWord = this.hotWordService.getById(hotWord.getWordId());
		Assert.notNull(dbHotWord, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("wordId", hotWord.getWordId()));
		boolean checkUnique = checkUnique(hotWord.getGroupId(), hotWord.getWordId(), hotWord.getWord());
		Assert.isTrue(checkUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("word: " + hotWord.getWord()));

		dbHotWord.setWord(hotWord.getWord());
		dbHotWord.setUrl(hotWord.getUrl());
		dbHotWord.setUrlTarget(hotWord.getUrlTarget());
		dbHotWord.setRemark(hotWord.getRemark());
		dbHotWord.updateBy(StpAdminUtil.getLoginUser().getUsername());
		this.hotWordService.updateById(hotWord);
    	return R.ok();
	}
	
	private boolean checkUnique(Long groupId, Long wordId, String word) {
    	return this.hotWordService.lambdaQuery()
    			.eq(HotWord::getGroupId, groupId)
    			.ne(wordId != null && wordId > 0, HotWord::getWordId, wordId)
    			.eq(HotWord::getWord, word)
    			.count() == 0;
	}

	@DeleteMapping
	public R<String> remove(@RequestBody @NotEmpty List<Long> hotWordIds) {
		this.hotWordService.removeByIds(hotWordIds);
		return R.ok();
	}
}

