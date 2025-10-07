package com.tyse.scrutiny.gateway.service.dto;

import java.io.Serializable;

public class RecaptchaResponseDTO implements Serializable {

    private boolean success = false;

    public RecaptchaResponseDTO() {}

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
