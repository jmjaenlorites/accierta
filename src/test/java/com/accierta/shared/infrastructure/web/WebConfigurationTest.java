package com.accierta.shared.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = WebConfigurationTest.TestController.class)
@Import(WebConfiguration.class)
@TestPropertySource(properties = "accierta.web.allowed-origins[0]=https://accierta.com")
class WebConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_allow_configured_origin_for_api_requests() throws Exception {
        mockMvc.perform(options("/api/ping")
                        .header(HttpHeaders.ORIGIN, "https://accierta.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://accierta.com"));
    }

    @RestController
    static class TestController {

        @GetMapping("/api/ping")
        String ping() {
            return "ok";
        }
    }
}
