package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

	@Autowired
	MemberRepository memberRepository;
	@Autowired
	TeamRepository teamRepository;
	@PersistenceContext
	EntityManager em;

	@Autowired
	MemberQueryRepository memberQueryRepository;

	@Test
	public void testMember() {
		System.out.println("memberRepository = " + memberRepository.getClass());
		Member member = new Member("memberA");
		Member saveMember = memberRepository.save(member);

		Member findMember = memberRepository.findById(saveMember.getId()).get();

		assertThat(findMember.getId()).isEqualTo(member.getId());
		assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
		assertThat(findMember).isEqualTo(member);
	}

	@Test
	public void basicCRUD() {
		Member member1 = new Member("member1");
		Member member2 = new Member("member2");
		memberRepository.save(member1);
		memberRepository.save(member2);

		//단건 조회 검증
		Member findMember1 = memberRepository.findById(member1.getId()).get();
		Member findMember2 = memberRepository.findById(member2.getId()).get();
		assertThat(findMember1.getId()).isEqualTo(member1.getId());
		assertThat(findMember2.getId()).isEqualTo(member2.getId());

//		findMember1.setUsername("member!");

		//리스트 조회 검증
		List<Member> all = memberRepository.findAll();
		assertThat(all.size()).isEqualTo(2);

		//카운트 검증
		long count = memberRepository.count();
		assertThat(count).isEqualTo(2);

		//삭제 검증
		memberRepository.delete(member1);
		memberRepository.delete(member2);
		long deleteCount = memberRepository.count();
		assertThat(deleteCount).isEqualTo(0);
	}

	@Test
	public void findByUsernameAndAgeGreaterThen() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);

		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

		assertThat(result.get(0).getUsername()).isEqualTo("AAA");
		assertThat(result.get(0).getAge()).isEqualTo(20);
		assertThat(result.size()).isEqualTo(1);
	}

	@Test
	public void findHelloBy() {
		List<Member> helloBy = memberRepository.findTop3HelloBy();
	}

	@Test
	public void testNamedQuery() {

		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);

		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findByUsername("AAA");
		Member findMember = result.get(0);
		assertThat(findMember).isEqualTo(m1);

	}

	@Test
	public void testQuery() {

		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findUser("AAA", 10);
		assertThat(result.get(0)).isEqualTo(m1);

	}

	@Test
	public void findUsernameLIst() {

		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<String> usernameList1 = memberRepository.findUsernameList();
		for (String s : usernameList1) {
			System.out.println("s = " + s);
		}
	}

	@Test
	public void findMemberDto() {

		Team team = new Team("teamA");
		teamRepository.save(team);

		Member m1 = new Member("AAA", 10);
		m1.setTeam(team);
		memberRepository.save(m1);

		List<MemberDto> memberDto = memberRepository.findMemberDto();
		for (MemberDto dto : memberDto) {
			System.out.println("dto = " + dto);
		}
	}

	@Test
	public void findByNames() {

		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
		for (Member member : result) {
			System.out.println("member = " + member);
		}
	}

	@Test
	public void returnType() {

		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

//		List<Member> result = memberRepository.findByUsername("AAAB");
//		System.out.println("result = " + result.size());

//		Member findMember = memberRepository.findMemberByUsername("AAA");
//		System.out.println("findMember = " + findMember);

//		Optional<Member> aaa = memberRepository.findOptionalByUsername("AAA");
//		System.out.println("aaa = " + aaa);
	}

	@Test
	public void paging() {
		//given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member2", 10));
		memberRepository.save(new Member("member3", 10));
		memberRepository.save(new Member("member4", 10));
		memberRepository.save(new Member("member5", 10));
		memberRepository.save(new Member("member6", 10));
		memberRepository.save(new Member("member7", 10));

		int age = 10;
		PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

		//when

		Page<Member> page = memberRepository.findByAge(age, pageRequest);

		Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));

		//페이지 계산공식 적용
		//totalPage = totalCount / size
		//마지막 페이지
		//최초 페이지

		//then
		List<Member> content = page.getContent();
		long totalElements = page.getTotalElements();

		for (Member member : content) {
			System.out.println("member = " + member);
		}
		System.out.println("totalElements = " + totalElements);

		assertThat(content.size()).isEqualTo(3);
		assertThat(page.getTotalElements()).isEqualTo(7);
		assertThat(page.getNumber()).isEqualTo(0);
		assertThat(page.getTotalPages()).isEqualTo(3);
		assertThat(page.isFirst()).isTrue();
		assertThat(page.hasNext()).isTrue();
	}

	@Test
	public void slice() {
		//given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member2", 10));
		memberRepository.save(new Member("member3", 10));
		memberRepository.save(new Member("member4", 10));
		memberRepository.save(new Member("member5", 10));
		memberRepository.save(new Member("member6", 10));
		memberRepository.save(new Member("member7", 10));

		int age = 10;
		PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

		//when

		Slice<Member> page = memberRepository.findSliceByAge(age, pageRequest);

		//페이지 계산공식 적용
		//totalPage = totalCount / size
		//마지막 페이지
		//최초 페이지

		//then
		List<Member> content = page.getContent();
//		long totalElements = page.getTotalElements();

		for (Member member : content) {
			System.out.println("member = " + member);
		}
//		System.out.println("totalElements = " + totalElements);

		assertThat(content.size()).isEqualTo(3);
//		assertThat(page.getTotalElements()).isEqualTo(7);
		assertThat(page.getNumber()).isEqualTo(0);
//		assertThat(page.getTotalPages()).isEqualTo(3);
		assertThat(page.isFirst()).isTrue();
		assertThat(page.hasNext()).isTrue();
	}

	@Test
	public void bulkUpdate() {
		//given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member2", 19));
		memberRepository.save(new Member("member3", 20));
		memberRepository.save(new Member("member4", 21));
		memberRepository.save(new Member("member5", 40));

		//when
		int resultCunt = memberRepository.bulkAgePlus(20);
//		em.flush();
//		em.clear();

		List<Member> result = memberRepository.findByUsername("member5");
		Member member5 = result.get(0);
		System.out.println("member5 = " + member5);

		//then
		assertThat(resultCunt).isEqualTo(3);
	}

	@Test
	public void findMemberLazy() throws Exception {
	    //given
	    //member1 -> teamA
		//member2 -> teamB

		Team teamA = new Team("teatA");
		Team teamB = new Team("teatB");
		teamRepository.save(teamA);
		teamRepository.save(teamB);
		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 10, teamB);
		memberRepository.save(member1);
		memberRepository.save(member2);

		em.flush();
		em.clear();

		//when
		//select Member 1 + N
		List<Member> members = memberRepository.findAll();

		for (Member member : members) {
			System.out.println("member = " + member);
			System.out.println("member.teamClass = " + member.getTeam().getClass());
			System.out.println("member.team = " + member.getTeam().getName());
		}

		//then

	}

	@Test
	public void queryHint() {
		//given
		Member member1 = memberRepository.save(new Member("member1", 10));
		em.flush();
		em.clear();

		//when
//		Member findMember = memberRepository.findById(member1.getId()).get();
//		findMember.setUsername("member2");

		Member findMember = memberRepository.findReadOnlyByUsername("member1");
		findMember.setUsername("member2");

		em.flush();
	}

	@Test
	public void lock() {
		//given
		Member member1 = memberRepository.save(new Member("member1", 10));
		em.flush();
		em.clear();

		//when
//		Member findMember = memberRepository.findById(member1.getId()).get();
//		findMember.setUsername("member2");

		memberRepository.findLockByUsername("member1");
	}

	@Test
	public void callCustom() {
		List<Member> result = memberRepository.findMemberCustom();
	}
}