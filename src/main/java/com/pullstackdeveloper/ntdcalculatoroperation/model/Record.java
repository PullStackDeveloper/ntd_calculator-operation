package com.pullstackdeveloper.ntdcalculatoroperation.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Record {
    private Long id;
    private String operationType;
    private Long userId;
    private Double amount;
    private Double userBalance;
    private String operationResponse;
    private Date date;
}