package com.ruoyi.system.controller.common;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.system.groovy.BaseGroovyScript;
import com.ruoyi.system.groovy.SpringBoot3GlueFactory;
import com.ruoyi.system.security.AdminUserType;
import com.xxl.job.core.handler.IJobHandler;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Groovy脚本执行控制器
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/groovy")
public class GroovyController {
	
	@Priv(type = AdminUserType.TYPE, value = "sys:groovy:exec")
	@PostMapping("/exec")
	public R<?> execGroovyScript(@RequestBody ScriptBody scriptBody) throws Exception {
		IJobHandler jobHandler = SpringBoot3GlueFactory.getInstance().loadNewInstance(scriptBody.getScriptText());
		if (jobHandler instanceof BaseGroovyScript script) {
			StringWriter writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			script.setPrintWriter(printWriter);
			script.execute();
			return R.ok(writer.toString());
		}
		return R.fail("The script class must extends com.ruoyi.system.groovy.BaseGroovyScript.");
	}
	
	@Getter
	@Setter
	static class ScriptBody {
		
		@NotEmpty
		private String scriptText;
	}
}
