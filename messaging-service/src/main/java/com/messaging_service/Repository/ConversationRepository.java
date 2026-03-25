package com.messaging_service.Repository;

import com.messaging_service.Entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByStartupId(Long startupId);
    List<Conversation> findByParticipantEmail(String participantEmail);
    Optional<Conversation> findByStartupIdAndParticipantEmail(Long startupId, String participantEmail);
}
