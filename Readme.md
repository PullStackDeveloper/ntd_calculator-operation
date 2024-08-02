# NTD Calculator Operation Documentation

## Table of Contents
1. [Overview](#overview)
2. [Installation](#installation)
3. [Project Structure](#project-structure)
4. [Configuration](#configuration)
5. [Services and Controllers](#services-and-controllers)
6. [Token Validation](#token-validation)
7. [Docker Setup for MySQL](#docker-setup-for-mysql)
8. [Clojure Integration](#clojure-integration)

## Overview

The NTD Calculator Operation service is a Spring Boot application designed to handle various calculator operations. It includes functionalities such as token validation, balance management, and calculation of operation costs. This documentation will guide you through the installation, configuration, and main components of the service.

## Installation

### Prerequisites
- Java 17
- Maven
- Docker

### Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-repo/ntd-calculator-operation.git
   cd ntd-calculator-operation
   ```

2. **Build the project:**
   ```bash
   mvn clean install
   ```

3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

4. **Docker Setup for MySQL:**
    - Pull the Docker image:
      ```bash
      docker pull fernando0988/ntd_calculator_image:latest
      ```
    - Run the Docker container:
      ```bash
      docker run --name ntd_calculator-container -e MYSQL_ROOT_PASSWORD=set-your-password -e MYSQL_DATABASE=ntd_calculator -e MYSQL_USER=ntd_user -e MYSQL_PASSWORD=set-your-password -p 3306:3306 -d fernando0988/ntd_calculator_image:latest
      ```

## Configuration

### `application.properties`
This file contains configuration properties for the Spring Boot application.

```properties
spring.datasource.url=jdbc:mysql://your-database-url/ntd_calculator
spring.datasource.username=ntd_user
spring.datasource.password=your-password
spring.jpa.hibernate.ddl-auto=update

validation.url=token-validation-service/api/validate
balance-record.api.url=balance-record-service/api/record
```

## Services and Controllers

### Controllers

#### `CalculatorController.java`

This controller handles calculation requests.

- **Endpoint:** `/calculate`
- **Method:** `POST`
- **Description:** Accepts an `OperationRequest` and performs the specified calculation after validating the token and checking the user's balance.
- **Headers:** `Authorization` (Bearer Token)
- **Response:** Calculation result or error message.

```java
@RestController
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
```

### Services

#### `CalculatorService.java`

- **Description:** Handles the core calculation logic.
- **Methods:**
    - `calculate(OperationRequest request, String token)`: Validates the token, checks the user's balance, performs the calculation, updates the balance, and records the operation.

```java
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
        try {
            balanceService.updateBalance(balance, balance.getAmount() + cost, token);
        } catch (Exception e) {
            // Log the error or handle further
        }
    }
}
```

#### `BalanceService.java`
- **Description:** Manages user balance

operations.
- **Methods:**
    - `getBalance(String token)`: Retrieves the user's balance.
    - `updateBalance(Balance balance, double amount, String token)`: Updates the user's balance.

#### `TokenValidationService.java`
- **Description:** Validates tokens using an external service.
- **Methods:**
    - `validateToken(String token)`: Validates the provided token and returns the validation response.

#### `RecordService.java` and `RecordServiceImpl.java`
- **Description:** Manages recording of operations.
- **Methods:**
    - `createRecord(Record record, String token)`: Creates a record of the performed operation.

#### `CalculatorCostService.java`
- **Description:** Retrieves the cost of operations.
- **Methods:**
    - `getOperationCost(String operator)`: Gets the cost for the specified operator.

## Token Validation

The token validation is handled by an external service. The `TokenValidationClient.java` component communicates with this service to validate tokens.

```java
@Component
public class TokenValidationClient {

    @Value("${validation.url}")
    private String validationUrl;

    public TokenValidationResponse validateToken(String token) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(validationUrl);
            request.setHeader("Authorization", "Bearer " + token);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject jsonResponse = new JSONObject(responseBody);

                boolean isValid = response.getCode() == 200 && jsonResponse.get("status").equals("active");

                User user = new User();
                user.setId(jsonResponse.getLong("id"));
                user.setUsername(jsonResponse.getString("username"));
                user.setPassword(jsonResponse.getString("password"));
                user.setStatus(jsonResponse.getString("status"));

                return new TokenValidationResponse(isValid, user);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return new TokenValidationResponse(false, null);
        }
    }
}
```

## Docker Setup for MySQL

To set up the MySQL database required for this application, you can use Docker. Follow these steps:

1. **Pull the Docker image:**
   ```bash
   docker pull fernando0988/ntd_calculator_image:latest
   ```

2. **Run the Docker container:**
   ```bash
   docker run --name ntd_calculator-container -e MYSQL_ROOT_PASSWORD=set-your-password -e MYSQL_DATABASE=ntd_calculator -e MYSQL_USER=ntd_user -e MYSQL_PASSWORD=set-your-password -p 3306:3306 -d fernando0988/ntd_calculator_image:latest
   ```

This setup will create and run a MySQL database container that the application can connect to for storing and retrieving data.

## Clojure Integration

The project integrates Clojure functions to perform the core mathematical operations. The functions are defined in the `clojure/functions.clj` file and loaded in the `CalculatorService.java`.

### Clojure Functions

```clojure
(ns clojure.functions)

(defn add-numbers [a b]
  (+ a b))

(defn subtract-numbers [a b]
  (- a b))

(defn multiply-numbers [a b]
  (* a b))

(defn divide-numbers [a b]
  (if (zero? b)
    (throw (IllegalArgumentException. "Division by zero"))
    (/ a b)))

(defn sqrt-number [a]
  (if (neg? a)
    (throw (IllegalArgumentException. "Square root of negative number"))
    (Math/sqrt a)))

(defn generate-random-string []
  (slurp "https://www.random.org/strings/?num=1&len=10&digits=on&upperalpha=on&loweralpha=on&unique=on&format=plain&rnd=new"))
```

These functions are used in the `CalculatorService.java` to perform calculations based on the operation type.

### Loading Clojure Functions

The Clojure functions are loaded in the `CalculatorService` constructor:

```java
@Service
public class CalculatorService {

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
}
```

This setup ensures that the mathematical operations leverage the powerful capabilities of Clojure.