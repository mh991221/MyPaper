package com.sp.fc.service;

import com.sp.fc.paper.domain.Paper;
import com.sp.fc.paper.domain.PaperTemplate;
import com.sp.fc.paper.domain.Problem;
import com.sp.fc.paper.repository.PaperAnswerRepository;
import com.sp.fc.paper.repository.PaperRepository;
import com.sp.fc.paper.service.PaperService;
import com.sp.fc.service.helper.WithPaperTest;
import com.sp.fc.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class paperSolveTest extends WithPaperTest {

    @Autowired
    private PaperRepository paperRepository;
    @Autowired
    private PaperAnswerRepository paperAnswerRepository;

    private PaperService paperService;

    private PaperTemplate paperTemplate;
    private User study1;

    private Paper paper;

    private Problem problem1;
    private Problem problem2;


    @BeforeEach
    void before(){
        prepareTest();
        this.paperService = new PaperService(paperRepository, paperAnswerRepository,paperTemplateService,userRepository);

        this.study1 = this.userTestHelper.createStudent(school, teacher, "study1", "중1");

        paperTemplate = paperTemplateHelper.createPaperTemplate(teacher, "시험 1");

        this.problem1 = this.paperTemplateHelper.addProblem(paperTemplate.getPaperTemplateId(),
                problem(paperTemplate.getPaperTemplateId(),"문제 1","답 1"));

        this.problem2 = this.paperTemplateHelper.addProblem(paperTemplate.getPaperTemplateId(),
                problem(paperTemplate.getPaperTemplateId(),"문제 2","답 2"));
        this.paper = paperService.publishPapers(paperTemplate.getPaperTemplateId(), List.of(study1.getUserId())).get(0);
    }

    @DisplayName("1. 답을 모두 맞춘다.")
    @Test
    void test_1(){
        paperService.answer(paper.getPaperId(),problem1.getProblemId(),problem1.getIndexNum(),"답 1");

        Paper ingPaper = paperService.findPaper(paper.getPaperId()).get();

        assertEquals(1, paperService.getPapersByUserIng(study1.getUserId()).size());

        assertEquals(Paper.PaperState.START, paper.getState());
        assertEquals(2,paper.getTotal());
        assertEquals(1, paper.getAnswered());
        assertEquals(0, paper.getCorrect());
        assertNotNull(ingPaper.getStartTime());
        assertNull(ingPaper.getEndTime());

        paperService.answer(paper.getPaperId(), problem2.getProblemId(), problem2.getIndexNum(), "답 2");
        assertEquals(2,paper.getTotal());
        assertEquals(2, paper.getAnswered());
        assertEquals(0, paper.getCorrect());
        assertNotNull(ingPaper.getStartTime());
        assertNull(ingPaper.getEndTime());

        paperService.paperDone(paper.getPaperId());
        assertEquals(2,paper.getTotal());
        assertEquals(2, paper.getAnswered());
        assertEquals(2, paper.getCorrect());

        assertEquals(100,paper.getScore());
        assertEquals(Paper.PaperState.END, paper.getState());
        assertNotNull(paper.getStartTime());
        assertNotNull(paper.getEndTime());
    }

    @DisplayName("2. 한문제를 틀린다.")
    @Test
    void test_2(){
        paperService.answer(paper.getPaperId(), problem1.getProblemId(),problem1.getIndexNum(), "답 1");
        paperService.answer(paper.getPaperId(), problem2.getProblemId(),problem2.getIndexNum(), "오답");
        paperService.paperDone(paper.getPaperId());

        assertEquals(1, paper.getCorrect());
        assertEquals(50, paper.getScore());
    }

    @DisplayName("3. 한문제를 미작성 한다.")
    @Test
    void test_3(){
        paperService.answer(paper.getPaperId(), problem2.getProblemId(),problem2.getIndexNum(), "답 2");
        paperService.paperDone(paper.getPaperId());

        assertEquals(1, paper.getCorrect());
        assertEquals(50, paper.getScore());
    }

    @DisplayName("4. 상태에 따라 시험지 조회")
    @Test
    void test_4(){
        assertEquals(1, paperService.getPapersByUserState(study1.getUserId(), List.of(Paper.PaperState.READY, Paper.PaperState.START)).size());
        assertEquals(0, paperService.getPapersByUserState(study1.getUserId(), Paper.PaperState.END).size());

        paperService.answer(paper.getPaperId(), problem2.getProblemId(), problem2.getIndexNum(), "답2");
        assertEquals(1, paperService.getPapersByUserState(study1.getUserId(), List.of(Paper.PaperState.READY, Paper.PaperState.START)).size());
        assertEquals(0, paperService.getPapersByUserState(study1.getUserId(), Paper.PaperState.END).size());

        paperService.paperDone(paper.getPaperId());

        assertEquals(0, paperService.getPapersByUserState(study1.getUserId(), List.of(Paper.PaperState.READY, Paper.PaperState.START)).size());
        assertEquals(1, paperService.getPapersByUserState(study1.getUserId(), Paper.PaperState.END).size());
    }

    @DisplayName("5. 문제의 답을 수정했다.")
    @Test
    void test_5(){
        paperTemplateService.update(problem1.getProblemId(),"문제 3", "답 3");

        paperService.answer(paper.getPaperId(), problem1.getProblemId(),1,"답 3");
        paperService.answer(paper.getPaperId(), problem2.getProblemId(),problem2.getIndexNum(),"답 2");
        paperService.paperDone(paper.getPaperId());

        assertEquals(2, paper.getCorrect());
    }
}
