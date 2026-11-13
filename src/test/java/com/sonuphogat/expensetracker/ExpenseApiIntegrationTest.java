package com.sonuphogat.expensetracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonuphogat.expensetracker.expense.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired ExpenseRepository repository;

    private final String today = LocalDate.now().toString();
    private final String month = today.substring(0, 7);

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private long createExpense(String title, String amount, String category) throws Exception {
        String body = """
                {"title":"%s","amount":%s,"category":"%s","spentOn":"%s"}
                """.formatted(title, amount, category, today);
        String json = mvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/expenses/")))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(json);
        return node.get("id").asLong();
    }

    @Test
    void fullCrudLifecycle() throws Exception {
        long id = createExpense("Groceries", "54.90", "FOOD");

        mvc.perform(get("/api/expenses/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Groceries"))
                .andExpect(jsonPath("$.amount").value(54.90));

        mvc.perform(put("/api/expenses/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"title":"Groceries (REWE)","amount":60.10,"category":"FOOD","spentOn":"%s","note":"updated"}
                        """.formatted(today)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Groceries (REWE)"))
                .andExpect(jsonPath("$.note").value("updated"));

        mvc.perform(delete("/api/expenses/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/expenses/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not found"));
    }

    @Test
    void invalidPayloadReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title":"","amount":-5,"category":"FOOD","spentOn":"2999-01-01"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.amount").exists())
                .andExpect(jsonPath("$.errors.spentOn").exists());
    }

    @Test
    void unknownCategoryIsBadRequest() throws Exception {
        mvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title":"x","amount":5,"category":"PIZZA","spentOn":"%s"}
                        """.formatted(today)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listCanBeFilteredByCategory() throws Exception {
        createExpense("Rent", "650", "RENT");
        createExpense("Groceries", "40", "FOOD");
        createExpense("Döner", "8.50", "FOOD");

        mvc.perform(get("/api/expenses").param("category", "FOOD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void monthlySummaryGroupsByCategory() throws Exception {
        createExpense("Rent", "600", "RENT");
        createExpense("Groceries", "150", "FOOD");
        createExpense("Ticket", "50", "TRANSPORT");

        mvc.perform(get("/api/expenses/summary").param("month", month))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(800.0))
                .andExpect(jsonPath("$.numberOfExpenses").value(3))
                .andExpect(jsonPath("$.byCategory[0].category").value("RENT"))
                .andExpect(jsonPath("$.byCategory[0].percentage").value(75.0));
    }

    @Test
    void healthEndpointIsUp() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }
}
