package com.ruoyi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author ruoyi
 */
@SpringBootApplication
public class RuoYiApplication {
	
	public static void main(String[] args) {
		long s = System.currentTimeMillis();
		System.setProperty("spring.devtools.restart.enabled", "false");
		SpringApplication.run(RuoYiApplication.class, args);
		System.out.println("RuoYiApplication startup, cost: " + (System.currentTimeMillis() - s) + "ms");
	}
}
