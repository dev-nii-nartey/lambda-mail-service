package org.emailservice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SendMailTest {

    private SendMail sendMail;
    
    @Mock
    private Context context;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        sendMail = new SendMail();
    }

    @Test
    public void testMissingRequestBody() {
        // Arrange
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody(null);

        // Act
        APIGatewayProxyResponseEvent response = sendMail.handleRequest(request, context);

        // Assert
        assertEquals(400, response.getStatusCode());
        assertEquals("{\"error\": \"Request body is empty or null\"}", response.getBody());
    }

    @Test
    public void testMissingRequiredFields() {
        // Arrange
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        Map<String, String> incompleteData = new HashMap<>();
        incompleteData.put("name", "Test User");
        // Missing email and message
        
        Gson gson = new Gson();
        request.setBody(gson.toJson(incompleteData));

        // Act
        APIGatewayProxyResponseEvent response = sendMail.handleRequest(request, context);

        // Assert
        assertEquals(400, response.getStatusCode());
        assertEquals("{\"error\": \"Name , Email and Message are all required \"}", response.getBody());
    }
} 