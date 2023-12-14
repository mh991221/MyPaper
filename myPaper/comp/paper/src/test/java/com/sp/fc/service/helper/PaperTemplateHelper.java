package com.sp.fc.service.helper;

import com.sp.fc.paper.domain.PaperTemplate;
import com.sp.fc.paper.domain.Problem;
import com.sp.fc.paper.repository.PaperTemplateRepository;
import com.sp.fc.paper.service.PaperTemplateService;
import com.sp.fc.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RequiredArgsConstructor
public class PaperTemplateHelper {

    private final PaperTemplateService paperTemplateService;

    public PaperTemplate createPaperTemplate(User teacher, String name){
        PaperTemplate paperTemplate = PaperTemplate.builder()
                .userId(teacher.getUserId())
                .creator(teacher)
                .name(name)
                .build();

        return paperTemplateService.save(paperTemplate);
    }

    public Problem addProblem(long paperTemplateId, Problem problem){
        return paperTemplateService.addProblem(paperTemplateId,problem);
    }

    public static void assertPaperTemplate(PaperTemplate pt, User user, String paperName){
        assertNotNull(pt.getPaperTemplateId());
        assertNotNull(pt.getCreated());
        assertNotNull(pt.getUpdated());
        assertEquals(paperName, pt.getName());
        assertEquals(user.getUserId(), pt.getCreator().getUserId());
    }
}
