package com.ruoyi;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JasyptTest {

	/**
	 * 注入加密方法
	 */
	@Autowired
	private StringEncryptor encryptor;

	/**
	 * 手动生成密文，此处演示了url，user，password
	 */
	@Test
	public void encrypt() {
		
		String url = encryptor.encrypt(
				"jdbc:mysql://localhost:33066/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8");
		String name = encryptor.encrypt("root");
		String password = encryptor.encrypt("asdasd");
		System.out.println("database url: " + url);
		System.out.println("database name: " + name);
		System.out.println("database password: " + password);
	}
}
