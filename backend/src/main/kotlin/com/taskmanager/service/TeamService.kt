package com.taskmanager.service

import com.taskmanager.dto.*
import com.taskmanager.model.*
import com.taskmanager.repository.*
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val memberRepository: TeamMemberRepository,
    private val userRepository: UserRepository
) {
    fun getTeamsForUser(userId: UUID): List<TeamDto> =
        teamRepository.findByMemberUserId(userId).map { it.toDto() }

    fun getTeamById(teamId: UUID, userId: UUID): TeamDto {
        assertMember(teamId, userId)
        return teamRepository.findById(teamId).orElseThrow { EntityNotFoundException("Team not found") }.toDto()
    }

    @Transactional
    fun createTeam(req: CreateTeamRequest, userId: UUID): TeamDto {
        val creator = userRepository.findById(userId).orElseThrow { EntityNotFoundException("User not found") }
        val team = teamRepository.save(Team(name = req.name, description = req.description, createdBy = creator))
        memberRepository.save(TeamMember(TeamMemberId(team.id!!, userId), team, creator, TeamRole.OWNER))
        return teamRepository.findById(team.id!!).get().toDto()
    }

    @Transactional
    fun updateTeam(teamId: UUID, req: UpdateTeamRequest, userId: UUID): TeamDto {
        assertAdmin(teamId, userId)
        val team = teamRepository.findById(teamId).orElseThrow { EntityNotFoundException("Team not found") }
        req.name?.let { team.name = it }
        req.description?.let { team.description = it }
        return teamRepository.save(team).toDto()
    }

    @Transactional
    fun deleteTeam(teamId: UUID, userId: UUID) {
        assertOwner(teamId, userId)
        teamRepository.deleteById(teamId)
    }

    @Transactional
    fun addMember(teamId: UUID, req: AddMemberRequest, userId: UUID): TeamDto {
        assertAdmin(teamId, userId)
        if (memberRepository.existsByTeamIdAndUserId(teamId, req.userId))
            throw IllegalArgumentException("User is already a member")
        val team = teamRepository.findById(teamId).orElseThrow { EntityNotFoundException("Team not found") }
        val newMember = userRepository.findById(req.userId).orElseThrow { EntityNotFoundException("User not found") }
        memberRepository.save(TeamMember(TeamMemberId(teamId, req.userId), team, newMember, req.role))
        return teamRepository.findById(teamId).get().toDto()
    }

    @Transactional
    fun removeMember(teamId: UUID, memberId: UUID, userId: UUID) {
        assertAdmin(teamId, userId)
        val m = memberRepository.findByTeamIdAndUserId(teamId, memberId)
            ?: throw EntityNotFoundException("Member not found")
        memberRepository.delete(m)
    }

    private fun assertMember(teamId: UUID, userId: UUID) {
        if (!memberRepository.existsByTeamIdAndUserId(teamId, userId)) throw SecurityException("Not a team member")
    }

    private fun assertAdmin(teamId: UUID, userId: UUID) {
        val m = memberRepository.findByTeamIdAndUserId(teamId, userId) ?: throw SecurityException("Not a team member")
        if (m.role !in listOf(TeamRole.OWNER, TeamRole.ADMIN)) throw SecurityException("Admin required")
    }

    private fun assertOwner(teamId: UUID, userId: UUID) {
        val m = memberRepository.findByTeamIdAndUserId(teamId, userId) ?: throw SecurityException("Not a team member")
        if (m.role != TeamRole.OWNER) throw SecurityException("Owner required")
    }
}
