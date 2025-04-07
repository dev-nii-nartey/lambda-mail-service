package org.emailservice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SesHandler implements RequestHandler<Map<String, String>, Map<String, String>> {

    private static final String SENDER_EMAIL = "nii.tetteh@amalitech.com";
    private static final String TEMPLATE_NAME = "ContactFormAcknowledgement";
    private static final Region AWS_REGION = Region.EU_WEST_1;
    private static final Gson gson = new Gson();

    @Override
    public Map<String, String> handleRequest(final Map<String, String> input, Context context) {
        try {
            // Get values from input and strip any wrapping quotes
            String firstName = stripQuotes(input.get("firstName"));
            String lastName = stripQuotes(input.get("lastName"));
            String email = stripQuotes(input.get("email"));
            String company = stripQuotes(input.get("company"));
            String country = stripQuotes(input.get("country"));
            String message = stripQuotes(input.get("message"));
            String inquiryType = stripQuotes(input.get("inquiryType"));
            String getUpdates;

            if (input.get("getUpdates") == null) {
                getUpdates = "false";
            } else if (Objects.equals(stripQuotes(input.get("getUpdates")), "true")) {
                getUpdates = "true";
            } else {
                getUpdates = "false";
            }

            context.getLogger().log("firstName: [" + firstName + "]");
            context.getLogger().log("lastName: [" + lastName + "]");
            context.getLogger().log("email: [" + email + "]");
            context.getLogger().log("company: [" + company + "]");
            context.getLogger().log("country: [" + country + "]");
            context.getLogger().log("message: [" + message + "]");
            context.getLogger().log("inquiryType: [" + inquiryType + "]");
            context.getLogger().log("getUpdates: [" + getUpdates + "]");

            String contactId = UUID.randomUUID().toString();

            // Send acknowledgement email via SES
            sendAcknowledgementEmail(firstName, lastName, email, company, inquiryType, message, contactId, context);

            // Set up response
            Map<String, String> response = new HashMap<>();
            response.put("name", firstName + " " + lastName);
            response.put("message", message);
            response.put("email", email);
            response.put("inquiryType", inquiryType);
            response.put("id", contactId);

            context.getLogger().log("response: [" + response + "]");

            return response;
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String stripQuotes(String value) {
        if (value == null) return "";
        return value.replaceAll("^\"|\"$", "");
    }

    private void sendAcknowledgementEmail(String firstName, String lastName, String recipientEmail,
                                          String company, String inquiryType, String message,
                                          String contactId, Context context) {
        try {
            SesV2Client sesClient = SesV2Client.builder()
                    .region(AWS_REGION)
                    .build();

            // Create destination
            Destination destination = Destination.builder()
                    .toAddresses(recipientEmail)
                    .build();

            // Use Gson to create proper JSON for template data
            JsonObject templateDataObj = new JsonObject();
            templateDataObj.addProperty("firstName", firstName);
            templateDataObj.addProperty("lastName", lastName);
            templateDataObj.addProperty("fullName", firstName + " " + lastName);
            templateDataObj.addProperty("company", company);
            templateDataObj.addProperty("inquiryType", inquiryType);
            templateDataObj.addProperty("message", message);
            templateDataObj.addProperty("contactId", contactId);
            templateDataObj.addProperty("currentDate", java.time.LocalDate.now().toString());

            String templateData = gson.toJson(templateDataObj);
            context.getLogger().log("Template data: " + templateData);

            // Create the template email content
            Template template = Template.builder()
                    .templateName(TEMPLATE_NAME)
                    .templateData(templateData)
                    .build();

            EmailContent emailContent = EmailContent.builder()
                    .template(template)
                    .build();

            // Create the send email request
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .destination(destination)
                    .content(emailContent)
                    .fromEmailAddress(SENDER_EMAIL)
                    .build();

            // Send the email
            sesClient.sendEmail(emailRequest);
            context.getLogger().log("Acknowledgement email sent successfully to: " + recipientEmail);
        } catch (SesV2Exception e) {
            context.getLogger().log("Error sending email: " + e.getMessage());
            throw e;
        }
    }
}