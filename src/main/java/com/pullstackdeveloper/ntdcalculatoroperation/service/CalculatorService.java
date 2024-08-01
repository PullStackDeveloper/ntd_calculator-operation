package com.pullstackdeveloper.ntdcalculatoroperation.service;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import com.pullstackdeveloper.ntdcalculatoroperation.model.Balance;
import com.pullstackdeveloper.ntdcalculatoroperation.model.OperationRequest;
import com.pullstackdeveloper.ntdcalculatoroperation.model.Record;
import com.pullstackdeveloper.ntdcalculatoroperation.model.TokenValidationResponse;
import com.pullstackdeveloper.ntdcalculatoroperation.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class CalculatorService {

    @Autowired
    private CalculatorCostService calculatorCostService;

    @Autowired
    private TokenValidationService tokenValidationService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private RecordService recordService;

    // Declare IFn variables to hold references to Clojure functions
    private final IFn addNumbers;
    private final IFn subtractNumbers;
    private final IFn multiplyNumbers;
    private final IFn divideNumbers;
    private final IFn sqrtNumber;
    private final IFn generateRandomString;

    // Constructor to initialize the Clojure functions
    public CalculatorService() {
        // Load the Clojure functions from the "clojure.functions" namespace
        Clojure.var("clojure.core", "require").invoke(Clojure.read("clojure.functions"));
        addNumbers = Clojure.var("clojure.functions", "add-numbers");
        subtractNumbers = Clojure.var("clojure.functions", "subtract-numbers");
        multiplyNumbers = Clojure.var("clojure.functions", "multiply-numbers");
        divideNumbers = Clojure.var("clojure.functions", "divide-numbers");
        sqrtNumber = Clojure.var("clojure.functions", "sqrt-number");
        generateRandomString = Clojure.var("clojure.functions", "generate-random-string");
    }

    // Method to perform the calculation based on the request and token
    @Transactional
    public Map<String, Object> calculate(OperationRequest request, String token) {
        Map<String, Object> response = new HashMap<>();

        // Validate the token and get the user
        TokenValidationResponse validationResponse = tokenValidationService.validateToken(token);
        if (!validationResponse.isValid()) {
            throw new SecurityException("Invalid token");
        }
        User user = validationResponse.getUser();

        // Check if operands are valid numbers
        Double firstOperand;
        Double secondOperand;
        try {
            firstOperand = request.getFirstOperand() != null ? Double.parseDouble(request.getFirstOperand()) : 0;
            secondOperand = request.getSecondOperand() != null ? Double.parseDouble(request.getSecondOperand()) : 0;
        } catch (NumberFormatException e) {
            response.put("value", "Invalid number format");
            return response;
        }

        String operator = request.getOperator();
        Double cost = calculatorCostService.getOperationCost(operator);

        // Check user balance
        Balance balance = balanceService.getBalance(token);
        if (balance.getAmount() < cost) {
            response.put("value", "Insufficient balance");
            return response;
        }

        Object result;
        try {
            // Perform the calculation based on the operator
            switch (operator) {
                case "addition":
                    result = addNumbers.invoke(firstOperand, secondOperand);
                    break;
                case "subtraction":
                    result = subtractNumbers.invoke(firstOperand, secondOperand);
                    break;
                case "multiplication":
                    result = multiplyNumbers.invoke(firstOperand, secondOperand);
                    break;
                case "division":
                    if (secondOperand == 0) {
                        throw new IllegalArgumentException("Division by zero");
                    }
                    result = divideNumbers.invoke(firstOperand, secondOperand);
                    break;
                case "square_root":
                    if (firstOperand < 0) {
                          result = "Error: Square root of negative number";
                          break;
//                        throw new IllegalArgumentException("Square root of negative number");
                    }
                    result = sqrtNumber.invoke(firstOperand);
                    break;
                case "random_string":
                    result = generateRandomString.invoke().toString();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid operator");
            }

            // Update balance
            balanceService.updateBalance(balance, balance.getAmount() - cost, token);

            // Create record of the operation
            Record record = new Record();
            record.setOperationType(operator);
            record.setAmount(cost);
            record.setUserBalance(balance.getAmount() - cost);
            record.setOperationResponse(result.toString());
            record.setUserId(user.getId());
            record.setDate(new Date());
            recordService.createRecord(record, token);

            // Return the calculation result
            response.put("value", result);
        } catch (Exception e) {
            // Compensate if any exception occurs
            compensateOperation(balance, cost, token);
            throw new RuntimeException("Calculation failed", e);
        }

        return response;
    }

    private void compensateOperation(Balance balance, Double cost, String token) {
        try {// Revert balance update
            balanceService.updateBalance(balance, balance.getAmount() + cost, token);
        } catch (Exception e) {
            // Log the error or handle further
        }
    }
}
