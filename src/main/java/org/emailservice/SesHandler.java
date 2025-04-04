package org.emailservice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.HashMap;
import java.util.Map;

public class SesHandler implements RequestHandler<Map<String, String>, Map<String, String>> {

    @Override
    public Map<String, String> handleRequest(final Map<String, String> input, Context context) {


        String key1 = input.get("name");
        String key2 = input.get("email");
        String key3 = input.get("message");
        String key4 = input.get("inquiryType");


        // Set up response
        Map<String, String> response = new HashMap<>();
        response.put("name", key1);
        response.put("message", key2);
        response.put("email", key3);
        response.put("inquiryType", key4);

        context.getLogger().log("response: [" + response + " ]");

        return response;
    }

}
