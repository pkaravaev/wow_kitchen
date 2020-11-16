package com.foodtech.back.service.http;

import lombok.Data;

@Data
public class BasicAuth {

    private String username;
    private String password;

    BasicAuth(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
