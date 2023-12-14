package com.sp.fc.user.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sp_table")
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long schoolId;

    private String city;
    private String name;

    @Transient
    private Long teacherCount;

    @Transient
    private Long studyCount;

    private LocalDateTime updated;
    @Column(updatable = false)
    private LocalDateTime created;

}
