package com.pullstackdeveloper.ntdcalculatoroperation.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenValidationResponse {
    private boolean valid;
    private User user;

    public TokenValidationResponse(boolean valid, User user) {
        this.valid = valid;
        this.user = user;
    }

    public boolean isValid() {
        return valid;
    }

    public User getUser() {
        return user;
    }
}
