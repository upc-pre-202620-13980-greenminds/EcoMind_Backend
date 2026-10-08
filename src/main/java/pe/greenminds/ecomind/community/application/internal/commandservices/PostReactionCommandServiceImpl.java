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

    public PostReactionCommandServiceImpl(PostRepository postRepository, PostReactionRepository postReactionRepository) {
        posts = postRepository;
        reactions = postReactionRepository;
    }

    @Transactional
    @Override
    public Result<PostReaction, ApplicationError> handle(ReactToPostCommand command) {
        if (!posts.existsById(command.postId()))
            return Result.failure(ApplicationError.notFound("Post", String.valueOf(command.postId())));
        try {
            return Result.success(reactions.save(new PostReaction(null, command.postId(), command.userId(), command.reactionType())));
        } catch (IllegalArgumentException exception) {
            return Result.failure(ApplicationError.validationError("Post reaction", exception.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<PostReaction, ApplicationError> handle(UpdatePostReactionTypeCommand command) {
        var current = reactions.findByPostIdAndUserId(command.postId(), command.userId());
        if (current.isEmpty())
            return Result.failure(ApplicationError.notFound("Post reaction", command.postId() + ":" + command.userId()));
        try {
            return Result.success(
                    reactions.save(new PostReaction(current.get().id(), command.postId(), command.userId(), command.reactionType())));
        } catch (IllegalArgumentException exception) {
            return Result.failure(ApplicationError.validationError("Post reaction", exception.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<Void, ApplicationError> handle(RemovePostReactionCommand command) {
        reactions.findByPostIdAndUserId(command.postId(), command.userId()).ifPresent(reactions::delete);
        return Result.success(null);
    }
}
