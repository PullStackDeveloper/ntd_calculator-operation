package com.pullstackdeveloper.ntdcalculatoroperation.service;

import com.pullstackdeveloper.ntdcalculatoroperation.entity.Operation;
import com.pullstackdeveloper.ntdcalculatoroperation.repository.OperationRepository;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CalculatorCostService {

    @Autowired
    private OperationRepository operationRepository;

    public double getOperationCost(String operator) {
        Operation operation = operationRepository.findByType(operator);
        if (operation != null) {
            return operation.getCost();
        } else {
            throw new IllegalArgumentException("Invalid operator");
        }
    }
}