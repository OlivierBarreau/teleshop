package eu.telecom.teleshop.notification.web;

import eu.telecom.teleshop.notification.model.Notification;
import eu.telecom.teleshop.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    NotificationRepository repository;

    @Test
    void list_returns_notifications() throws Exception {
        when(repository.findAll()).thenReturn(List.of(
                new Notification("ORDER_CREATED", "msg1", 1L),
                new Notification("STOCK_UPDATED", "msg2", null)
        ));

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("ORDER_CREATED"));
    }

    @Test
    void listByUserId_returns_notifications_for_user() throws Exception {
        when(repository.findByUserId(1L)).thenReturn(List.of(
                new Notification("ORDER_CREATED", "msg1", 1L),
                new Notification("STOCK_UPDATED", "msg2", 1L)
        ));

        mockMvc.perform(get("/notifications/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1L));
    }
}
