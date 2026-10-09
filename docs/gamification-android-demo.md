# Android integration fixture

`src/test/java/pe/greenminds/ecomind/demo/GamificationDemoServer.java` starts an explicit test-only backend on port 8095. It forces the test profile, a fresh in-memory H2 database and the real Gamification outbox. Command-line configuration overrides are rejected. Stopping the process discards all test data. The launcher is outside production source and is not packaged in the application.

With Java 21 selected, from the backend repository:

```sh
./mvnw -q test-compile dependency:build-classpath -Dmdep.includeScope=test -Dmdep.outputFile=target/demo-classpath.txt
java -cp "target/test-classes:target/classes:$(cat target/demo-classpath.txt)" pe.greenminds.ecomind.demo.GamificationDemoServer
```

Wait for `QA_READY` before running Android. The fictional account and password are defined in the test launcher. The fixture creates a profile, family, local community, a personal achievement definition and one daily checkbox activity. It does **not** seed completed activities, rewards, streaks, awards or publications.

Android's `RemoteGamificationFlowTest` signs in through the real UI, completes the activity, reads the granted reward and streak, opens the earned medal and explicitly requests publication in the community. The outbox and Community handler must confirm the publication before the test accepts `PUBLISHED`.

The fixture is a local integration environment. It does not verify a deployed API, physical device, streak rollover, every activity handler or the full application. The backend's regular test suite separately covers domain rules, reward idempotency, membership, streaks and outbox delivery.
