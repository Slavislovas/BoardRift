package com.socialnetwork.boardrift.repository.model;

import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "warnings")
public class WarningEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_warning")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_recipient")
    private UserEntity recipient;

    @Column(name = "reason")
    private String reason;

    @Column(name = "issued_date")
    private Date issuedDate;

    @ManyToOne
    @JoinColumn(name = "id_issuer")
    private UserEntity issuer;
}
