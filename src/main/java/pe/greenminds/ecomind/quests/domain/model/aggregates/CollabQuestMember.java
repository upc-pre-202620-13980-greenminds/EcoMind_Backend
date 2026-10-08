package pe.greenminds.ecomind.quests.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.MemberRole;

import java.time.LocalDateTime;
import java.util.Objects;

public class CollabQuestMember {

    @Getter
    @Setter
    private Long id;

    private Long sessionId;
    private Long userId;
    private Long ownerId;
    private MemberRole role;
    private CollabMemberStatus status;
    private LocalDateTime answerDate;
    private LocalDateTime revokeDate;

    public CollabQuestMember(Long id, Long sessionId, Long userId, Long ownerId, MemberRole role, CollabMemberStatus status, LocalDateTime answerDate, LocalDateTime revokeDate) {
        this.id = id;
        this.sessionId = Objects.requireNonNull(sessionId);
        this.userId = Objects.requireNonNull(userId);
        this.ownerId = Objects.requireNonNull(ownerId);
        this.role = Objects.requireNonNull(role);
        this.status = Objects.requireNonNull(status);
        this.answerDate = answerDate;
        this.revokeDate = revokeDate;
    }

    public CollabQuestMember(Long id, Long sessionId, Long userId, Long ownerId, MemberRole role) {
        this.id = id;
        this.sessionId = Objects.requireNonNull(sessionId);
        this.userId = Objects.requireNonNull(userId);
        this.ownerId = Objects.requireNonNull(ownerId);
        this.role = Objects.requireNonNull(role);
        this.status = CollabMemberStatus.PENDING;
        this.answerDate = null;
        this.revokeDate = null;
    }

    public CollabQuestMember(Long sessionId, Long userId, Long ownerId, MemberRole role, CollabMemberStatus status, LocalDateTime answerDate, LocalDateTime revokeDate) {
        this(null, sessionId, userId, ownerId, role, status, answerDate, revokeDate);
    }

    public CollabQuestMember(Long sessionId, Long userId, Long ownerId, MemberRole role, CollabMemberStatus status) {
        this(null, sessionId, userId, ownerId, role, status, null, null);
    }

    public void answerInvite(CollabMemberStatus status){
        this.status = status;
        this.answerDate = LocalDateTime.now();
    }

    public void declineInvite(){
        this.status = CollabMemberStatus.REJECTED;
        this.answerDate = LocalDateTime.now();
        this.revokeDate = LocalDateTime.now();
    }

    public void revokeInvite() {
        this.status = CollabMemberStatus.REJECTED;
        this.answerDate = LocalDateTime.now();
        this.revokeDate = LocalDateTime.now();
    }

    public void leaveQuest(){
        this.status = CollabMemberStatus.LEFT;
        this.answerDate = LocalDateTime.now();
        this.revokeDate = LocalDateTime.now();
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public MemberRole getRole() {
        return role;
    }

    public CollabMemberStatus getStatus() {
        return status;
    }

    public LocalDateTime getAnswerDate() {
        return answerDate;
    }

    public LocalDateTime getRevokeDate() {
        return revokeDate;
    }
}
