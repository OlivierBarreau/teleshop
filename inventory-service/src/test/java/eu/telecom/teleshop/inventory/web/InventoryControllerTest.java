package eu.telecom.teleshop.inventory.web;

import eu.telecom.teleshop.inventory.model.Product;
import eu.telecom.teleshop.inventory.repository.ProductRepository;
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

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ProductRepository productRepository;

    @Test
    void products_returns_list() throws Exception {
        when(productRepository.findAll()).thenReturn(List.of(
                new Product("Phone", 10), new Product("Laptop", 5)
        ));

        mockMvc.perform(get("/inventory/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Phone"));
    }
}
