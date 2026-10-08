package pe.greenminds.ecomind.community.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.community.application.commandservices.PostReactionCommandService;
import pe.greenminds.ecomind.community.domain.model.aggregates.PostReaction;
import pe.greenminds.ecomind.community.domain.model.commands.*;
import pe.greenminds.ecomind.community.domain.repositories.*;
import pe.greenminds.ecomind.shared.application.result.*;

@Service
public class PostReactionCommandServiceImpl implements PostReactionCommandService {
    private final PostRepository posts;
    private final PostReactionRepository reactions;

    public PostReactionCommandServiceImpl(PostRepository p, PostReactionRepository r) {
        posts = p;
        reactions = r;
    }

    @Transactional
    public Result<PostReaction, ApplicationError> handle(ReactToPostCommand c) {
        if (!posts.existsById(c.postId()))
            return Result.failure(ApplicationError.notFound("Post", String.valueOf(c.postId())));
        try {
            return Result.success(reactions.save(new PostReaction(null, c.postId(), c.userId(), c.reactionType())));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Post reaction", e.getMessage()));
        }
    }

    @Transactional
    public Result<PostReaction, ApplicationError> handle(UpdatePostReactionTypeCommand c) {
        var current = reactions.findByPostIdAndUserId(c.postId(), c.userId());
        if (current.isEmpty())
            return Result.failure(ApplicationError.notFound("Post reaction", c.postId() + ":" + c.userId()));
        try {
            return Result.success(
                    reactions.save(new PostReaction(current.get().id(), c.postId(), c.userId(), c.reactionType())));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Post reaction", e.getMessage()));
        }
    }

    @Transactional
    public Result<Void, ApplicationError> handle(RemovePostReactionCommand c) {
        reactions.findByPostIdAndUserId(c.postId(), c.userId()).ifPresent(reactions::delete);
        return Result.success(null);
    }
}
