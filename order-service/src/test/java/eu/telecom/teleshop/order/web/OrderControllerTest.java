package eu.telecom.teleshop.order.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.telecom.teleshop.order.events.OrderCreated;
import eu.telecom.teleshop.order.model.Order;
import eu.telecom.teleshop.order.repository.OrderRepository;
import eu.telecom.teleshop.order.service.OrderEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    OrderRepository orderRepository;

    @MockBean
    OrderEventPublisher publisher;

    @Test
    void create_persists_and_publishes_event() throws Exception {
        Order toCreate = new Order();
        toCreate.setUserId(1L); toCreate.setProductId(1L); toCreate.setQuantity(2);
        Order saved = new Order();
        saved.setId(10L); saved.setUserId(1L); saved.setProductId(1L); saved.setQuantity(2); saved.setStatus("CREATED");
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toCreate)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/orders/10"))
            .andExpect(jsonPath("$.status").value("CREATED"));

        ArgumentCaptor<OrderCreated> captor = ArgumentCaptor.forClass(OrderCreated.class);
        verify(publisher).publishOrderCreated(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo(10L);
    }

    @Test
    void getById_returns_order() throws Exception {
        Order o = new Order(); o.setId(5L); o.setStatus("CREATED");
        when(orderRepository.findById(5L)).thenReturn(Optional.of(o));

        mockMvc.perform(get("/orders/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(5));
    }
}
