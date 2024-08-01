package com.pullstackdeveloper.ntdcalculatoroperation.filter;

import com.pullstackdeveloper.ntdcalculatoroperation.model.CustomRequestWrapper;
import com.pullstackdeveloper.ntdcalculatoroperation.model.OperationRequest;
import com.pullstackdeveloper.ntdcalculatoroperation.model.TokenValidationResponse;
import com.pullstackdeveloper.ntdcalculatoroperation.service.CalculatorCostService;
import com.pullstackdeveloper.ntdcalculatoroperation.service.TokenValidationService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomRequestFilter implements Filter {

    @Autowired
    private CalculatorCostService calculatorCostService;

    @Autowired
    private TokenValidationService tokenValidationService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && "POST".equalsIgnoreCase(((HttpServletRequest) request).getMethod())) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String authHeader = httpRequest.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                TokenValidationResponse validationResponse = tokenValidationService.validateToken(token);
                if (!validationResponse.isValid()) {
                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                    return;
                }
//                System.out.println("Token Validation Response Body: " + validationResponse.getResponseBody());
            } else {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing token");
                return;
            }

            CustomRequestWrapper requestWrapper = new CustomRequestWrapper((HttpServletRequest) request);

            OperationRequest operationRequest = requestWrapper.getOperationRequest();

            double cost = calculatorCostService.getOperationCost(operationRequest.getOperator());
            operationRequest.setCost(cost);

            requestWrapper.setOperationRequest(operationRequest);

            chain.doFilter(requestWrapper, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}