package com.ruoyi.contentcore.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.contentcore.domain.CmsPublishPipe;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.PublishPipeProp;
import com.ruoyi.contentcore.perms.ContentCorePriv;
import com.ruoyi.contentcore.service.IPublishPipeService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.system.fixed.dict.EnableOrDisable;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.validator.LongId;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * 发布点管理
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Priv(type = AdminUserType.TYPE, value = ContentCorePriv.PublishPipeView)
@RestController
@RequestMapping("/cms/publishpipe")
@RequiredArgsConstructor
public class PublishPipeController extends BaseRestController {

    private final IPublishPipeService publishPipeService;
    
	private final ISiteService siteService;
	
	/**
	 * 获取当前站点发布通道选择器数据
	 * 
	 * @return
	 */
    @GetMapping("/selectData")
    public R<?> bindSelectData() {
    	CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
    	List<PublishPipeProp> datalist = this.publishPipeService.getPublishPipes(site.getSiteId())
    		.stream().map(p -> PublishPipeProp.newInstance(p.getCode(), p.getName(), null))
    		.collect(Collectors.toList());
    	return this.bindDataTable(datalist);
    }

    /**
     * 查询当前站点发布通道数据列表
     * 
     * @return
     */
    @GetMapping("/list")
    public R<?> list() {
	    PageRequest pr = this.getPageRequest();
    	CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		Page<CmsPublishPipe> page = publishPipeService.page(new Page<CmsPublishPipe>(pr.getPageNumber(), pr.getPageSize(), true)
				, new LambdaQueryWrapper<CmsPublishPipe>().eq(CmsPublishPipe::getSiteId, site.getSiteId()).orderByAsc(CmsPublishPipe::getSort));
        return this.bindDataTable(page);
    }
    
    /**
     * 获取发布通道详情
     * 
     * @param publishPipeId 发布通道ID
     * @return
     */
    @GetMapping(value = "/{publishPipeId}")
    public R<?> getInfo(@PathVariable @LongId Long publishPipeId) {
        CmsPublishPipe publishPipe = publishPipeService.getById(publishPipeId);
        Assert.notNull(publishPipe, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("publishPipeId", publishPipeId));
        return R.ok(publishPipe);
    }

    /**
     * 新增发布通道数据
     * 
     * @param publishPipe
     * @return
     * @throws IOException
     */
	@Log(title = "新增发布通道", businessType = BusinessType.INSERT)
    @PostMapping
    public R<?> addSave(@RequestBody @Validated CmsPublishPipe publishPipe) throws IOException {
    	CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
    	publishPipe.setSiteId(site.getSiteId());
    	publishPipe.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
        this.publishPipeService.addPublishPipe(publishPipe);
        return R.ok();
    }

    /**
     * 修改发布通道数据
     * 
     * @param publishPipe
     * @return
     * @throws IOException
     */
	@Log(title = "编辑发布通道", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<?> editSave(@RequestBody @Validated CmsPublishPipe publishPipe) throws IOException {
    	publishPipe.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
        this.publishPipeService.savePublishPipe(publishPipe);
        return R.ok();
    }

    /**
     * 删除发布通道数据
     * 
     * @param publishPipeIds 发布通道IDs
     * @return
     * @throws IOException
     */
	@Log(title = "删除发布通道", businessType = BusinessType.DELETE)
    @DeleteMapping
    public R<String> remove(@RequestBody @NotEmpty List<Long> publishPipeIds) throws IOException {
    	this.publishPipeService.deletePublishPipe(publishPipeIds);
    	return R.ok();
    }

    /**
     * 启用发布通道
     * 
     * @param publishPipeId 发布通道ID
     * @return
     * @throws IOException
     */
	@Log(title = "启用发布通道", businessType = BusinessType.UPDATE)
    @PostMapping("/enable/{publishPipeId}")
    public R<String> enable(@PathVariable("publishPipeId") @LongId Long publishPipeId) throws IOException {
    	CmsPublishPipe publishPipe = this.publishPipeService.getById(publishPipeId);
    	Assert.notNull(publishPipe, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("publishPipeId", publishPipe));

    	publishPipe.setState(EnableOrDisable.ENABLE);
    	publishPipe.updateBy(StpAdminUtil.getLoginUser().getUsername());
    	this.publishPipeService.savePublishPipe(publishPipe);
    	return R.ok();
    }

    /**
     * 禁用发布通道
     * 
     * @param publishPipeId 发布通道ID
     * @return
     * @throws IOException
     */
	@Log(title = "禁用发布通道", businessType = BusinessType.UPDATE)
    @PostMapping("/disable/{publishPipeId}")
    public R<String> disable(@PathVariable("publishPipeId") Long publishPipeId) throws IOException {
    	CmsPublishPipe publishPipe = this.publishPipeService.getById(publishPipeId);
    	if (publishPipe == null) {
    		return R.fail("数据ID错误：" + publishPipeId);
    	}
    	publishPipe.setState(EnableOrDisable.DISABLE);
    	publishPipe.updateBy(StpAdminUtil.getLoginUser().getUsername());
    	this.publishPipeService.savePublishPipe(publishPipe);
    	return R.ok();
    }
}
