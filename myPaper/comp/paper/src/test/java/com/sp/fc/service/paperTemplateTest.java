package com.sp.fc.service;

import com.sp.fc.paper.domain.PaperTemplate;
import com.sp.fc.paper.domain.Problem;
import com.sp.fc.service.helper.PaperTemplateHelper;
import com.sp.fc.service.helper.WithPaperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Lazy;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class paperTemplateTest extends WithPaperTest {

    private PaperTemplate paperTemplate;

    @BeforeEach
    void before(){
        prepareTest();
        paperTemplate = paperTemplateHelper.createPaperTemplate(teacher,"테스트1");
    }

    private Problem problem(String content, String answer){
        return Problem.builder().paperTemplateId(paperTemplate.getPaperTemplateId())
                .content(content)
                .answer(answer)
                .build();
    }

    @DisplayName("1. 시험지 만들기")
    @Test
    void test_1(){
        assertEquals(1, paperTemplateRepository.count());
        PaperTemplateHelper.assertPaperTemplate(paperTemplateRepository.findAll().get(0), teacher, "테스트1");
    }

    @DisplayName("2. 문제 추가하기")
    @Test
    void test_2(){
        Problem problem = problem("문제1","답 1");
        paperTemplateHelper.addProblem(paperTemplate.getPaperTemplateId(),problem);

        assertEquals(1, paperTemplate.getProblemList().size());
        assertEquals("문제1",paperTemplate.getProblemList().get(0).getContent());
        assertEquals("답 1",paperTemplate.getProblemList().get(0).getAnswer());
    }

    @DisplayName("3. 문제 삭제하기")
    @Test
    void test_3(){
        Problem problem = problem("문제1","답 1");
        paperTemplateHelper.addProblem(paperTemplate.getPaperTemplateId(),problem);

        assertEquals(1, paperTemplate.getProblemList().size());
        assertEquals("문제1",paperTemplate.getProblemList().get(0).getContent());
        assertEquals("답 1",paperTemplate.getProblemList().get(0).getAnswer());

        paperTemplateService.removeProblem(paperTemplate.getPaperTemplateId(),problem.getProblemId());
        assertEquals(0,paperTemplate.getProblemList().size());
    }

    @DisplayName("4. 문제 업데이트 하기")
    @Test
    void test_4(){
        Problem problem = problem("문제1","답 1");
        problemRepository.save(problem);

        paperTemplateHelper.addProblem(paperTemplate.getPaperTemplateId(),problem);

        assertEquals("문제1",problemRepository.findById(problem.getProblemId()).get().getContent());

        assertEquals(1, paperTemplate.getProblemList().size());
        assertEquals("문제1",paperTemplate.getProblemList().get(0).getContent());
        assertEquals("답 1",paperTemplate.getProblemList().get(0).getAnswer());


        paperTemplateService.update(problem.getProblemId(),"문제 2","답 2");

        assertEquals(1, paperTemplate.getProblemList().size());
        assertEquals("문제 2",paperTemplate.getProblemList().get(0).getContent());
        assertEquals("답 2",paperTemplate.getProblemList().get(0).getAnswer());
        assertEquals("답 2", problemRepository.findById(problem.getProblemId()).get().getAnswer());
    }
}
