# Lambda Email Service

A serverless AWS Lambda function that forwards contact form submissions to an SNS topic for email notifications.

## Project Description

This service provides an API endpoint that accepts form data (name, email, message) from frontend applications like websites or mobile apps. The Lambda function validates the input, then publishes the message to an AWS SNS topic which can be configured to send emails to subscribers.

## Features

- Handles API Gateway requests for contact form submissions
- Validates incoming data (name, email, message)
- Publishes messages to an SNS topic
- CORS headers for frontend integration

## Setup & Deployment

### Prerequisites

- Java 21
- Maven
- AWS Account with appropriate permissions
- AWS CLI configured

### Building the Project

```bash
mvn clean package
```

This will create a shaded JAR file in the `target` directory that includes all dependencies.

### Deploying to AWS Lambda

1. Create an SNS topic in your AWS account
2. Configure SNS topic to send emails to desired recipients
3. Create a Lambda function in AWS Console
4. Upload the JAR file from `target/lambda-email-1.0-SNAPSHOT.jar`
5. Set the handler to: `org.emailservice.SendMail::handleRequest`
6. Set the environment variables (see Environment Variables section)
7. Create an API Gateway trigger
8. Configure appropriate IAM permissions for the Lambda function to publish to SNS

## Environment Variables

The following environment variables should be set in the Lambda function configuration:

| Variable            | Description           | Example |
|---------------------|-----------------------|---------|
| SNS_TOPIC_ARN       | ARN of the SNS topic  | arn:aws:sns:eu-west-1:123456789012:Email-Notification |
| CORS_ALLOWED_ORIGIN | Domain(s) allowed to access the API | * |

## Example Usage

### Sample Request

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "message": "Hello, I'd like to discuss a project with you."
}
```

### Sample Response (Success)

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "message": "Hello, I'd like to discuss a project with you."
}
```

### Sample Response (Error)

```json
{
  "error": "Name, Email and Message are all required"
}
```

## License

MIT 