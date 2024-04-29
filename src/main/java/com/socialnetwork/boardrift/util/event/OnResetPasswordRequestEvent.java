package com.socialnetwork.boardrift.util.event;

import com.socialnetwork.boardrift.repository.model.VerificationTokenEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class OnResetPasswordRequestEvent extends ApplicationEvent {
    private VerificationTokenEntity passwordResetTokenEntity;
    private UserEntity userEntity;

    public OnResetPasswordRequestEvent(UserEntity userEntity, VerificationTokenEntity passwordResetTokenEntity) {
        super(userEntity);
        this.userEntity = userEntity;
        this.passwordResetTokenEntity = passwordResetTokenEntity;
    }
}
