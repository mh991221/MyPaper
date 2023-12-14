package com.sp.fc.paper.service;


import com.sp.fc.paper.domain.Paper;
import com.sp.fc.paper.domain.PaperAnswer;
import com.sp.fc.paper.domain.PaperTemplate;
import com.sp.fc.paper.repository.PaperAnswerRepository;
import com.sp.fc.paper.repository.PaperRepository;
import com.sp.fc.paper.repository.PaperTemplateRepository;
import com.sp.fc.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Service
@Transactional
@RequiredArgsConstructor
public class PaperService {

    private final PaperRepository paperRepository;
    private final PaperAnswerRepository paperAnswerRepository;
    private final PaperTemplateService paperTemplateService;
    private final UserRepository userRepository;

    public Paper save(Paper paper){
        if(paper.getPaperId() == null){
            paper.setCreated(LocalDateTime.now());
        }
        return paperRepository.save(paper);
    }

    @Transactional(readOnly = true)
    public Optional<Paper> findPaper(Long paperId){
        return paperRepository.findById(paperId);
    }

    @Transactional
    public List<Paper> publishPapers(Long paperTemplateId, List<Long> studentIds){
        List<Paper> papers =paperTemplateService.findById(paperTemplateId).map(paperTemplate ->
            StreamSupport.stream(userRepository.findAllById(studentIds).spliterator(), false)
                    .map(student ->{
                        Paper paper = Paper.builder()
                                .user(student)
                                .studyUserId(student.getUserId())
                                .total(paperTemplate.getTotal())
                                .paperTemplateId(paperTemplate.getPaperTemplateId())
                                .name(paperTemplate.getName())
                                .state(Paper.PaperState.READY)
                                .build();
                        return save(paper);
                    }).collect(Collectors.toList())
        ).orElseThrow(() -> new IllegalArgumentException(paperTemplateId + "의 시험지가 존재하지 않습니다."));

        paperTemplateService.updatePublishedCount(paperTemplateId,papers.size());
        return papers;
    }

    public void removePapers(Long paperTemplateID, List<Long> studentIds){
        paperRepository.findAllByPaperTemplateIdAndStudyUserIdIn(paperTemplateID, studentIds)
                .forEach(paper -> {
                    paperRepository.delete(paper);
                    paperTemplateService.findById(paperTemplateID).get().setPublishedCount(paperTemplateService.findById(paperTemplateID).get().getPublishedCount() - 1);
                });
    }

    @Transactional
    public void answer(Long paperId, Long problemId, int indexNum, String answer){
         findPaper(paperId).ifPresentOrElse(p ->{
            Optional<PaperAnswer> pa = p.getPaperAnswerList() == null? Optional.empty() :
                    p.getPaperAnswerList().stream().filter(a -> a.getId().getNum() == indexNum).findFirst();
            if(pa.isPresent()){
                PaperAnswer paperAnswer = pa.get();
                paperAnswer.setAnswer(answer);
                paperAnswer.setProblemId(problemId);
                paperAnswer.setAnswered(LocalDateTime.now());
                paperAnswerRepository.save(paperAnswer);
            }else{
                PaperAnswer pAnswer =  new PaperAnswer().builder()
                        .id(new PaperAnswer.PaperAnswerId(paperId, indexNum))
                        .answer(answer)
                        .problemId(problemId)
                        .answered(LocalDateTime.now())
                        .build();

                pAnswer.setPaper(p);
                if(p.getPaperAnswerList() == null){
                    p.setPaperAnswerList(new ArrayList<>());
                }
                p.getPaperAnswerList().add(pAnswer);
                p.addAnswered();
                if(p.getState() != Paper.PaperState.START){
                    p.setState(Paper.PaperState.START);
                    p.setStartTime(LocalDateTime.now());
                }
                paperRepository.save(p);
            }
        }, () -> new IllegalArgumentException(paperId + "시험지를 찾을 수 없습니다."));
    }

    @Transactional
    public void paperDone(Long paperId){
        final Paper paper = findPaper(paperId).orElseThrow(() -> new IllegalArgumentException(paperId + "시험지를 찾을 수 없습니다."));
        final Map<Integer, String> answerSheet = paperTemplateService.getAnswerSheet(paper.getPaperTemplateId());

        paper.setCorrect(0);

        if(paper.getPaperAnswerList() != null){
            paper.getPaperAnswerList().forEach(a ->{
                if(a.getAnswer() != null && a.getAnswer().equals(answerSheet.get(a.getId().getNum()))){
                    a.setCorrect(true);
                    paper.addCorrect();
                    paperAnswerRepository.save(a);
                }
            });
        }

        paper.setState(Paper.PaperState.END);
        paper.setEndTime(LocalDateTime.now());
        Paper saved = paperRepository.save(paper);
        paperTemplateService.updateCompleteCount(saved.getPaperTemplateId());
    }


    @Transactional(readOnly = true)
    public List<Paper> getPapers(Long paperTemplateId) {
        return paperRepository.findAllByPaperTemplateId(paperTemplateId);
    }

    @Transactional(readOnly = true)
    public List<Paper> getPapersByUser(Long studyUserId) {
        return paperRepository.findAllByStudyUserIdOrderByCreatedDesc(studyUserId);
    }

    @Transactional(readOnly = true)
    public List<Paper> getPapersByUserState(Long studyUserId, Paper.PaperState state) {
        return paperRepository.findAllByStudyUserIdAndStateOrderByCreatedDesc(studyUserId, state);
    }

    @Transactional(readOnly = true)
    public List<Paper> getPapersByUserIng(Long studyUserId) {
        return paperRepository.findAllByStudyUserIdAndStateInOrderByCreatedDesc(studyUserId, List.of(Paper.PaperState.READY, Paper.PaperState.START));
    }

    @Transactional(readOnly = true)
    public long countPapersByUserIng(Long studyUserId) {
        return paperRepository.countByStudyUserIdAndStateIn(studyUserId, List.of(Paper.PaperState.READY, Paper.PaperState.START));
    }

    @Transactional(readOnly = true)
    public Page<Paper> getPapersByUserResult(Long studyUserId, int pageNum, int size) {
        return paperRepository.findAllByStudyUserIdAndStateOrderByCreatedDesc(studyUserId, Paper.PaperState.END, PageRequest.of(pageNum-1, size));
    }

    @Transactional(readOnly = true)
    public long countPapersByUserResult(Long studyUserId) {
        return paperRepository.countByStudyUserIdAndState(studyUserId, Paper.PaperState.END);
    }

    @Transactional(readOnly = true)
    public List<Paper> getPapersByUserState(Long studyUserId, List<Paper.PaperState> states) {
        return paperRepository.findAllByStudyUserIdAndStateInOrderByCreatedDesc(studyUserId, states);
    }

}
