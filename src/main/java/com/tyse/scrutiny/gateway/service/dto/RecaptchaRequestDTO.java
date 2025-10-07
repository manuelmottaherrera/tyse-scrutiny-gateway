package com.tyse.scrutiny.gateway.service.dto;

import java.io.Serializable;

public class RecaptchaRequestDTO implements Serializable {

    private String token;
    private String action;

    public RecaptchaRequestDTO() {}

    public RecaptchaRequestDTO(String token, String action) {
        this.token = token;
        this.action = action;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
