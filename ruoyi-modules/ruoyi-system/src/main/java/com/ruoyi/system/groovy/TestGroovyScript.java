package com.ruoyi.system.groovy;

import java.io.PrintWriter;

import org.springframework.beans.factory.annotation.Autowired;

import com.ruoyi.common.redis.RedisCache;

public class TestGroovyScript extends BaseGroovyScript {

	@Autowired
	private RedisCache redisCache;
	
	@Override
	protected void run(PrintWriter out) {
		out.println(redisCache.hasKey("adv:stat-view:2023040414"));
	}
}