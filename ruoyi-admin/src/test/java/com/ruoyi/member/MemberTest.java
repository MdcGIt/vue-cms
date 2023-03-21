package com.ruoyi.member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ruoyi.member.domain.Member;
import com.ruoyi.member.service.IMemberExpConfigService;
import com.ruoyi.member.service.IMemberService;

@SpringBootTest
public class MemberTest {

	@Autowired
	private IMemberService memberService;
	
	@Autowired
	private IMemberExpConfigService expConfigService;
	
	@Test
	void testMemberSignIn() {
		Member member = this.memberService.getById(398339741712453L);
		
		expConfigService.list().forEach(expConfig -> {
			
			expConfigService.triggerExpOperation(expConfig.getOpType(), member.getMemberId());
		});
	}
}
