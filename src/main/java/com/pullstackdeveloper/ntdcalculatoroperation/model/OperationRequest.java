package com.pullstackdeveloper.ntdcalculatoroperation.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

//Using lombok to generate getters and setters
@Setter
@Getter
public class OperationRequest {
    // Getters and Setters
    private String firstOperand;
    private String operator;
    private String secondOperand;
    private Double cost;

}