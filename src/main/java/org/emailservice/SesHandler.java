package org.emailservice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SesHandler implements RequestHandler<Map<String, String>, Map<String, String>> {

    @Override
    public Map<String, String> handleRequest(final Map<String, String> input, Context context) {


        try {
            String firstName = input.get("firstName");
            String lastName = input.get("lastName");
            String email = input.get("workEmail");
            String company = input.get("company");
            String country = input.get("country");
            String message = input.get("message");
            String inquiryType = input.get("inquiryType");
            String getUpdates;

            if (input.get("getUpdates") == null) {
                getUpdates = "false";
            } else if (Objects.equals(input.get("getUpdates"), "true")) {
                getUpdates = "true";
            } else {
                getUpdates = "false";
            }


            context.getLogger().log("firstName: [" + firstName + " ]");
            context.getLogger().log("lastName: [" + lastName + " ]");
            context.getLogger().log("email: [" + email + " ]");
            context.getLogger().log("company: [" + company + " ]");
            context.getLogger().log("country: [" + country + " ]");
            context.getLogger().log("message: [" + message + " ]");
            context.getLogger().log("inquiryType: [" + inquiryType + " ]");
            context.getLogger().log("getUpdates: [" + getUpdates + " ]");


            // Set up response
            Map<String, String> response = new HashMap<>();
            response.put("name", firstName + " " + lastName);
            response.put("message", message);
            response.put("email", email);
            response.put("inquiryType", inquiryType);
            response.put("id", UUID.randomUUID().toString());

            context.getLogger().log("response: [" + response + " ]");

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
