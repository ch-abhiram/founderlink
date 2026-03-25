package com.team_service.Repository;

import com.team_service.Entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByStartupId(Long startupId);
    List<TeamMember> findByUserEmail(String userEmail);
    Optional<TeamMember> findByStartupIdAndUserEmail(Long startupId, String userEmail);
}
