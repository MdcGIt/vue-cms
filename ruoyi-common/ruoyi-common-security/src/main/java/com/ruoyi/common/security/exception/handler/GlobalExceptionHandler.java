package com.ruoyi.common.security.exception.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.GlobalException;
import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.common.security.exception.SecurityErrorCode;
import com.ruoyi.common.utils.StringUtils;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * 全局异常处理器
 * 
 * @author ruoyi
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/**
	 * 自定义通用异常
	 * 
	 * @param e
	 * @return
	 */
	@ExceptionHandler(GlobalException.class)
	public R<?> handleGlobalException(GlobalException e) {
		if (StringUtils.isNotEmpty(e.getMessage())) {
			return R.fail(e.getMessage());
		}
		e.printStackTrace();
		return R.fail(I18nUtils.get(e.getErrorCode().value(), LocaleContextHolder.getLocale(), e.getErrArgs()));
	}

	/**
	 * 未登录
	 */
	@ExceptionHandler(NotLoginException.class)
	public R<?> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
		log.error("NotLogin[code: {}, type: {}]{}", e.getCode(), e.getType(), e.getMessage());
		return R.fail(HttpStatus.UNAUTHORIZED.value(), I18nUtils.get(SecurityErrorCode.NOT_LOGIN.value()));
	}

	/**
	 * 权限校验异常
	 */
	@ExceptionHandler(NotPermissionException.class)
	public R<?> handleSecurityPermissionException(NotPermissionException e, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.error("请求地址'{}',权限校验失败'{}'", requestURI, e.getMessage());
		return R.fail(HttpStatus.FORBIDDEN.value(), I18nUtils.get(SecurityErrorCode.NOT_PERMISSION.value()));
	}

	/**
	 * 请求方式不支持
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public R<?> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
			HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
		return R.fail(e.getMessage());
	}

	/**
	 * 拦截未知的运行时异常
	 */
	@ExceptionHandler(RuntimeException.class)
	public R<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.error("请求地址'{}',发生未知异常.", requestURI, e);
		return R.fail(e.getMessage());
	}

	/**
	 * 系统异常
	 */
	@ExceptionHandler(Exception.class)
	public R<?> handleException(Exception e, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.error("请求地址'{}',发生系统异常.", requestURI, e);
		return R.fail(e.getMessage());
	}

	/**
	 * 自定义验证异常
	 */
	@ExceptionHandler(BindException.class)
	public R<?> handleBindException(BindException e) {
		log.error(e.getMessage(), e);
		String message = e.getAllErrors().get(0).getDefaultMessage();
		return R.fail(message);
	}

	/**
	 * 自定义验证异常
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.error(e.getMessage(), e);
		String message = e.getBindingResult().getFieldError().getDefaultMessage();
		return R.fail(message);
	}
}
