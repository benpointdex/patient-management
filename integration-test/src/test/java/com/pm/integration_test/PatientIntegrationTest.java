package com.pm.integration_test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.oneOf;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PatientIntegrationTest {
    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    public void shouldReturnPatientsWithValidToken () {
        // 1. Register the user first to ensure they exist
//        String registerPayload = """
//          {
//            "email": "testuser@test.com",
//            "password": "password123",
//            "role": "USER"
//          }
//        """;
//        given()
//                .contentType("application/json")
//                .body(registerPayload)
//                .when()
//                .post("/api/v1/register")
//                .then()
//                .statusCode(oneOf(201, 400));

        // 2. Login to get the token
        String loginPayload = """
          {
            "email": "testuser@test.com",
            "password": "password123"
          }
        """;

        String token = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/api/v1/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .get("token");

        // 3. Use the token to get patients
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/patients")
                .then()
                .statusCode(200);
    }
}