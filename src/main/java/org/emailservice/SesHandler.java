package org.emailservice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SesHandler implements RequestHandler<Map<String, String>, Map<String, String>> {

    private static final String SENDER_EMAIL = "nii.tetteh@amalitech.com"; // Replace with your verified SES email
    private static final String TEMPLATE_NAME = "ContactFormAcknowledgement";
    private static final Region AWS_REGION = Region.EU_WEST_1;

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

            context.getLogger().log("response: [" + response + " ]");

            return response;
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
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

            // Prepare template data - all variables that will be used in the template
            String templateData = String.format(
                    "{"
                            + "\"firstName\": \"%s\","
                            + "\"lastName\": \"%s\","
                            + "\"fullName\": \"%s %s\","
                            + "\"company\": \"%s\","
                            + "\"inquiryType\": \"%s\","
                            + "\"message\": \"%s\","
                            + "\"contactId\": \"%s\","
                            + "\"currentDate\": \"%s\""
                            + "}",
                    firstName, lastName, firstName, lastName, company, inquiryType,
                    message.replace("\"", "\\\"").replace("\n", "\\n"),
                    contactId, java.time.LocalDate.now());

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