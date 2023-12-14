package com.sp.fc.paper.repository;

import com.sp.fc.paper.domain.Paper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Map;

public interface PaperRepository extends JpaRepository<Paper,Long> {
    List<Paper> findAllByPaperTemplateIdAndStudyUserIdIn(Long paperTemplateID, List<Long> studentIdlist);

    List<Paper> findAllByPaperTemplateId(Long paperTemplateId);

    long countByPaperTemplateId(Long paperTemplateId);

    List<Paper> findAllByStudyUserIdOrderByCreatedDesc(Long studyUserId);

    long countByStudyUserId(Long studyUserId);

    List<Paper> findAllByStudyUserIdAndStateOrderByCreatedDesc(Long studyUserId, Paper.PaperState state);

    List<Paper> findAllByStudyUserIdAndStateInOrderByCreatedDesc(Long studyUserId, List<Paper.PaperState> states);

    long countByStudyUserIdAndStateIn(Long studyUserId, List<Paper.PaperState> states);

    long countByStudyUserIdAndState(Long studyUserId, Paper.PaperState state);

    Page<Paper> findAllByStudyUserIdAndStateOrderByCreatedDesc(Long studyUserId, Paper.PaperState state, Pageable pageable);
}
