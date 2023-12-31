package com.sp.fc.paper.service;

import com.sp.fc.paper.domain.Paper;
import com.sp.fc.paper.domain.PaperTemplate;
import com.sp.fc.paper.domain.Problem;
import com.sp.fc.paper.repository.PaperTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
@Transactional
@RequiredArgsConstructor
public class PaperTemplateService {

    private final PaperTemplateRepository paperTemplateRepository;
    private final ProblemService problemService;

    public PaperTemplate save(PaperTemplate paperTemplate){
        if(paperTemplate.getPaperTemplateId() == null){
            paperTemplate.setCreated(LocalDateTime.now());
        }
        paperTemplate.setUpdated(LocalDateTime.now());
        return paperTemplateRepository.save(paperTemplate);
    }

    public Problem addProblem(long paperTemplateId, Problem problem){
        problem.setPaperTemplateId(paperTemplateId);

        return paperTemplateRepository.findById(paperTemplateId).map(paperTemplate -> {
            if(paperTemplate.getProblemList() == null){
                paperTemplate.setProblemList(new ArrayList<>());
            }
            problem.setCreated(LocalDateTime.now());
            paperTemplate.getProblemList().add(problem);
            IntStream.rangeClosed(1, paperTemplate.getProblemList().size()).forEach(i->{
                paperTemplate.getProblemList().get(i-1).setIndexNum(i);
            });

            paperTemplate.setTotal(paperTemplate.getProblemList().size());
            Problem saved = problemService.save(problem);

            save(paperTemplate);
            return saved;
        }).orElseThrow(()-> new IllegalArgumentException(paperTemplateId+" 아이디 시험지가 없습니다."));
    }

    public Optional<PaperTemplate> findById(Long paperTemplateId){
        return paperTemplateRepository.findById(paperTemplateId);
    }

    public PaperTemplate removeProblem(Long paperTemplateId, Long problemId){
        return findById(paperTemplateId).map(paperTemplate ->{
            if(paperTemplate.getProblemList() == null){
                return paperTemplate;
            }
            Optional<Problem> problem = paperTemplate.getProblemList().stream()
                    .filter(o -> problemId.equals(problemId)).findFirst();

            if(problem.isPresent()){
                paperTemplate.setProblemList(
                        paperTemplate.getProblemList().stream()
                                .filter(o -> !o.getProblemId().equals(problemId)).collect(Collectors.toList())
                );

                problemService.delete(problem.get());

                IntStream.rangeClosed(1, paperTemplate.getProblemList().size()).forEach(i ->{
                    paperTemplate.getProblemList().get(i-1).setIndexNum(i);
                });
            }
            paperTemplate.setTotal(paperTemplate.getProblemList().size());
            return save(paperTemplate);
        }).orElseThrow(() -> new IllegalArgumentException(paperTemplateId + "번 시험지가 존재하지 않습니다."));
    }


    public void update(Long problemId, String content, String answer){
        problemService.updateProblem(problemId,content, answer);

        paperTemplateRepository.findAll().forEach(paperTemplate -> {
            if(paperTemplate.getProblemList().stream().anyMatch(p -> p.getProblemId().equals(problemId))){
                List<Problem> problemList = paperTemplate.getProblemList();

                problemList.stream().forEach(problem -> {
                    if(problem.getProblemId().equals(problemId)){
                        problem.setContent(content);
                        problem.setAnswer(answer);
                        problem.setUpdated(LocalDateTime.now());
                    }
                });
            }
        });
    }

    @Transactional(readOnly = true)
    public Map<Integer, String> getAnswerSheet(Long paperTemplateId){
        Optional<PaperTemplate> paperTemplate = findById(paperTemplateId);
        if(paperTemplate.isEmpty()){
            return new HashMap<>();
        }
        return paperTemplate.get().getProblemList().stream()
                .collect(Collectors.toMap(Problem::getIndexNum,Problem::getAnswer));
    }

    @Transactional(readOnly = true)
    public Optional<PaperTemplate> findProblemTemplate(Long paperTemplateId) {
        return paperTemplateRepository.findById(paperTemplateId).map(pt->{
            if(pt.getProblemList().size() != pt.getTotal()){ // lazy 해결위해 체크...
                pt.setTotal(pt.getProblemList().size());
            }
            return pt;
        });
    }
    @Transactional(readOnly = true)
    public List<PaperTemplate> findByTeacherId(Long teacherId){
        return paperTemplateRepository.findAllByUserIdOrderByCreatedDesc(teacherId);
    }
    @Transactional(readOnly = true)
    public Page<PaperTemplate> findByTeacherId(Long userId, int pageNum, int size) {
        return paperTemplateRepository.findAllByUserIdOrderByCreatedDesc(userId, PageRequest.of(pageNum-1, size));
    }

    @Transactional(readOnly = true)
    public Object countByUserId(Long userId) {
        return paperTemplateRepository.countByuserId(userId);
    }

    public void updatePublishedCount(long paperTemplateId, long publishedCount) {
        paperTemplateRepository.findById(paperTemplateId).ifPresent(paperTemplate -> {
            paperTemplate.setPublishedCount(publishedCount);
            paperTemplateRepository.save(paperTemplate);
        });
    }

    public void updateCompleteCount(Long paperTemplateId) {
        paperTemplateRepository.findById(paperTemplateId).ifPresent(paperTemplate -> {
            paperTemplate.setCompleteCount(paperTemplate.getCompleteCount()+1);
            paperTemplateRepository.save(paperTemplate);
        });
    }
}
