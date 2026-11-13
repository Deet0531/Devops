package com.example.todo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TodoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void helloReturnsMessage() throws Exception {
        mvc.perform(get("/hello"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void canAddAndListTodo() throws Exception {
        mvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"learn jenkins\"}"))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.title").value("learn jenkins"));

        mvc.perform(get("/todos"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].title").value("learn jenkins"));
    }

    @Test
    void blankTitleIsRejected() throws Exception {
        mvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\"}"))
           .andExpect(status().isBadRequest());
    }
}
