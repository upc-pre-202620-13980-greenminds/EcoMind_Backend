package pe.greenminds.ecomind.community.interfaces.acl;

public class AmbiguousLocalCommunityException extends IllegalStateException {
    public AmbiguousLocalCommunityException(Long userId) {
        super("More than one local community membership exists for user " + userId);
    }
}
