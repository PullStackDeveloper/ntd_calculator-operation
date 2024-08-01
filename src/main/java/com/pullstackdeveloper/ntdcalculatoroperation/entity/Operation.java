package com.pullstackdeveloper.ntdcalculatoroperation.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Setter
@Getter
@Entity
public class Operation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private Double cost;
    public Operation() {
    }
    public Operation(Long id, String type, Double cost) {
        this.id = id;
        this.type = type;
        this.cost = cost;
    }
}