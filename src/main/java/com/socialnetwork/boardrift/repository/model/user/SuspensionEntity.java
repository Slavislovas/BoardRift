package com.socialnetwork.boardrift.repository.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "suspensions")
public class SuspensionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suspension")
    private Long id;

    @Column(name = "suspended_until")
    private LocalDate suspendedUntil;

    @Column(name = "reason")
    private String reason;

    @OneToOne
    @JoinColumn(name = "id_user")
    private UserEntity user;

}
