package com.ruoyi;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAdminServer
@SpringBootApplication
public class RuoYiMonitorApplication {
	
	public static void main(String[] args) {
		long s = System.currentTimeMillis();
		SpringApplication.run(RuoYiMonitorApplication.class, args);
		System.out.println("RuoYiMonitorApplication startup, cost: " + (System.currentTimeMillis() - s) + "ms");
	}
}
