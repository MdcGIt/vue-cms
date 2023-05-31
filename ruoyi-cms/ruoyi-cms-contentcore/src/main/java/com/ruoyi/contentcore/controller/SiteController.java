package com.ruoyi.contentcore.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.io.Files;
import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileExUtils;
import com.ruoyi.contentcore.core.IProperty.UseType;
import com.ruoyi.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.PublishPipeProp;
import com.ruoyi.contentcore.domain.dto.PublishSiteDTO;
import com.ruoyi.contentcore.domain.dto.SiteDTO;
import com.ruoyi.contentcore.domain.dto.SiteDefaultTemplateDTO;
import com.ruoyi.contentcore.perms.ContentCorePriv;
import com.ruoyi.contentcore.perms.SitePermissionType.SitePrivItem;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IPublishPipeService;
import com.ruoyi.contentcore.service.IPublishService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.validator.LongId;
import freemarker.template.TemplateException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 站点管理
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@RestController
@RequestMapping("/cms/site")
@RequiredArgsConstructor
public class SiteController extends BaseRestController {

    private final ISiteService siteService;

    private final ICatalogService catalogService;

    private final IPublishPipeService publishPipeService;

    private final IPublishService publishService;

    private final AsyncTaskManager asyncTaskManager;

    /**
     * 获取当前站点数据
     *
     * @return
     * @apiNote 读取request.header['CurrentSite']中的siteId，如果无header或无站点则取数据库第一条站点数据
     */
    @SaAdminCheckLogin
    @GetMapping("/getCurrentSite")
    public R<Map<String, Object>> getCurrentSite() {
        CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest(), StpAdminUtil.getLoginUser());
        return R.ok(Map.of("siteId", site.getSiteId(), "siteName", site.getName()));
    }

    /**
     * 设置当前站点
     *
     * @param siteId 站点ID
     * @return
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:View:${#siteId}")
    @Log(title = "切换站点", businessType = BusinessType.UPDATE)
    @PostMapping("/setCurrentSite/{siteId}")
    public R<Map<String, Object>> setCurrentSite(@PathVariable("siteId") @LongId Long siteId) {
        CmsSite site = this.siteService.getSite(siteId);
        return R.ok(Map.of("siteId", site.getSiteId(), "siteName", site.getName()));
    }

    /**
     * 查询站点数据列表
     *
     * @param siteName 站点名称
     * @return
     */
    @Priv(type = AdminUserType.TYPE, value = ContentCorePriv.SiteView)
    @GetMapping("/list")
    public R<?> list(@RequestParam(value = "siteName", required = false) String siteName) {
        PageRequest pr = this.getPageRequest();
        LambdaQueryWrapper<CmsSite> q = new LambdaQueryWrapper<CmsSite>().like(StringUtils.isNotEmpty(siteName),
                CmsSite::getName, siteName);
        Page<CmsSite> page = siteService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
        LoginUser loginUser = StpAdminUtil.getLoginUser();
        List<CmsSite> list = page.getRecords().stream()
                .filter(site -> loginUser.hasPermission(SitePrivItem.View.getPermissionKey(site.getSiteId())))
                .toList();
        list.forEach(site -> {
            if (StringUtils.isNotEmpty(site.getLogo())) {
                site.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(site.getLogo()));
            }
        });
        return this.bindDataTable(list, page.getTotal());
    }

    /**
     * 获取站点详情
     *
     * @param siteId 站点ID
     * @return
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:View:${#siteId}")
    @GetMapping(value = "/{siteId}")
    public R<?> getInfo(@PathVariable @LongId Long siteId) {
        CmsSite site = siteService.getById(siteId);
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

        if (StringUtils.isNotEmpty(site.getLogo())) {
            site.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(site.getLogo()));
        }
        SiteDTO dto = SiteDTO.newInstance(site);
        // 发布通道数据
        List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(site.getSiteId(),
                PublishPipePropUseType.Site, site.getPublishPipeProps());
        dto.setPublishPipeDatas(publishPipeProps);
        return R.ok(dto);
    }

    @SaAdminCheckLogin
    @GetMapping("/options")
    public R<?> getSiteOptions() {
        LoginUser loginUser = StpAdminUtil.getLoginUser();
        List<Map<String, Object>> list = this.siteService.lambdaQuery().select(CmsSite::getSiteId, CmsSite::getName)
                .list().stream()
                .filter(site -> loginUser.hasPermission(SitePrivItem.View.getPermissionKey(site.getSiteId())))
                .map(site -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", site.getSiteId());
                    map.put("name", site.getName());
                    return map;
                }).toList();
        return this.bindDataTable(list);
    }

    /**
     * 新增站点数据
     *
     * @param dto
     * @return
     * @throws IOException
     */
    @Priv(type = AdminUserType.TYPE, value = ContentCorePriv.SiteView)
    @Log(title = "新增站点", businessType = BusinessType.INSERT)
    @PostMapping
    public R<?> addSave(@RequestBody @Validated SiteDTO dto) throws IOException {
        dto.setOperator(StpAdminUtil.getLoginUser());
        CmsSite site = this.siteService.addSite(dto);
        return R.ok(site);
    }

    /**
     * 修改站点数据
     *
     * @param dto
     * @return
     * @throws IOException
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#dto.siteId}")
    @Log(title = "编辑站点", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<?> editSave(@RequestBody @Validated SiteDTO dto) throws IOException {
        dto.setOperator(StpAdminUtil.getLoginUser());
        this.siteService.saveSite(dto);
        return R.ok();
    }

    /**
     * 删除站点数据
     *
     * @param siteId 站点ID
     * @return
     * @throws IOException
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:Delete:${#siteId}")
    @Log(title = "删除站点", businessType = BusinessType.DELETE)
    @DeleteMapping("/{siteId}")
    public R<String> remove(@PathVariable("siteId") @LongId Long siteId) throws IOException {
        CmsSite site = siteService.getById(siteId);
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

        AsyncTask task = new AsyncTask() {

            @Override
            public void run0() throws Exception {
                siteService.deleteSite(siteId);
            }
        };
        task.setTaskId("DeleteSite_" + siteId);
        this.asyncTaskManager.execute(task);
        return R.ok(task.getTaskId());
    }

    /**
     * 发布站点
     *
     * @param dto
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:Publish:${#dto.siteId}")
    @Log(title = "发布站点", businessType = BusinessType.OTHER)
    @PostMapping("/publish")
    public R<String> publishAll(@RequestBody @Validated PublishSiteDTO dto) throws IOException, TemplateException {
        CmsSite site = siteService.getById(dto.getSiteId());
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", dto.getSiteId()));

        if (!dto.isPublishIndex()) {
            AsyncTask task = publishService.publishAll(site, dto.getContentStatus());
            return R.ok(task.getTaskId());
        }
        publishService.publishSiteIndex(site);
        return R.ok();
    }

    /**
     * 获取站点扩展配置数据
     *
     * @param siteId 站点ID
     * @return
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:View:${#siteId}")
    @GetMapping("/extends")
    public R<?> getSiteExtends(@RequestParam("siteId") @LongId Long siteId) {
        CmsSite site = this.siteService.getSite(siteId);
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

        Map<String, Object> configProps = ConfigPropertyUtils.paseConfigProps(site.getConfigProps(), UseType.Site);
        configProps.put("PreviewPrefix", SiteUtils.getResourcePrefix(site));
        return R.ok(configProps);
    }

    /**
     * 保存站点扩展配置数据
     *
     * @param siteId  站点ID
     * @param configs 扩展配置数据
     * @return
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#siteId}")
    @Log(title = "站点扩展", businessType = BusinessType.UPDATE, isSaveRequestData = false)
    @PostMapping("/extends/{siteId}")
    public R<?> saveSiteExtends(@PathVariable("siteId") @LongId Long siteId, @RequestBody Map<String, String> configs) {
        CmsSite site = this.siteService.getSite(siteId);
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

        this.siteService.saveSiteExtend(site, configs, StpAdminUtil.getLoginUser().getUsername());
        return R.ok();
    }

    /**
     * 获取站点默认模板配置
     *
     * @param siteId 站点ID
     * @return
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:View:${#siteId}")
    @GetMapping("/default_template")
    public R<?> getDefaultTemplates(@RequestParam("siteId") @LongId Long siteId) {
        CmsSite site = this.siteService.getSite(siteId);
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

        SiteDefaultTemplateDTO dto = new SiteDefaultTemplateDTO();
        dto.setSiteId(siteId);
        // 发布通道数据
        List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(site.getSiteId(),
                PublishPipePropUseType.Site, site.getPublishPipeProps());
        dto.setPublishPipeProps(publishPipeProps);
        return R.ok(dto);
    }

    /**
     * 保存站点默认模板配置
     *
     * @param dto
     * @return
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#dto.siteId}")
    @Log(title = "默认模板", businessType = BusinessType.UPDATE)
    @PostMapping("/default_template")
    public R<?> saveDefaultTemplates(@RequestBody @Validated SiteDefaultTemplateDTO dto) {
        CmsSite site = this.siteService.getSite(dto.getSiteId());
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", dto.getSiteId()));

        dto.setOperator(StpAdminUtil.getLoginUser());
        this.siteService.saveSiteDefaultTemplate(dto);
        return R.ok();
    }

    /**
     * 应用站点默认模板配置到指定栏目
     *
     * @param dto
     * @return
     */
    @SaAdminCheckLogin
    @Log(title = "应用默认模板", businessType = BusinessType.UPDATE)
    @PostMapping("/apply_default_template")
    public R<?> applyDefaultTemplateToCatalog(@RequestBody @Validated SiteDefaultTemplateDTO dto) {
        Assert.isTrue(IdUtils.validate(dto.getToCatalogIds()), () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("toCatalogIds"));

        CmsSite site = this.siteService.getSite(dto.getSiteId());
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", dto.getSiteId()));

        dto.setOperator(StpAdminUtil.getLoginUser());
        this.catalogService.applySiteDefaultTemplateToCatalog(dto);
        return R.ok();
    }

    /**
     * 上传水印图片
     *
     * @param siteId        站点ID
     * @param multipartFile 上传文件
     * @return
     * @throws Exception
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#siteId}")
    @Log(title = "上传水印图", businessType = BusinessType.UPDATE)
    @PostMapping("/upload_watermarkimage")
    public R<?> uploadFile(@RequestParam("siteId") @LongId Long siteId,
                           @RequestParam("file") @NotNull MultipartFile multipartFile) throws Exception {
        try {
            CmsSite site = this.siteService.getSite(siteId);
            Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

            String dir = SiteUtils.getSiteResourceRoot(site.getPath());
            String suffix = FileExUtils.getExtension(multipartFile.getOriginalFilename());
            String path = "watermaker" + StringUtils.DOT + suffix;
            File file = new File(dir + path);
            Files.write(multipartFile.getBytes(), file);
            String src = SiteUtils.getResourcePrefix(site) + path;
            return R.ok(Map.of("path", path, "src", src));
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }
}