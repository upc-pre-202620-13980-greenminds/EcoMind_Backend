package pe.greenminds.ecomind.community.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.community.application.commandservices.PostCommandService;
import pe.greenminds.ecomind.community.domain.model.aggregates.Post;
import pe.greenminds.ecomind.community.domain.model.commands.*;
import pe.greenminds.ecomind.community.domain.repositories.*;
import pe.greenminds.ecomind.shared.application.result.*;

@Service
public class PostCommandServiceImpl implements PostCommandService {
    private final PostRepository posts;
    private final CommunityMembershipRepository memberships;

    public PostCommandServiceImpl(PostRepository postRepository, CommunityMembershipRepository communityMembershipRepository) {
        posts = postRepository;
        memberships = communityMembershipRepository;
    }

    @Transactional
    @Override
    public Result<Post, ApplicationError> handle(CreatePostCommand command) {
        if (memberships.findByCommunityIdAndUserId(command.communityId(), command.authorId()).isEmpty())
            return Result.failure(
                    ApplicationError.forbidden("COMMUNITY_MEMBERSHIP_REQUIRED", "Community membership is required"));
        try {
            return Result.success(
                    posts.save(new Post(null, command.communityId(), command.authorId(), command.content(), "USER", command.imageUrl(), null)));
        } catch (IllegalArgumentException exception) {
            return Result.failure(ApplicationError.validationError("Post", exception.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<Void, ApplicationError> handle(DeletePostCommand command) {
        var found = posts.findById(command.postId());
        if (found.isEmpty())
            return Result.failure(ApplicationError.notFound("Post", String.valueOf(command.postId())));
        if (found.get().authorId() == null || !found.get().authorId().equals(command.requestedBy()))
            return Result
                    .failure(ApplicationError.forbidden("POST_DELETE_FORBIDDEN", "Only the post author may delete it"));
        posts.delete(found.get());
        return Result.success(null);
    }
}
