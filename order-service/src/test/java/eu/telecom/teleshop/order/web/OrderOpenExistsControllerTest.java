package eu.telecom.teleshop.order.web;

import eu.telecom.teleshop.order.repository.OrderRepository;
import eu.telecom.teleshop.order.service.OrderEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderOpenExistsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private OrderEventPublisher orderEventPublisher;

    @Test
    void checkOpenOrdersExist_returnsTrue_whenOpenOrderExists() throws Exception {
        // Given
        Long userId = 100L;
        when(orderRepository.existsByUserIdAndStatusIn(userId, List.of("CREATED"))).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/orders/users/{userId}/open-exists", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    void checkOpenOrdersExist_returnsFalse_whenNoOpenOrderExists() throws Exception {
        // Given
        Long userId = 200L;
        when(orderRepository.existsByUserIdAndStatusIn(userId, List.of("CREATED"))).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/orders/users/{userId}/open-exists", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.exists").value(false));
    }

    @Test
    void checkOpenOrdersExist_returnsFalse_whenUserHasNoOrders() throws Exception {
        // Given
        Long userId = 999L;
        when(orderRepository.existsByUserIdAndStatusIn(userId, List.of("CREATED"))).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/orders/users/{userId}/open-exists", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.exists").value(false));
    }
}
