package com.sp.fc.service.helper;

import com.sp.fc.paper.domain.PaperAnswer;
import com.sp.fc.paper.domain.PaperTemplate;
import com.sp.fc.paper.domain.Problem;
import com.sp.fc.paper.repository.PaperAnswerRepository;
import com.sp.fc.paper.repository.PaperRepository;
import com.sp.fc.paper.repository.PaperTemplateRepository;
import com.sp.fc.paper.repository.ProblemRepository;
import com.sp.fc.paper.service.PaperService;
import com.sp.fc.paper.service.PaperTemplateService;
import com.sp.fc.paper.service.ProblemService;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.service.helper.UserTestHelper;
import com.sp.fc.user.service.helper.WithUserTest;
import org.springframework.beans.factory.annotation.Autowired;

public class WithPaperTest extends WithUserTest {

    @Autowired
    protected PaperTemplateRepository paperTemplateRepository;
    @Autowired
    protected ProblemRepository problemRepository;

    protected PaperTemplateService paperTemplateService;

    protected ProblemService problemService;
    protected User teacher;
    protected PaperTemplateHelper paperTemplateHelper;

    protected void prepareTest(){
        this.paperTemplateRepository.deleteAll();
        this.problemRepository.deleteAll();
        prepareUserServices();

        this.problemService = new ProblemService(problemRepository);
        this.paperTemplateService = new PaperTemplateService(paperTemplateRepository, problemService);

        this.paperTemplateHelper = new PaperTemplateHelper(this.paperTemplateService);

        this.teacher = userTestHelper.createTeacher(school, "teacher1");

    }
    protected Problem problem(Long paperTemplateId, String content, String answer){
        return Problem.builder()
                .paperTemplateId(paperTemplateId)
                .content(content)
                .answer(answer)
                .build();
    }

}
