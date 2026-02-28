package com.example.demo.services;

import com.example.demo.model.Product;
import com.example.demo.model.PushToken;
import com.example.demo.util.PushNotificationHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PushNotificationService {

    private static final int EXPO_MAX_MESSAGES_PER_REQUEST = 100;

    @Autowired
    private PushTokenService pushTokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public int sendAnnouncementToAll(String title, String body, Map<String, Object> data) throws IOException {
        JSONObject jsonData = data == null ? null : new JSONObject(data);
        return sendToTokens(pushTokenService.getAllActivePushTokens(), title, body, jsonData);
    }

    public int notifyNewProductToAll(Product product) throws IOException {
        String productName = extractEnglishName(product.getNameJson(), "New product");

        JSONObject data = new JSONObject();
        data.put("type", "new_product");
        data.put("productId", product.getId());

        String title = "New Product Launched!";
        String body = productName + " is now available";

        return sendToTokens(pushTokenService.getAllActivePushTokens(), title, body, data);
    }

    public int notifyProductEnabledToAll(Product product) throws IOException {
        String productName = extractEnglishName(product.getNameJson(), "Product");

        JSONObject data = new JSONObject();
        data.put("type", "product_enabled");
        data.put("productId", product.getId());

        String title = "Now Available";
        String body = productName + " is available now";

        return sendToTokens(pushTokenService.getAllActivePushTokens(), title, body, data);
    }

    public int notifyPriceChangedToAll(Product product, double oldPrice, double newPrice) throws IOException {
        if (Double.compare(oldPrice, newPrice) == 0) return 0;

        String productName = extractEnglishName(product.getNameJson(), "Product");
        double changePercent = oldPrice == 0 ? 0.0 : ((newPrice - oldPrice) / oldPrice) * 100.0;

        JSONObject data = new JSONObject();
        data.put("type", "price_change");
        data.put("productId", product.getId());
        data.put("oldPrice", oldPrice);
        data.put("newPrice", newPrice);
        data.put("change", newPrice - oldPrice);

        String changeDirection = changePercent > 0 ? "increased" : "decreased";
        String changeSign = changePercent > 0 ? "+" : "";

        String title = "Price " + changeDirection;
        String body = String.format("%s: ₹%.2f → ₹%.2f (%s%.2f%%)",
                productName, oldPrice, newPrice, changeSign, changePercent);

        return sendToTokens(pushTokenService.getAllActivePushTokens(), title, body, data);
    }

    private int sendToTokens(List<PushToken> tokens, String title, String body, JSONObject data) throws IOException {
        if (tokens == null || tokens.isEmpty()) return 0;

        List<String> pushTokens = tokens.stream()
                .filter(t -> Boolean.TRUE.equals(t.getActive()))
                .map(PushToken::getPushToken)
                .filter(this::looksLikeExpoToken)
                .distinct()
                .collect(Collectors.toList());

        if (pushTokens.isEmpty()) return 0;

        int sentCount = 0;
        for (int i = 0; i < pushTokens.size(); i += EXPO_MAX_MESSAGES_PER_REQUEST) {
            List<String> chunk = pushTokens.subList(i, Math.min(i + EXPO_MAX_MESSAGES_PER_REQUEST, pushTokens.size()));
            String response = PushNotificationHelper.sendPushNotificationToMultiple(chunk, title, body, data);
            handleExpoResponse(chunk, response);
            sentCount += chunk.size();
        }

        return sentCount;
    }

    /**
     * Deactivate tokens when Expo says they are not registered anymore.
     * Expo response data array lines up with the request order.
     */
    private void handleExpoResponse(List<String> requestTokens, String expoResponseBody) {
        try {
            JSONObject response = new JSONObject(expoResponseBody);
            JSONArray data = response.optJSONArray("data");
            if (data == null) return;

            int n = Math.min(requestTokens.size(), data.length());
            for (int i = 0; i < n; i++) {
                JSONObject item = data.optJSONObject(i);
                if (item == null) continue;

                String status = item.optString("status", "");
                if (!"error".equalsIgnoreCase(status)) continue;

                JSONObject details = item.optJSONObject("details");
                String error = details != null ? details.optString("error", "") : item.optString("message", "");
                if ("DeviceNotRegistered".equalsIgnoreCase(error)) {
                    pushTokenService.deactivateByPushToken(requestTokens.get(i));
                }
            }
        } catch (Exception ignored) {
            // If parsing fails, don't break the business flow
        }
    }

    private boolean looksLikeExpoToken(String token) {
        if (token == null) return false;
        String t = token.trim();
        return t.startsWith("ExponentPushToken[") || t.startsWith("ExpoPushToken[");
    }

    private String extractEnglishName(String nameJson, String fallback) {
        if (nameJson == null || nameJson.trim().isEmpty()) return fallback;
        try {
            Map<String, String> map = objectMapper.readValue(nameJson, new TypeReference<Map<String, String>>() {});
            String en = map.get("en");
            if (en != null && !en.trim().isEmpty()) return en.trim();
            for (String v : map.values()) {
                if (v != null && !v.trim().isEmpty()) return v.trim();
            }
            return fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}

