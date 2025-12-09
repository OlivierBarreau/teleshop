package eu.telecom.teleshop.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(@Value("${order.service.url:http://order-service:8080}") String orderServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(orderServiceUrl)
                .build();
    }

    public boolean hasOpenOrders(Long userId) {
        OpenOrdersExistsResponse response = restClient.get()
                .uri("/orders/users/{userId}/open-exists", userId)
                .retrieve()
                .body(OpenOrdersExistsResponse.class);
        
        return response != null && response.isExists();
    }

    public static class OpenOrdersExistsResponse {
        private boolean exists;

        public OpenOrdersExistsResponse() {}

        public boolean isExists() {
            return exists;
        }

        public void setExists(boolean exists) {
            this.exists = exists;
        }
    }
}
