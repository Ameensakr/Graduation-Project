# Forgot Password & OTP Implementation Guide

This guide provides a step-by-step approach to implementing a "Forgot Password" feature with an OTP (One-Time Password) sent via email in your Spring Boot & MongoDB application.

## Step 1: Add Email Dependency
To send emails from your Spring Boot application, you need the `spring-boot-starter-mail` dependency.

Add this to your `pom.xml` inside `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

## Step 2: Configure Application Properties
You need to configure your email server (SMTP) in `src/main/resources/application.properties` or `application.yml`. If you are using Gmail, it looks like this:

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
# For Gmail, you must generate an "App Password" from your Google Account settings
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## Step 3: Create OTP Data Structure
You need a place to store the generated OTPs so you can verify them later. Since you're using MongoDB, you can create a new document collection for OTPs. 

Create an `OtpToken` class:

```java
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "otp_tokens")
public class OtpToken {
    @Id
    private String id;
    private String email;
    private String otp;
    private LocalDateTime expirationTime;
}
```

*Note: You should also create a standard Spring Data repository interface for this (`OtpTokenRepository extends MongoRepository<OtpToken, String>`) with a method like `Optional<OtpToken> findByEmailAndOtp(String email, String otp)`.*

## Step 4: Create DTOs (Data Transfer Objects)
Create models to receive data from the user in your controllers.

**1. ForgotPasswordRequest.java**
```java
@Data
public class ForgotPasswordRequest {
    private String email;
}
```

**2. ResetPasswordRequest.java**
```java
@Data
public class ResetPasswordRequest {
    private String email;
    private String otp;
    private String newPassword;
}
```

## Step 5: Create Email Service
Create a service class to handle generating and sending the email.

```java
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender javaMailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Your Password Reset OTP");
        message.setText("Your OTP for password reset is: " + otp + "\nIt is valid for 10 minutes.");
        
        javaMailSender.send(message);
    }
}
```

## Step 6: Create the Main Business Logic (Service)
In your `AuthService` or `UserService`, implement the logic for both requesting an OTP and resetting the password.

### Concept for `requestPasswordReset(String email)`:
1. Check if the `email` exists in your `UserRepository`. If not, throw an exception or return an error.
2. Generate a random 6-digit OTP (e.g., `String.format("%06d", new Random().nextInt(999999))`).
3. Save the OTP to MongoDB via `OtpTokenRepository` along with the user's email and an expiration time (e.g., `LocalDateTime.now().plusMinutes(10)`).
4. Call `EmailService.sendOtpEmail(email, generatedOtp)`.
5. Return a success message snippet to the user.

### Concept for `resetPassword(ResetPasswordRequest request)`:
1. Query `OtpTokenRepository` by the provided `email` and `otp`.
2. Check if the token exists. If not, return "Invalid OTP".
3. Check if the current time is after the `expirationTime`. If it is expired, delete the OTP and return "OTP Expired".
4. If valid, fetch the `User` from the database.
5. Hash the `newPassword` using `PasswordEncoder` (e.g., `passwordEncoder.encode(request.getNewPassword())`).
6. Update the user's password in MongoDB.
7. Delete the used OTP from `OtpTokenRepository` so it can't be used again.

## Step 7: Create the Rest Controller
Finally, expose the endpoints in your authentication controller.

```java
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final AuthService authService; // Or wherever you put the logic

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        // Call service method to check email, generate OTP, save it, and send email
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok("OTP sent to your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        // Call service method to validate OTP and update password
        authService.resetPassword(request);
        return ResponseEntity.ok("Password successfully updated.");
    }
}
```

## Optional Enhancements
- Instead of keeping expired OTPs in the database, implement a MongoDB TTL (Time-To-Live) index on the `OtpToken` collection to auto-delete expired documents.
- Restrict how many times a user can request an OTP within a certain timeframe to prevent spam.
