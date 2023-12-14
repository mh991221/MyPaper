package com.sp.fc.paper.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.bytebuddy.asm.Advice;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="sp_problem")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long problemId;

    private Long paperTemplateId;

    private String content;
    private String answer;

    private int indexNum;

    @Column(updatable = false)
    private LocalDateTime created;
    private LocalDateTime updated;
}
