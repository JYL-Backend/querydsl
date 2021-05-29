package study.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {
    @PersistenceContext
    EntityManager em;

    @BeforeEach
    public void before() throws Exception {
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

        em.flush();
        em.clear();
        //when
        List<Member> members = em.createQuery("select m from Member m ", Member.class)
                .getResultList();


        //then
        assertThat(members.get(0).getUsername()).isEqualTo("member1");
        assertThat(members.get(1).getUsername()).isEqualTo("member2");
        assertThat(members.get(2).getUsername()).isEqualTo("member3");
        assertThat(members.get(3).getUsername()).isEqualTo("member4");
    }
    @Test
    public void startJPQL() throws Exception {
        //멤버1 찾기
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQueryDsl() throws Exception {
        //given
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        //when
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();


        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }



    @Test
    public void searchAndParam() throws Exception {
        //given
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        //when
        Member findMember = queryFactory
                .selectFrom(QMember.member)
                .where(
                        QMember.member.username.eq("member1")
                        ,(QMember.member.age.eq(10)))
                .fetchOne();

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }


    /**
     * 1 나이 내림차순
     * 2 이름 올림차순
     * 3 2에서 회원이름이 없다면 마지막 출력
     * @throws Exception
     */
    @Test
    public void sort() throws Exception {
        //given
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        //when
        List<Member> result = queryFactory.
                selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        //then
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1() throws Exception {
        //given
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        //when
        List<Member> result = queryFactory.
                selectFrom(member)
                .orderBy(member.age.desc())
                .offset(1)
                .limit(2)
                .fetch();


        //then
        assertThat(result.size()).isEqualTo(2);
    }
    @Test
    public void aggregation() throws Exception {
        //given
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        //when
        List<Tuple> result = queryFactory.
                select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);

        //then
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀이름과 각팀의 평균 연령
     * @throws Exception
     */
    @Test
    public void group() throws Exception {
        //given
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        //when
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA =  result.get(0);
        Tuple teamB =  result.get(1);

        //then
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamB.get(team.name)).isEqualTo("teamB");

        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);

    }

    /**
     * 팀A에 소속된 모든 회원
     * @throws Exception
     */
    @Test
    public void join() throws Exception {
        //given
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        //then
    }
}
