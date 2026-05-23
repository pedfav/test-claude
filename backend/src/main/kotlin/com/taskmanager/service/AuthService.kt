package com.taskmanager.service

import com.taskmanager.dto.*
import com.taskmanager.model.User
import com.taskmanager.repository.UserRepository
import com.taskmanager.security.JwtService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authManager: AuthenticationManager
) {
    @Transactional
    fun register(req: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(req.email))
            throw IllegalArgumentException("Email already registered")
        val user = userRepository.save(User(email = req.email, passwordHash = passwordEncoder.encode(req.password), displayName = req.displayName))
        return AuthResponse(jwtService.generateToken(user.id!!, user.email), user.toDto())
    }

    fun login(req: LoginRequest): AuthResponse {
        authManager.authenticate(UsernamePasswordAuthenticationToken(req.email, req.password))
        val user = userRepository.findByEmail(req.email)!!
        return AuthResponse(jwtService.generateToken(user.id!!, user.email), user.toDto())
    }
}
