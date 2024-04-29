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
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "id_chat")
    String chatId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "timestamp")
    private Date timestamp;

    @Column(name = "unread")
    private Boolean unread;

    @ManyToOne
    @JoinColumn(name = "id_sender")
    private UserEntity sender;

    @ManyToOne
    @JoinColumn(name = "id_recipient")
    private UserEntity recipient;
}
