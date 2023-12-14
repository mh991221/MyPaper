package com.sp.fc.paper.domain;

import com.sp.fc.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="sp_paper_template")
public class PaperTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paperTemplateId;

    @Transient
    private User creator;

    private Long userId;
    private String name;

    private int total;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(foreignKey = @ForeignKey(name = "paperTemplateId"))
    private List<Problem> problemList;

    private long publishedCount;
    private long completeCount;

    @Column(updatable = false)
    private LocalDateTime created;
    private LocalDateTime updated;
}
