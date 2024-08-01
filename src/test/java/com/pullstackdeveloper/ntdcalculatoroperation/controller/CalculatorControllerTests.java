package com.pullstackdeveloper.ntdcalculatoroperation.controller;

import com.pullstackdeveloper.ntdcalculatoroperation.model.OperationRequest;
import com.pullstackdeveloper.ntdcalculatoroperation.service.CalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CalculatorControllerTests {

    @Mock
    private CalculatorService calculatorService;

    @InjectMocks
    private CalculatorController calculatorController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCalculate_Success() {
        OperationRequest request = new OperationRequest();
        String token = "Bearer valid_token";
        when(calculatorService.calculate(any(), anyString())).thenReturn(new HashMap<>());

        ResponseEntity<?> response = calculatorController.calculate(token, request);

        assertEquals(200, response.getStatusCodeValue());
        verify(calculatorService, times(1)).calculate(any(OperationRequest.class), anyString());
    }

    @Test
    public void testCalculate_InvalidToken() {
        OperationRequest request = new OperationRequest();
        String token = "Bearer invalid_token";
        when(calculatorService.calculate(any(), anyString())).thenThrow(new SecurityException("Invalid token"));

        ResponseEntity<?> response = calculatorController.calculate(token, request);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
    }
}
