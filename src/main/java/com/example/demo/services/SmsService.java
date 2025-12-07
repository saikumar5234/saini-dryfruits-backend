package com.example.demo.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

@Service
public class SmsService {
    
    private static final String API_KEY = "tVy31wugz9UeMFx0hXiWckT5vnsbYmNBfEZPJGa6KoD4OCQ82Ix8o5wTckL2KDPqfegNZCidaIrVps7v";
    private static final String SENDER_ID = "SMDRY";

    @Value("${sms.template.id}")
    private String messageId;  // <-- this now holds 203754

    @Value("${sms.test.mode:false}")
    private boolean testMode;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String sendOtp(String mobile, String otp) {
        try {
            if (testMode) {
                return "TEST MODE ENABLED";
            }

            // Build URL (NO TEMPLATE TEXT NEEDED HERE)
            String urlString = "https://www.fast2sms.com/dev/bulkV2"
                    + "?authorization=" + API_KEY
                    + "&route=dlt"
                    + "&sender_id=" + SENDER_ID
                    + "&message=" + messageId   // <-- only message ID
                    + "&variables_values=" + URLEncoder.encode(otp, "UTF-8")
                    + "&numbers=" + mobile;

            System.out.println("URL sent => " + urlString.replace(API_KEY, "HIDDEN"));

            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(15000);
            con.setReadTimeout(15000);
            con.setRequestProperty("Accept", "application/json");

            int responseCode = con.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (responseCode >= 200 && responseCode < 300) 
                        ? con.getInputStream() 
                        : con.getErrorStream()
            ));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            System.out.println("Raw API Response = " + response.toString());

            // JSON parse
            Map<String, Object> jsonResponse = objectMapper.readValue(response.toString(), Map.class);
            Boolean status = (Boolean) jsonResponse.get("return");

            if (status != null && status) {
                System.out.println("✔ SMS delivered successfully");
            } else {
                System.err.println("❌ SMS Delivery Failed => " + jsonResponse.get("message"));
            }

            return response.toString();

        } catch (Exception e) {
            System.err.println("ERROR sending SMS -> " + e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }
}

