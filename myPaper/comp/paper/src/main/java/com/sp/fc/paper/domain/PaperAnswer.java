package com.sp.fc.paper.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="sp_paper_answer")
public class PaperAnswer {

    @ManyToOne
    @JsonIgnore
    @JoinColumn(foreignKey = @ForeignKey(name = "paperId"))
    Paper paper;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class PaperAnswerId implements Serializable {
        private Long paperId;
        private Integer num; // 1-base
    }

    @EmbeddedId
    private PaperAnswerId id;

    private Long problemId;
    private String answer;
    private Boolean correct;

    private LocalDateTime answered;

    public Integer num(){
        return id.num;
    }

}
