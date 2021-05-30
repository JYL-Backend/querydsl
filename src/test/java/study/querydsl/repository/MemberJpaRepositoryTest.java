package study.querydsl.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {
    @Autowired
    EntityManager em;
    @Autowired
    MemberJpaRepository memberJpaRepository;
    @Test
    public void basicTest() throws Exception {
        //given
        Member newMember = new Member("member1", 10);
        memberJpaRepository.save(newMember);

        //when
        Member findMember = memberJpaRepository.findById(newMember.getId()).get();
        List<Member> result1 = memberJpaRepository.findAll();
        List<Member> result2 = memberJpaRepository.findByUsername("member1");

        //then
        assertThat(findMember).isEqualTo(newMember);
        assertThat(result1).containsExactly(newMember);
        assertThat(result2).containsExactly(newMember);
    }

    @Test
    public void basicQueryDsql() throws Exception {
        //given
        Member newMember = new Member("member1", 10);
        memberJpaRepository.save(newMember);

        //when
        Member findMember = memberJpaRepository.findById(newMember.getId()).get();
        List<Member> result1 = memberJpaRepository.findAll_queryDsl();
        List<Member> result2 = memberJpaRepository.findByUsername_queryDsl("member1");

        //then
        assertThat(findMember).isEqualTo(newMember);
        assertThat(result1).containsExactly(newMember);
        assertThat(result2).containsExactly(newMember);
    }

    @Test
    public void searchTest() throws Exception {
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        //when
        List<MemberTeamDto> result = memberJpaRepository.search(condition);

        //then
        assertThat(result).extracting("username").containsExactly("member4");
    }
}