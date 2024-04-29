package com.socialnetwork.boardrift.util.event;

import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

@Getter
@Setter
public class OnRegistrationCompleteEvent extends ApplicationEvent {
    private String appUrl;
    private Locale locale;
    private UserEntity userEntity;

    public OnRegistrationCompleteEvent(UserEntity userEntity, Locale locale, String appUrl) {
        super(userEntity);
        this.userEntity = userEntity;
        this.locale = locale;
        this.appUrl = appUrl;
    }
}
