package pe.greenminds.ecomind.community.infrastructure.acl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.community.application.outboundservices.CommunityActorGateway;

@Component
public class CommunityActorGatewayImpl implements CommunityActorGateway {
    private final JdbcTemplate jdbc;
    public CommunityActorGatewayImpl(JdbcTemplate jdbc){
        this.jdbc=jdbc;
    }
    @Override
    public boolean isParent(Long userId){
        return jdbc.query("select social_role from user_profiles where user_id = ?",
                resultSet -> resultSet.next() && "PARENT".equals(resultSet.getString(1)), userId);
    }
    @Override
    public void requireParent(Long userId){
        if(!isParent(userId))
            throw new SecurityException("Only parent users may create communities or events");
    }

    @Override
    public int requireFamilyParentAndCount(Long userId, Long familyId) {
        Integer memberCount = jdbc.query("select count(*) from family_members where family_id = ?", resultSet -> {
            resultSet.next();
            return resultSet.getInt(1);
        }, familyId);
        Boolean isParent = jdbc.query(
                "select exists(select 1 from family_members where family_id = ? and user_id = ? and family_role = 'PARENT')",
                resultSet -> {
                    resultSet.next();
                    return resultSet.getBoolean(1);
                }, familyId, userId);
        if (!isParent || memberCount == 0)
            throw new SecurityException("Only a parent member of the selected family may register it");
        return memberCount;
    }
}
