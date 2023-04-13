package com.ruoyi.system.controller.common;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.system.groovy.BaseGroovyScript;
import com.ruoyi.system.groovy.GroovyScriptFactory;
import com.ruoyi.system.permission.SysMenuPriv;
import com.ruoyi.system.security.AdminUserType;

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

	@Log(title = "执行Groovy脚本", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.GroovyExec)
	@PostMapping("/exec")
	public R<?> execGroovyScript(@RequestBody ScriptBody scriptBody) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		try {
			BaseGroovyScript script = GroovyScriptFactory.getInstance().loadNewInstance(scriptBody.getScriptText());
			script.setPrintWriter(printWriter);
			script.run();
		} catch (Exception e) {
			e.printStackTrace(printWriter);
		}
		return R.ok(writer.toString());
	}
	
	@Getter
	@Setter
	static class ScriptBody {
		
		@NotEmpty
		private String scriptText;
	}
}
