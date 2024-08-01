package com.pullstackdeveloper.ntdcalculatoroperation.service;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.pullstackdeveloper.ntdcalculatoroperation.model.Record;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class RecordServiceImpl implements RecordService {

    @Value("${balance-record.api.url}")
    private String recordUrl;

    @Override
    public void createRecord(Record record, String token) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(recordUrl + "/records");
            request.setHeader("Authorization", "Bearer " + token);
            request.setHeader("Content-Type", "application/json");

            JSONObject json = new JSONObject();
            json.put("operationType", record.getOperationType());
            json.put("amount", record.getAmount());
            json.put("userBalance", record.getUserBalance());
            json.put("operationResponse", record.getOperationResponse());
            json.put("userId", record.getUserId());

            StringEntity entity = new StringEntity(json.toString(), StandardCharsets.UTF_8);
            request.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                System.out.println("Response Cod: " + response.getCode());
                if (response.getCode() != 200 && response.getCode() != 201) {
                    throw new RuntimeException("Failed to create record");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create record", e);
        }
    }
}