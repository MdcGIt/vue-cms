package com.ruoyi.contentcore.controller.front;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.vo.ContentDynamicDataVO;
import com.ruoyi.contentcore.domain.vo.ContentVO;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.impl.ContentDynamicDataService;
import com.ruoyi.system.validator.LongId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 内容数据API接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cms/content")
public class ContentApiController extends BaseRestController {

	private final ContentDynamicDataService contentDynamicDataService;

	/**
	 * 内容动态数据，评论数、点赞数、收藏数、浏览数
	 *
	 * @param contentId
	 * @return
	 */
	@GetMapping("/data")
	public R<ContentDynamicDataVO> getContentFavorites(@RequestBody @LongId Long contentId) {
		ContentDynamicDataVO data = this.contentDynamicDataService.getContentDynamicData(contentId);
		if (data == null) {
			return R.fail();
		}
		return R.ok(data);
	}

	/**
	 * 按浏览量排序获取数据
	 */
	public R<ContentVO> getHotContentList() {

		return R.ok();
	}
}
