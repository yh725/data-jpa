package study.datajpa.dto;

import lombok.Data;
import study.datajpa.entity.Member;

@Data
public class MemberDto {

	private Long id;
	private String username;
	private String testName;

	public MemberDto(Long id, String username, String testName) {
		this.id = id;
		this.username = username;
		this.testName = testName;
	}

	public MemberDto(Member member) {
		this.id = member.getId();
		this.username = member.getUsername();
	}
}
