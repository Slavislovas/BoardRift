package com.socialnetwork.boardrift.repository.model;

import com.socialnetwork.boardrift.enumeration.VerificationTokenType;
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

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "verification_tokens")
public class VerificationTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "token")
    private String token;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @Column(name = "type")
    private VerificationTokenType type;

    @ManyToOne
    @JoinColumn(name = "id_user")
    UserEntity user;

    public VerificationTokenEntity(String token, VerificationTokenType type, UserEntity userEntity, int expiryTimeInMinutes){
        this.token = token;
        this.type = type;
        this.user = userEntity;
        this.expirationDate = calculateExpiryDate(expiryTimeInMinutes);
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }
}
