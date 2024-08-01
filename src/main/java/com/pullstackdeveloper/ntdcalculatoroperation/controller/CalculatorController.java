package com.pullstackdeveloper.ntdcalculatoroperation.controller;

import com.pullstackdeveloper.ntdcalculatoroperation.model.OperationRequest;
import com.pullstackdeveloper.ntdcalculatoroperation.service.CalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class CalculatorController {

    @Autowired
    private CalculatorService calculatorService;

    @PostMapping("/calculate")
    public ResponseEntity<?> calculate(@RequestHeader("Authorization") String token, @RequestBody OperationRequest request) {
        try {
            Object result = calculatorService.calculate(request, token.substring(7));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
