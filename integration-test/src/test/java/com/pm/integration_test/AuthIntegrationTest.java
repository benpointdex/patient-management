package com.pm.integration_test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.oneOf;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AuthIntegrationTest {

    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    public void shouldReturnOKWithValidToken() {

        String registerPayload = """
          {
            "email": "testuser@test.com",
            "password": "password123",
            "role": "USER"
          }
        """;
        given()
                .contentType("application/json")
                .body(registerPayload)
                .when()
                .post("/api/v1/register") // Use the correct gateway path
                .then()
                .statusCode(oneOf(201, 400));


        String loginPayload = """
          {
            "email": "testuser@test.com",
            "password": "password123"
          }
        """;

        Response response = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/api/v1/login") // Changed from /auth/login
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .response();

        System.out.println("Generated Token: " + response.jsonPath().getString("token"));
    }


    @Test
    public void shouldReturnUnauthorizedOnInvalidLogin() {
        String loginPayload = """
          {
            "email": "invalid_user@test.com",
            "password": "wrongpassword"
          }
        """;

        given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/api/v1/login")
                .then()
                .statusCode(401);
    }
    }


