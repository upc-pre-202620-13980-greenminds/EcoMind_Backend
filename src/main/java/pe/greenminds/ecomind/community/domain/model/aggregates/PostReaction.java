package pe.greenminds.ecomind.community.domain.model.aggregates;
import java.util.Locale;
import java.util.Set;
public record PostReaction(Long id,Long postId,Long userId,String reactionType){private static final Set<String>TYPES=Set.of("like","funny","love","surprise","sad","angry");public PostReaction{if(postId==null||userId==null||reactionType==null)throw new IllegalArgumentException("Post, user and supported reaction are required");reactionType=reactionType.toLowerCase(Locale.ROOT);if(!TYPES.contains(reactionType))throw new IllegalArgumentException("Reaction type must be like, funny, love, surprise, sad or angry");}}
