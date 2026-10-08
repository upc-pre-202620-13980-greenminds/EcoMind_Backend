package pe.greenminds.ecomind.community.interfaces.acl;

/** Community must resolve conflicting memberships before supplying a local ranking scope. */
public class AmbiguousLocalCommunityException extends IllegalStateException {
    public AmbiguousLocalCommunityException(Long userId) {
        super("More than one local community membership exists for user " + userId);
    }
}
