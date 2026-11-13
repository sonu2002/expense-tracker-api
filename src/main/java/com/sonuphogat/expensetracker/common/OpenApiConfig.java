package com.sonuphogat.expensetracker.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI expenseTrackerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Expense Tracker API")
                .version("1.0.0")
                .description("Track personal expenses, filter them by category and date, and get monthly spending summaries.")
                .contact(new Contact().name("Sonu Phogat").url("https://github.com/sonu2002"))
                .license(new License().name("MIT")));
    }
}
