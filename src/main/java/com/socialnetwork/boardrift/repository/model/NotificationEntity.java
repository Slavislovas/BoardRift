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
@Table(name = "notifications")
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification")
    private Long id;

    @Column(name = "description")
    private String description;

    @Column(name = "redirect_url")
    private String redirectUrl;

    @Column(name = "creation_date")
    private Date creationDate;

    @Column(name = "unread")
    private Boolean unread;

    @ManyToOne
    @JoinColumn(name = "id_recipient")
    private UserEntity recipient;
}
