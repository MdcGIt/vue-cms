package com.ruoyi.common.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ruoyi.common.security.IUserType;
import com.ruoyi.common.security.SecurityService;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.exception.SecurityErrorCode;
import com.ruoyi.common.utils.Assert;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.annotation.SaCheckBasic;
import cn.dev33.satoken.annotation.SaCheckDisable;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaCheckSafe;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.basic.SaBasicUtil;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.strategy.SaStrategy;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

	private final SecurityService securityService;

	/**
	 * 自定义拦截规则
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// SaToken
		registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
	}
	
	@Autowired
    public void rewriteCheckElementAnnotationSaStrategy() {
		// 重写Sa-Token的注解处理器，增加注解合并功能 
        SaStrategy.me.getAnnotation = (element, annotationClass) -> {
            return AnnotatedElementUtils.getMergedAnnotation(element, annotationClass); 
        };
        // 重写Sa-Token对一个 [元素] 对象进行注解校验
        SaStrategy.me.checkElementAnnotation = (target) -> {
    		// 校验 @Priv 注解 
        	Priv priv = (Priv) SaStrategy.me.getAnnotation.apply(target, Priv.class);
    		if(priv != null) {
    			IUserType ut = securityService.getUserType(priv.type());
    			Assert.notNull(ut, () -> SecurityErrorCode.UNKNOWN_USER_TYPE.exception(priv.type()));
    			
    			SaManager.getStpLogic(priv.type(), false).checkLogin();
    			if (priv.value().length > 0) {
    				StpLogic stpLogic = SaManager.getStpLogic(priv.type(), false);
					if(priv.mode() == SaMode.AND) {
						stpLogic.checkPermissionAnd(priv.value());	
					} else {
						stpLogic.checkPermissionOr(priv.value());	
					}
    			}
    		} else {
        		// 校验 @SaCheckLogin 注解 
        		SaCheckLogin checkLogin = (SaCheckLogin) SaStrategy.me.getAnnotation.apply(target, SaCheckLogin.class);
        		if(checkLogin != null) {
        			SaManager.getStpLogic(checkLogin.type(), false).checkByAnnotation(checkLogin);
        		}
        		
        		// 校验 @SaCheckRole 注解 
        		SaCheckRole checkRole = (SaCheckRole) SaStrategy.me.getAnnotation.apply(target, SaCheckRole.class);
        		if(checkRole != null) {
        			SaManager.getStpLogic(checkRole.type(), false).checkByAnnotation(checkRole);
        		}
        		
        		// 校验 @SaCheckPermission 注解
        		SaCheckPermission checkPermission = (SaCheckPermission) SaStrategy.me.getAnnotation.apply(target, SaCheckPermission.class);
        		if(checkPermission != null) {
        			SaManager.getStpLogic(checkPermission.type(), false).checkByAnnotation(checkPermission);
        		}
    		}
    		// 校验 @SaCheckSafe 注解
    		SaCheckSafe checkSafe = (SaCheckSafe) SaStrategy.me.getAnnotation.apply(target, SaCheckSafe.class);
    		if(checkSafe != null) {
    			SaManager.getStpLogic(checkSafe.type(), false).checkByAnnotation(checkSafe);
    		}

    		// 校验 @SaCheckDisable 注解
    		SaCheckDisable checkDisable = (SaCheckDisable) SaStrategy.me.getAnnotation.apply(target, SaCheckDisable.class);
    		if(checkDisable != null) {
    			SaManager.getStpLogic(checkDisable.type(), false).checkByAnnotation(checkDisable);
    		}
    		
    		// 校验 @SaCheckBasic 注解
    		SaCheckBasic checkBasic = (SaCheckBasic) SaStrategy.me.getAnnotation.apply(target, SaCheckBasic.class);
    		if(checkBasic != null) {
    			SaBasicUtil.check(checkBasic.realm(), checkBasic.account());
    		}
    	};
    }

	/**
	 * 跨域配置
	 */
	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		// 设置访问源地址
		config.addAllowedOriginPattern("*");
		// 设置访问源请求头
		config.addAllowedHeader("*");
		// 设置访问源请求方法
		config.addAllowedMethod("*");
		// 有效期 1800秒
		config.setMaxAge(1800L);
		// 添加映射路径，拦截一切请求
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		// 返回新的CorsFilter
		return new CorsFilter(source);
	}
}