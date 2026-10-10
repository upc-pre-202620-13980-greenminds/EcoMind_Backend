package pe.greenminds.ecomind.community.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.community.domain.model.aggregates.*;
import pe.greenminds.ecomind.community.domain.model.commands.*;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityRole;
import pe.greenminds.ecomind.community.domain.repositories.*;

@ExtendWith(MockitoExtension.class)
class PostCommandServiceImplTests {
  @Mock PostRepository posts;
  @Mock CommunityMembershipRepository memberships;
  PostCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new PostCommandServiceImpl(posts, memberships);
  }

  void member() {
    when(memberships.findByCommunityIdAndUserId(1L, 20L))
        .thenReturn(Optional.of(new CommunityMembership(1L, 1L, 20L, CommunityRole.MEMBER)));
  }

  @Test
  void nonMemberCannotPublish() {
    assertTrue(service.handle(new CreatePostCommand(1L, 20L, "A greener day", null)).isFailure());
    verifyNoInteractions(posts);
  }

  @Test
  void memberPublishesWithItsIdentity() {
    member();
    when(posts.save(any())).thenAnswer(call -> call.getArgument(0));
    var post =
        service
            .handle(new CreatePostCommand(1L, 20L, "A greener day", null))
            .toOptional()
            .orElseThrow();
    assertEquals(20L, post.authorId());
    assertEquals("USER", post.postType());
  }

  @Test
  void blankContentIsRejected() {
    member();
    assertTrue(service.handle(new CreatePostCommand(1L, 20L, " ", null)).isFailure());
    verify(posts, never()).save(any());
  }

  @Test
  void anotherUserCannotDeletePost() {
    when(posts.findById(7L))
        .thenReturn(Optional.of(new Post(7L, 1L, 20L, "Hello", "USER", null, null)));
    assertTrue(service.handle(new DeletePostCommand(7L, 30L)).isFailure());
    verify(posts, never()).delete(any());
  }

  @Test
  void authorCanDeletePost() {
    var post = new Post(7L, 1L, 20L, "Hello", "USER", null, null);
    when(posts.findById(7L)).thenReturn(Optional.of(post));
    assertTrue(service.handle(new DeletePostCommand(7L, 20L)).isSuccess());
    verify(posts).delete(post);
  }

  @Test
  void systemPostCannotBeDeletedAsUser() {
    when(posts.findById(7L))
        .thenReturn(
            Optional.of(new Post(7L, 1L, null, "Event created", "EVENT_CREATED", null, 8L)));
    assertTrue(service.handle(new DeletePostCommand(7L, 20L)).isFailure());
    verify(posts, never()).delete(any());
  }

  @Test
  void missingPostReturnsFailure() {
    assertTrue(service.handle(new DeletePostCommand(7L, 20L)).isFailure());
    verify(posts, never()).delete(any());
  }
}
