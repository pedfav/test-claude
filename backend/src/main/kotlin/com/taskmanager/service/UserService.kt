package com.taskmanager.service

import com.taskmanager.dto.*
import com.taskmanager.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(private val userRepository: UserRepository) {
    fun getById(id: UUID): UserDto =
        userRepository.findById(id).orElseThrow { EntityNotFoundException("User not found") }.toDto()

    fun getAll(): List<UserDto> = userRepository.findAll().map { it.toDto() }

    @Transactional
    fun updateProfile(id: UUID, req: UpdateProfileRequest): UserDto {
        val user = userRepository.findById(id).orElseThrow { EntityNotFoundException("User not found") }
        user.displayName = req.displayName
        user.avatarUrl = req.avatarUrl
        return userRepository.save(user).toDto()
    }
}
