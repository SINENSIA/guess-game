# GuessGame 0.0.3

GuessGame is an educational number guessing exercise (range 1-10) focused on clean Java practices, dependency inversion, and safe concurrency. The project now ships both console demos and a Spring Boot + Thymeleaf Web UI.

## Requirements

- Java 25 (`java --version` should print `openjdk 25`).
- Maven 3.9 or newer.
- Shell capable of running PowerShell or Bash commands.

## Quick start

1. Clone or copy the repository.
2. Open a terminal inside `guessgame/`.
3. Run the automated tests:

   ```bash
   mvn clean test
   ```

   The suite covers a winning scenario and validation of invalid inputs via a mocked `GameIO`.

## Automated tests

- Tests live under `src/test/java/com/sinensia/games/GuessGameTest.java`.
- Run all tests with `mvn test` (reports are placed in `target/surefire-reports/`).
- Execute a single case with `mvn -Dtest=GuessGameTest#testJuegoAciertaEnElSegundoIntento test`.
- From PowerShell remember to prefix scripts with `./` if you enable the Maven wrapper.
- Manual testers can still run `java -cp target/classes com.sinensia.games.AppGame` after `mvn compile`.

## Playing the game

You can now enjoy the game through the new Web UI or keep using the classic console variants.

### Web interface

1. Start the Spring Boot app from `guessgame/`:

   ```bash
   mvn spring-boot:run
   ```

2. Open `http://localhost:8080` and register with an alias. Each browser tab/session becomes an independent player sharing the same match.
3. When the round finishes, any player may press **Iniciar nueva partida** to reset the board while keeping the registered aliases.

### Console classics

1. Compile (builds classes under `target/classes`):

   ```bash
   mvn clean compile
   ```

2. Launch the desired `main` entry point:

   - **AppGame + Consola (strategy-friendly IO):**

     ```bash
     java -cp target/classes com.sinensia.games.AppGame
     ```

   - **TwrGame (procedural single file version):**

     ```bash
     java -cp target/classes com.sinensia.games.TwrGame
     ```

   Use single quotes in PowerShell when you need to pass extra arguments.

> **Note:** Seeing warnings about `java.lang.System::load` or `sun.misc.Unsafe` during Maven execution is expected on recent JDKs. They come from transitive tooling and do not indicate project issues.

## Design patterns

- **Strategy:** `GameIO` abstracts I/O; `Consola` and test doubles are concrete strategies.
- **Monitor Object / thread-safe core:** `GuessGame` protects mutable state with intrinsic locking and `AtomicBoolean`.
- **Template Method (informal):** `AppGame` and `TwrGame` define step-by-step flows while delegating behaviour.
- **Concurrent orchestration:** `ConcurrentGame` is now a Spring singleton service that coordinates multiple Web players safely.
- **Shared utilities:** `GameRandom` and `GameLogger` centralise randomness and logging, avoiding brittle singletons.
- **Test Double:** `GuessGameTest` introduces a handcrafted mock of `GameIO` to isolate game logic.

## Logging and configuration good practices

- `GameLogger` wraps `java.util.logging` to control formatting and sensitive data exposure.
- Exceptions get logged for developers while end users receive friendly messages.
- `GameRandom` exemplifies reusable helpers without resorting to global mutable state, keeping things testable.

## Generating Javadoc

The project is documented and ready to build HTML docs:

```bash
mvn javadoc:javadoc
```

Artifacts are emitted to `target/site/apidocs/index.html`.

## Project layout

- `src/main/java/com/sinensia/games/` — Core game logic, console runners, Spring Boot entry point.
- `src/main/java/com/sinensia/games/web/` — MVC forms and controllers for the Web UI.
- `src/main/resources/templates/` — Thymeleaf templates.
- `src/main/resources/static/` — Static assets (CSS).
- `src/test/java/com/sinensia/games/` — JUnit 5 tests.
- `pom.xml` — Maven build with the Spring Boot parent (Java 25).

## Possible next steps

- Expand test coverage (e.g., lives exhaustion, Web service tests via MockMvc).
- Add i18n bundles so the Web UI can switch languages.
- Hook the game state to persistence or WebSockets for richer experiences.

Enjoy experimenting with GuessGame!
