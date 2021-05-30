package study.querydsl.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

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
}