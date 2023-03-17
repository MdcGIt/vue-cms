package com.ruoyi.article.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.system.security.SaAdminCheckLogin;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@SaAdminCheckLogin
@RestController
@RequestMapping("/cms/article")
public class ArticleController extends BaseRestController {
	
}

