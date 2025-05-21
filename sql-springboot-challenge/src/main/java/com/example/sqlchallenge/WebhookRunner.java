
package com.example.sqlchallenge;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class WebhookRunner implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void run(String... args) {
        String initUrl = "https://your-api.com/init"; // Replace with actual URL
        ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(initUrl, null, WebhookResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String webhookUrl = response.getBody().getWebhookUrl();
            String jwtToken = response.getBody().getJwtToken();

            String finalSql = "SELECT p.AMOUNT AS SALARY, " +
                              "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                              "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
                              "d.DEPARTMENT_NAME " +
                              "FROM PAYMENTS p " +
                              "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
                              "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                              "WHERE DAY(PAYMENT_TIME) != 1 " +
                              "ORDER BY p.AMOUNT DESC LIMIT 1;";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(jwtToken);

            Map<String, String> payload = new HashMap<>();
            payload.put("query", finalSql);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(webhookUrl, request, String.class);
        }
    }
}
