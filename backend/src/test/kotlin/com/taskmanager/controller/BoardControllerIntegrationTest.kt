package com.taskmanager.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.taskmanager.dto.CreateBoardRequest
import com.taskmanager.dto.CreateTaskRequest
import com.taskmanager.dto.RegisterRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BoardControllerIntegrationTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var mapper: ObjectMapper

    private lateinit var authToken: String
    private lateinit var userId: String

    @BeforeEach
    fun setup() {
        val email = "board-${System.nanoTime()}@test.com"
        val res = mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(RegisterRequest(email, "pass1234", "Board User"))
        }.andReturn().response.contentAsString
        val body = mapper.readTree(res)
        authToken = "Bearer ${body.get("token").asText()}"
        userId = body.get("user").get("id").asText()
    }

    @Test
    fun `GET boards returns empty list initially`() {
        mockMvc.get("/api/boards") { header("Authorization", authToken) }
            .andExpect { status { isOk() }; jsonPath("$") { isArray() } }
    }

    @Test
    fun `POST board creates board with three default columns`() {
        mockMvc.post("/api/boards") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CreateBoardRequest("Sprint 1", "Sprint board"))
            header("Authorization", authToken)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.name") { value("Sprint 1") }
            jsonPath("$.columns.length()") { value(3) }
            jsonPath("$.columns[0].name") { value("To Do") }
            jsonPath("$.columns[1].name") { value("In Progress") }
            jsonPath("$.columns[2].name") { value("Done") }
        }
    }

    @Test
    fun `GET board returns board with columns`() {
        val createRes = mockMvc.post("/api/boards") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CreateBoardRequest("My Board"))
            header("Authorization", authToken)
        }.andReturn().response.contentAsString
        val boardId = mapper.readTree(createRes).get("id").asText()

        mockMvc.get("/api/boards/$boardId") { header("Authorization", authToken) }
            .andExpect { status { isOk() }; jsonPath("$.id") { value(boardId) } }
    }

    @Test
    fun `POST boards requires auth`() {
        mockMvc.post("/api/boards") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CreateBoardRequest("Board"))
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `POST task creates task in column`() {
        val boardRes = mockMvc.post("/api/boards") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CreateBoardRequest("Task Board"))
            header("Authorization", authToken)
        }.andReturn().response.contentAsString
        val boardBody = mapper.readTree(boardRes)
        val boardId = boardBody.get("id").asText()
        val columnId = boardBody.get("columns").get(0).get("id").asText()

        mockMvc.post("/api/boards/$boardId/tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CreateTaskRequest("My Task", columnId = java.util.UUID.fromString(columnId)))
            header("Authorization", authToken)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.title") { value("My Task") }
            jsonPath("$.columnId") { value(columnId) }
        }
    }
}
