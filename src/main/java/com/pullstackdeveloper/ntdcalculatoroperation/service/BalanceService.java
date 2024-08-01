package com.pullstackdeveloper.ntdcalculatoroperation.service;

import com.pullstackdeveloper.ntdcalculatoroperation.model.Balance;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class BalanceService {

    @Value("${balance-record.api.url}")
    private String balanceUrl;

    public Balance getBalance( String token) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(balanceUrl + "/balance/");
            request.setHeader("Authorization", "Bearer " + token);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getCode() == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    Balance balance = new Balance();
                    balance.setId(jsonResponse.getLong("id"));
                    balance.setAmount(jsonResponse.getDouble("amount"));
                    balance.setUserId(jsonResponse.getLong("userId"));
                    return balance;
                } else {
                    throw new RuntimeException("Failed to get balance");
                }
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to get balance", e);
        }
    }

    public void updateBalance(Balance balance, double amount, String token) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPatch request = new HttpPatch(balanceUrl + "/balance/");
            request.setHeader("Authorization", "Bearer " + token);
            request.setHeader("Content-Type", "application/json");
            JSONObject json = new JSONObject();

            json.put("amount", amount);
            StringEntity entity = new StringEntity(json.toString(), StandardCharsets.UTF_8);
            request.setEntity(entity);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getCode() != 200) {
                    throw new RuntimeException("Failed to update balance");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to update balance", e);
        }
    }
}
