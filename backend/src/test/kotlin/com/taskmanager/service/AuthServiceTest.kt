package com.taskmanager.service

import com.taskmanager.dto.LoginRequest
import com.taskmanager.dto.RegisterRequest
import com.taskmanager.model.User
import com.taskmanager.model.UserRole
import com.taskmanager.repository.UserRepository
import com.taskmanager.security.JwtService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

class AuthServiceTest {

    private val userRepository: UserRepository = mockk()
    private val encoder: PasswordEncoder = mockk()
    private val jwtService: JwtService = mockk()
    private val authManager: AuthenticationManager = mockk()

    private val service = AuthService(userRepository, encoder, jwtService, authManager)

    private val user = User(email = "a@b.com", passwordHash = "hashed", displayName = "Alice", role = UserRole.MEMBER)
        .also { it.id = UUID.randomUUID() }

    @Test
    fun `register succeeds for new email`() {
        every { userRepository.existsByEmail("a@b.com") } returns false
        every { encoder.encode("pass1234") } returns "hashed"
        every { userRepository.save(any()) } returns user
        every { jwtService.generateToken(any(), any()) } returns "tok"

        val result = service.register(RegisterRequest("a@b.com", "pass1234", "Alice"))

        assertEquals("tok", result.token)
        assertEquals("a@b.com", result.user.email)
        verify { userRepository.save(any()) }
    }

    @Test
    fun `register throws for duplicate email`() {
        every { userRepository.existsByEmail("a@b.com") } returns true

        assertThrows<IllegalArgumentException> {
            service.register(RegisterRequest("a@b.com", "pass1234", "Alice"))
        }
    }

    @Test
    fun `login returns token for valid credentials`() {
        every { authManager.authenticate(any()) } returns mockk()
        every { userRepository.findByEmail("a@b.com") } returns user
        every { jwtService.generateToken(any(), any()) } returns "tok"

        val result = service.login(LoginRequest("a@b.com", "pass1234"))

        assertEquals("tok", result.token)
    }

    @Test
    fun `login throws for bad credentials`() {
        every { authManager.authenticate(any()) } throws BadCredentialsException("bad")

        assertThrows<BadCredentialsException> {
            service.login(LoginRequest("a@b.com", "wrong"))
        }
    }
}
