package eu.telecom.teleshop.user.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.telecom.teleshop.user.events.UserDeleted;
import eu.telecom.teleshop.user.model.User;
import eu.telecom.teleshop.user.repository.UserRepository;
import eu.telecom.teleshop.user.service.OrderClient;
import eu.telecom.teleshop.user.service.UserEventPublisher;
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

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserRepository userRepository;

    @MockBean
    UserEventPublisher publisher;

    @MockBean
    OrderClient orderClient;

    @Test
    void register_creates_user() throws Exception {
        User toSave = new User();
        toSave.setName("Alice"); toSave.setEmail("alice@example.com"); toSave.setPassword("secret");
        User saved = new User();
        saved.setId(1L); saved.setName("Alice"); saved.setEmail("alice@example.com"); saved.setPassword("secret");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toSave)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/users/1"))
            .andExpect(jsonPath("$.id").value(1));

        verify(userRepository).save(any(User.class));
        verifyNoInteractions(publisher);
    }

    @Test
    void getById_returns_user() throws Exception {
        User saved = new User();
        saved.setId(2L); saved.setName("Bob"); saved.setEmail("bob@example.com"); saved.setPassword("x");
        when(userRepository.findById(2L)).thenReturn(Optional.of(saved));

        mockMvc.perform(get("/users/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("bob@example.com"));
    }

    @Test
    void delete_publishes_userDeleted() throws Exception {
        User saved = new User();
        saved.setId(3L); saved.setName("Carol"); saved.setEmail("carol@example.com");
        when(userRepository.findById(3L)).thenReturn(Optional.of(saved));
        when(orderClient.hasOpenOrders(3L)).thenReturn(false);

        mockMvc.perform(delete("/users/3"))
            .andExpect(status().isNoContent());

        ArgumentCaptor<UserDeleted> captor = ArgumentCaptor.forClass(UserDeleted.class);
        verify(publisher).publishUserDeleted(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(3L);
        verify(userRepository).deleteById(3L);
    }

    @Test
    void delete_returns409_whenUserHasOpenOrders() throws Exception {
        User saved = new User();
        saved.setId(4L); saved.setName("Dave"); saved.setEmail("dave@example.com");
        when(userRepository.findById(4L)).thenReturn(Optional.of(saved));
        when(orderClient.hasOpenOrders(4L)).thenReturn(true);

        mockMvc.perform(delete("/users/4"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("User has open orders"));

        verify(userRepository, never()).deleteById(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void delete_proceedsWithDeletion_whenUserHasNoOpenOrders() throws Exception {
        User saved = new User();
        saved.setId(5L); saved.setName("Eve"); saved.setEmail("eve@example.com");
        when(userRepository.findById(5L)).thenReturn(Optional.of(saved));
        when(orderClient.hasOpenOrders(5L)).thenReturn(false);

        mockMvc.perform(delete("/users/5"))
            .andExpect(status().isNoContent());

        verify(orderClient).hasOpenOrders(5L);
        verify(userRepository).deleteById(5L);
        ArgumentCaptor<UserDeleted> captor = ArgumentCaptor.forClass(UserDeleted.class);
        verify(publisher).publishUserDeleted(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(5L);
    }
}
