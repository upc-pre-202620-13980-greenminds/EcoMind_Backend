package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "prod"})
public class QuestThemeSchemaUpgrade implements ApplicationRunner {
    private final JdbcTemplate jdbc;

    public QuestThemeSchemaUpgrade(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Hibernate creates the check on new tables but does not expand it during ddl-auto=update.
        jdbc.execute("""
                DO $$
                BEGIN
                    IF EXISTS (
                        SELECT 1 FROM pg_constraint
                        WHERE conrelid = 'quests'::regclass
                          AND conname = 'quests_theme_check'
                          AND strpos(pg_get_constraintdef(oid), '''WRITE''') = 0
                    ) THEN
                        ALTER TABLE quests
                            DROP CONSTRAINT quests_theme_check,
                            ADD CONSTRAINT quests_theme_check
                                CHECK (theme IN ('CHECKBOX', 'MINIGAME', 'COLLABORATIVE', 'WRITE'));
                    END IF;
                END;
                $$;
                """);
    }
}
