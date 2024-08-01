package com.pullstackdeveloper.ntdcalculatoroperation.service;

import com.pullstackdeveloper.ntdcalculatoroperation.infrastructure.client.TokenValidationClient;
import com.pullstackdeveloper.ntdcalculatoroperation.model.TokenValidationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenValidationService {

    @Autowired
    private TokenValidationClient tokenValidationClient;

    public TokenValidationResponse validateToken(String token) {
        return tokenValidationClient.validateToken(token);
    }
}
