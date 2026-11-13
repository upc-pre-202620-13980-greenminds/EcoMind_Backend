package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.RewardTransactionPersistenceEntity;

@Component
@Profile({"dev", "prod"})
@DependsOn("entityManagerFactory")
public class RewardSourceSchemaUpgrade implements InitializingBean {
    private final JdbcTemplate jdbc;

    public RewardSourceSchemaUpgrade(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void afterPropertiesSet() {
        jdbc.execute("""
                DO $$
                BEGIN
                    IF EXISTS (
                        SELECT 1 FROM pg_constraint
                        WHERE conrelid = 'reward_transactions'::regclass
                          AND conname = 'ck_rewardtransaction'
                          AND strpos(pg_get_constraintdef(oid), '''STREAK_BONUS''') = 0
                    ) THEN
                        ALTER TABLE reward_transactions
                            DROP CONSTRAINT ck_rewardtransaction,
                            ADD CONSTRAINT ck_rewardtransaction CHECK (%s);
                    END IF;
                END;
                $$;
                """.formatted(RewardTransactionPersistenceEntity.CHECK_CONSTRAINT));
    }
}
