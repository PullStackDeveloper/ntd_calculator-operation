package com.pullstackdeveloper.ntdcalculatoroperation.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CustomRequestWrapper extends HttpServletRequestWrapper {
    private byte[] requestBody;

    public CustomRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        requestBody = request.getInputStream().readAllBytes();
    }

    public OperationRequest getOperationRequest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(requestBody, OperationRequest.class);
    }

    public void setOperationRequest(OperationRequest operationRequest) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestBody = objectMapper.writeValueAsBytes(operationRequest);
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(requestBody);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // No implementation required
            }

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }
}