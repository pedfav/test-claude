package com.taskmanager.service

import com.taskmanager.dto.AddMemberRequest
import com.taskmanager.dto.CreateTeamRequest
import com.taskmanager.dto.UpdateTeamRequest
import com.taskmanager.model.*
import com.taskmanager.repository.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional
import java.util.UUID

class TeamServiceTest {

    private val teamRepository: TeamRepository = mockk()
    private val memberRepository: TeamMemberRepository = mockk()
    private val userRepository: UserRepository = mockk()

    private val service = TeamService(teamRepository, memberRepository, userRepository)

    private val ownerId = UUID.randomUUID()
    private val owner = User(email = "o@o.com", passwordHash = "h", displayName = "Owner").also { it.id = ownerId }
    private val teamId = UUID.randomUUID()
    private val team = Team(name = "Team A", createdBy = owner).also { it.id = teamId }
    private val ownerMember = TeamMember(TeamMemberId(teamId, ownerId), team, owner, TeamRole.OWNER)

    @Test
    fun `getTeamsForUser returns teams`() {
        every { teamRepository.findByMemberUserId(ownerId) } returns listOf(team)
        val result = service.getTeamsForUser(ownerId)
        assertEquals(1, result.size)
        assertEquals("Team A", result[0].name)
    }

    @Test
    fun `getTeamById throws for non-member`() {
        every { memberRepository.existsByTeamIdAndUserId(teamId, ownerId) } returns false
        assertThrows<SecurityException> { service.getTeamById(teamId, ownerId) }
    }

    @Test
    fun `getTeamById returns team for member`() {
        every { memberRepository.existsByTeamIdAndUserId(teamId, ownerId) } returns true
        every { teamRepository.findById(teamId) } returns Optional.of(team)
        val result = service.getTeamById(teamId, ownerId)
        assertEquals("Team A", result.name)
    }

    @Test
    fun `createTeam saves team and adds owner as member`() {
        val savedTeam = Team(name = "Team A", createdBy = owner).also { it.id = teamId }
        every { userRepository.findById(ownerId) } returns Optional.of(owner)
        every { teamRepository.save(any()) } returns savedTeam
        every { memberRepository.save(any()) } returns ownerMember
        every { teamRepository.findById(teamId) } returns Optional.of(savedTeam)

        val result = service.createTeam(CreateTeamRequest("Team A"), ownerId)

        assertEquals("Team A", result.name)
        verify { memberRepository.save(any()) }
    }

    @Test
    fun `createTeam throws when user not found`() {
        every { userRepository.findById(any()) } returns Optional.empty()
        assertThrows<EntityNotFoundException> { service.createTeam(CreateTeamRequest("T"), ownerId) }
    }

    @Test
    fun `updateTeam modifies name`() {
        every { memberRepository.findByTeamIdAndUserId(teamId, ownerId) } returns ownerMember
        every { teamRepository.findById(teamId) } returns Optional.of(team)
        every { teamRepository.save(any()) } answers { firstArg() }

        val result = service.updateTeam(teamId, UpdateTeamRequest(name = "Renamed"), ownerId)
        assertEquals("Renamed", result.name)
    }

    @Test
    fun `updateTeam throws for non-admin`() {
        val memberId = UUID.randomUUID()
        val member = User(email = "m@m.com", passwordHash = "h", displayName = "M").also { it.id = memberId }
        val plainMember = TeamMember(TeamMemberId(teamId, memberId), team, member, TeamRole.MEMBER)
        every { memberRepository.findByTeamIdAndUserId(teamId, memberId) } returns plainMember
        assertThrows<SecurityException> { service.updateTeam(teamId, UpdateTeamRequest(name = "X"), memberId) }
    }

    @Test
    fun `deleteTeam throws for non-owner`() {
        val adminId = UUID.randomUUID()
        val admin = User(email = "a@a.com", passwordHash = "h", displayName = "A").also { it.id = adminId }
        val adminMember = TeamMember(TeamMemberId(teamId, adminId), team, admin, TeamRole.ADMIN)
        every { memberRepository.findByTeamIdAndUserId(teamId, adminId) } returns adminMember
        assertThrows<SecurityException> { service.deleteTeam(teamId, adminId) }
    }

    @Test
    fun `deleteTeam succeeds for owner`() {
        every { memberRepository.findByTeamIdAndUserId(teamId, ownerId) } returns ownerMember
        every { teamRepository.deleteById(teamId) } returns Unit

        service.deleteTeam(teamId, ownerId)
        verify { teamRepository.deleteById(teamId) }
    }

    @Test
    fun `addMember throws when user already member`() {
        val newUserId = UUID.randomUUID()
        every { memberRepository.findByTeamIdAndUserId(teamId, ownerId) } returns ownerMember
        every { memberRepository.existsByTeamIdAndUserId(teamId, newUserId) } returns true
        assertThrows<IllegalArgumentException> { service.addMember(teamId, AddMemberRequest(newUserId), ownerId) }
    }

    @Test
    fun `addMember adds new member`() {
        val newUserId = UUID.randomUUID()
        val newUser = User(email = "n@n.com", passwordHash = "h", displayName = "New").also { it.id = newUserId }
        val newMember = TeamMember(TeamMemberId(teamId, newUserId), team, newUser, TeamRole.MEMBER)
        every { memberRepository.findByTeamIdAndUserId(teamId, ownerId) } returns ownerMember
        every { memberRepository.existsByTeamIdAndUserId(teamId, newUserId) } returns false
        every { teamRepository.findById(teamId) } returns Optional.of(team)
        every { userRepository.findById(newUserId) } returns Optional.of(newUser)
        every { memberRepository.save(any()) } returns newMember
        every { teamRepository.findById(teamId) } returns Optional.of(team)

        service.addMember(teamId, AddMemberRequest(newUserId), ownerId)
        verify { memberRepository.save(any()) }
    }

    @Test
    fun `removeMember deletes membership`() {
        val memberId = UUID.randomUUID()
        val member = User(email = "m@m.com", passwordHash = "h", displayName = "M").also { it.id = memberId }
        val teamMember = TeamMember(TeamMemberId(teamId, memberId), team, member, TeamRole.MEMBER)
        every { memberRepository.findByTeamIdAndUserId(teamId, ownerId) } returns ownerMember
        every { memberRepository.findByTeamIdAndUserId(teamId, memberId) } returns teamMember
        every { memberRepository.delete(teamMember) } returns Unit

        service.removeMember(teamId, memberId, ownerId)
        verify { memberRepository.delete(teamMember) }
    }

    @Test
    fun `removeMember throws when member not found`() {
        val memberId = UUID.randomUUID()
        every { memberRepository.findByTeamIdAndUserId(teamId, ownerId) } returns ownerMember
        every { memberRepository.findByTeamIdAndUserId(teamId, memberId) } returns null
        assertThrows<EntityNotFoundException> { service.removeMember(teamId, memberId, ownerId) }
    }
}
