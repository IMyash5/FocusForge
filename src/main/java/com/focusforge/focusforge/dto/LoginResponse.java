
package com.focusforge.focusforge.dto;

public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private Long userId;
    private String email;
    private long expiresIn;

    public LoginResponse() {
    }

    public LoginResponse(
            String accessToken,
            Long userId,
            String email,
            long expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.email = email;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}
