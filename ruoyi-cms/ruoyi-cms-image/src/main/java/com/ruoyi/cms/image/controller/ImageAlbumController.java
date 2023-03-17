package com.ruoyi.cms.image.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.system.security.SaAdminCheckLogin;

/**
 * <p>
 *  图集内容前端控制器
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@SaAdminCheckLogin
@RestController
@RequestMapping("/cms/image_album")
public class ImageAlbumController extends BaseRestController {

}

