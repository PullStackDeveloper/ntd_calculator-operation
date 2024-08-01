package com.pullstackdeveloper.ntdcalculatoroperation.infrastructure.client;

import com.pullstackdeveloper.ntdcalculatoroperation.model.TokenValidationResponse;
import com.pullstackdeveloper.ntdcalculatoroperation.model.User;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.ParseException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TokenValidationClient {

    @Value("${validation.url}")
    private String validationUrl;

    public TokenValidationResponse validateToken(String token) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(validationUrl);
            request.setHeader("Authorization", "Bearer " + token);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject jsonResponse = new JSONObject(responseBody);

                boolean isValid = response.getCode() == 200 && jsonResponse.get("status").equals("active");

                User user = new User();
                user.setId(jsonResponse.getLong("id"));
                user.setUsername(jsonResponse.getString("username"));
                user.setPassword(jsonResponse.getString("password"));
                user.setStatus(jsonResponse.getString("status"));

                return new TokenValidationResponse(isValid, user);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return new TokenValidationResponse(false, null);
        }
    }
}
