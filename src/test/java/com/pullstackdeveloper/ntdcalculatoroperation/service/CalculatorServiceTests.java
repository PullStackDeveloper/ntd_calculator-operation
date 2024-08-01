package com.pullstackdeveloper.ntdcalculatoroperation.service;

import com.pullstackdeveloper.ntdcalculatoroperation.model.*;
import com.pullstackdeveloper.ntdcalculatoroperation.model.Record;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CalculatorServiceTests {

    @Mock
    private CalculatorCostService calculatorCostService;

    @Mock
    private TokenValidationService tokenValidationService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private RecordService recordService;

    @InjectMocks
    private CalculatorService calculatorService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCalculate_Success() {
        OperationRequest request = new OperationRequest();
        request.setFirstOperand("10");
        request.setOperator("addition");
        request.setSecondOperand("20");

        String token = "valid_token";

        TokenValidationResponse validationResponse = new TokenValidationResponse(true, new User());
        when(tokenValidationService.validateToken(token)).thenReturn(validationResponse);
        when(calculatorCostService.getOperationCost("addition")).thenReturn(1.0);
        Balance balance = new Balance();
        balance.setAmount(10.0);
        when(balanceService.getBalance(eq(token))).thenReturn(balance);
        doNothing().when(balanceService).updateBalance(any(), anyDouble(), eq(token));
        doNothing().when(recordService).createRecord(any(Record.class), eq(token));

        Map<String, Object> result = calculatorService.calculate(request, token);

        assertNotNull(result);
        assertEquals(30.0, result.get("value"));
        verify(balanceService, times(1)).updateBalance(any(Balance.class), anyDouble(), eq(token));
        verify(recordService, times(1)).createRecord(any(Record.class), eq(token));
    }

    @Test
    public void testCalculate_InvalidToken() {
        OperationRequest request = new OperationRequest();
        String token = "invalid_token";

        when(tokenValidationService.validateToken(token)).thenReturn(new TokenValidationResponse(false, null));

        SecurityException exception = assertThrows(SecurityException.class, () -> calculatorService.calculate(request, token));
        assertEquals("Invalid token", exception.getMessage());
    }
}
