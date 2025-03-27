package org.emailservice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.SnsException;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;


import java.util.Map;
import java.util.HashMap;

public class SendMail implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    // Environment variable keys
    private static final String ENV_SNS_TOPIC_ARN = "SNS_TOPIC_ARN";
    private static final String ENV_CORS_ALLOWED_ORIGIN = "CORS_ALLOWED_ORIGIN";
    
    // Default values if environment variables are not set
    private static final String DEFAULT_CORS_ALLOWED_ORIGIN = "*";
    
    // HTTP header constants
    private static final String CONTENT_TYPE = "Content-Type";
    

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

        String body = input.getBody();
        String topicArn = System.getenv(ENV_SNS_TOPIC_ARN);
        
        // Get CORS allowed origin from environment variables, defaulting to "*"
        String corsAllowedOrigin = System.getenv(ENV_CORS_ALLOWED_ORIGIN);
        if (corsAllowedOrigin == null || corsAllowedOrigin.isEmpty()) {
            corsAllowedOrigin = DEFAULT_CORS_ALLOWED_ORIGIN;
        }
        
        // Check if SNS Topic ARN is configured
        if (topicArn == null || topicArn.isEmpty()) {
            context.getLogger().log("Error: SNS_TOPIC_ARN environment variable is not set");
            return createErrorResponse(500, "Server configuration error: SNS_TOPIC_ARN not set");
        }

        // Set up response
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        Map<String, String> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, "application/json");
        headers.put("Access-Control-Allow-Origin", corsAllowedOrigin);
        headers.put("Access-Control-Allow-Headers", CONTENT_TYPE);
        headers.put("Access-Control-Allow-Methods", "OPTIONS,POST");



        // Body validation
        if (body == null || body.isEmpty()) {
            context.getLogger().log("Error: Request body is empty or null");
            return response
                    .withStatusCode(400)
                    .withHeaders(headers)
                    .withBody("{\"error\": \"Request body is empty or null\"}");
        }

        try {
            // Parse the incoming JSON directly to a Map
            Gson gson = new Gson();
            Map<String, String> userDataMap = gson.fromJson(body, Map.class);

            context.getLogger().log("Parsed data: " + userDataMap);

            if (userDataMap.containsKey("name") && userDataMap.containsKey("email") && userDataMap.containsKey("message")) {
                // Create a MailObject and set the fields from the map
                MailObject userData = new MailObject(userDataMap.get("name"), userDataMap.get("email"), userDataMap.get("message"));

                SnsClient snsClient = SnsClient.builder()
                        .region(Region.EU_WEST_1)
                        .credentialsProvider(DefaultCredentialsProvider.create())
                        .build();

                boolean published = pubTopic(snsClient, userData.getMessage(), topicArn, context);

                snsClient.close();

                if (!published) {
                    throw new Exception("Failed to send publish message");
                }

                Gson jsonResponse = new Gson();
                String json = jsonResponse.toJson(userData);
                return response
                        .withStatusCode(200)
                        .withHeaders(headers)
                        .withBody(json);
            } else {
                context.getLogger().log("Error: User data is empty or null");
                return response
                        .withStatusCode(400)
                        .withHeaders(headers)
                        .withBody("{\"error\": \"Name , Email and Message are all required \"}");
            }

        } catch (Exception e) {
            context.getLogger().log("Error parsing JSON: " + e.getMessage());
            context.getLogger().log("Stack trace: " + e.getStackTrace()[0]);
            return response
                    .withStatusCode(500)
                    .withHeaders(headers)
                    .withBody("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Helper method to create an error response
     */
    private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String errorMessage) {
        Map<String, String> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, "application/json");
        String corsAllowedOrigin = System.getenv(ENV_CORS_ALLOWED_ORIGIN);
        if (corsAllowedOrigin == null || corsAllowedOrigin.isEmpty()) {
            corsAllowedOrigin = DEFAULT_CORS_ALLOWED_ORIGIN;
        }
        headers.put("Access-Control-Allow-Origin", corsAllowedOrigin);
        headers.put("Access-Control-Allow-Headers", CONTENT_TYPE);
        headers.put("Access-Control-Allow-Methods", "OPTIONS,POST");
        
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody("{\"error\": \"" + errorMessage + "\"}");
    }

    /**
     * Helper method to publish a message to an SNS topic
     */
    public static boolean pubTopic(SnsClient snsClient, String message, String topicArn, Context context) {
        try {
            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .topicArn(topicArn)
                    .build();

             snsClient.publish(request);

            return true;

        } catch (SnsException e) {
            context.getLogger().log("SNS Error: " + e.awsErrorDetails().errorMessage());
            return false;
        }
    }
}