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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class paperTest extends WithPaperTest {

    @Autowired
    private PaperRepository paperRepository;
    @Autowired
    private PaperAnswerRepository paperAnswerRepository;

    private PaperService paperService;

    private PaperTemplate paperTemplate;
    private User study1;
    private User study2;

    @BeforeEach
    void before(){
        paperRepository.deleteAll();
        prepareTest();

        this.paperService = new PaperService(paperRepository, paperAnswerRepository,paperTemplateService,userRepository);

        this.study1 = this.userTestHelper.createStudent(school, teacher, "study1", "중1");
        this.study2 = this.userTestHelper.createStudent(school, teacher, "study2", "중1");

        paperTemplate = paperTemplateHelper.createPaperTemplate(teacher, "시험 1");

        this.paperTemplateHelper.addProblem(paperTemplate.getPaperTemplateId(),
                problem(paperTemplate.getPaperTemplateId(),"문제 1","답 1"));

        this.paperTemplateHelper.addProblem(paperTemplate.getPaperTemplateId(),
                problem(paperTemplate.getPaperTemplateId(),"문제 2","답 2"));
    }

    @DisplayName("1. paper 를 publish 하기")
    @Test
    void test_1(){
        paperService.publishPapers(paperTemplate.getPaperTemplateId(), List.of(study1.getUserId()));

        assertEquals(1,paperTemplate.getPublishedCount());
        List<Paper> papers = paperService.getPapers(paperTemplate.getPaperTemplateId());

        assertEquals(1, papers.size());

        Paper paper= papers.get(0);

        assertNotNull(paper.getPaperId());
        assertNotNull(paper.getCreated());
        assertEquals(study1.getUserId(), paper.getStudyUserId());
        assertEquals(Paper.PaperState.READY, paper.getState());
        assertEquals(paperTemplate.getPaperTemplateId(), paper.getPaperTemplateId());
        assertEquals(2, paper.getTotal());
        assertEquals(0, paper.getAnswered());
        assertEquals(0, paper.getCorrect());
        assertEquals(paperTemplate.getName(), paper.getName());
        assertNull(paper.getStartTime());
        assertNull(paper.getEndTime());
    }

    @DisplayName("2. paper 삭제하기")
    @Test
    void test_2(){
        paperService.publishPapers(paperTemplate.getPaperTemplateId(), List.of(study1.getUserId(), study2.getUserId()));
        List<Paper> paperList = paperService.getPapers(paperTemplate.getPaperTemplateId());

        assertEquals(2, paperList.size());
        assertEquals(2,paperTemplate.getPublishedCount());

        paperService.removePapers(paperTemplate.getPaperTemplateId(),List.of(study1.getUserId()));

        assertEquals(1, paperTemplate.getPublishedCount());
        paperList = paperService.getPapers(paperTemplate.getPaperTemplateId());

        assertEquals(1, paperList.size());
        Paper paper = paperList.get(0);

        assertEquals(study2.getUserId(), paper.getStudyUserId());
    }
}
