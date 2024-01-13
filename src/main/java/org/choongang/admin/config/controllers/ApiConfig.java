package org.choongang.admin.config.controllers;

import lombok.Data;

@Data
public class ApiConfig {
    private String publicOpenApiKey; // 공공 API 인증키

    private String kakaoJavascriptKey; // 카카오 API - 자바스크립트 앱 키
}
