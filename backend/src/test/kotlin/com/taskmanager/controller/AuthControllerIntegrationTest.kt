package com.taskmanager.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.taskmanager.dto.LoginRequest
import com.taskmanager.dto.RegisterRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var mapper: ObjectMapper

    @Test
    fun `POST register creates user and returns token`() {
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(RegisterRequest("reg-new@test.com", "pass1234", "New User"))
        }.andExpect {
            status { isCreated() }
            jsonPath("$.token") { exists() }
            jsonPath("$.user.email") { value("reg-new@test.com") }
        }
    }

    @Test
    fun `POST register rejects duplicate email`() {
        val req = mapper.writeValueAsString(RegisterRequest("reg-dup@test.com", "pass1234", "Dup"))
        mockMvc.post("/api/auth/register") { contentType = MediaType.APPLICATION_JSON; content = req }
            .andExpect { status { isCreated() } }
        mockMvc.post("/api/auth/register") { contentType = MediaType.APPLICATION_JSON; content = req }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `POST register rejects invalid email`() {
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(mapOf("email" to "not-email", "password" to "pass1234", "displayName" to "X"))
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `POST register rejects short password`() {
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(mapOf("email" to "reg-short@test.com", "password" to "short", "displayName" to "X"))
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `POST login returns token for valid credentials`() {
        val reg = mapper.writeValueAsString(RegisterRequest("reg-login@test.com", "pass1234", "Login"))
        mockMvc.post("/api/auth/register") { contentType = MediaType.APPLICATION_JSON; content = reg }
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(LoginRequest("reg-login@test.com", "pass1234"))
        }.andExpect { status { isOk() }; jsonPath("$.token") { exists() } }
    }

    @Test
    fun `POST login returns 401 for wrong password`() {
        val reg = mapper.writeValueAsString(RegisterRequest("reg-bad@test.com", "pass1234", "Bad"))
        mockMvc.post("/api/auth/register") { contentType = MediaType.APPLICATION_JSON; content = reg }
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(LoginRequest("reg-bad@test.com", "wrongpass"))
        }.andExpect { status { isUnauthorized() } }
    }
}
