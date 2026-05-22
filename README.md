# SoPra FS26 Group 28 – Server

## Introduction and Motivation

Drigleit is a social bluffing game: players invent believable answers to obscure trivia questions, then vote on which answer they think is the correct one. You earn a point for picking the right answer and an extra point for every player you successfully fooled into choosing yours.

We built this backend to make Drigleit accessible without friction. The original game is fun but cumbersome to set up in person, so our goal was to remove every barrier to entry:

- **Server-authoritative game flow** — rules and scoring are enforced on the server for fairness.
- **No personal data required** — players need only a username to join, no email or phone number.
- **Live translation** — questions are translated on the fly via DeepL so players can enjoy the game in their preferred language.


## Technologies Used

- Java 17
- Spring Boot 3 (Web, WebSocket, JPA)
- Gradle 8 (wrapper)
- H2 in-memory database (default local profile)
- MapStruct (DTO mapping)
- JaCoCo + SonarCloud (quality pipeline)
- Google App Engine (deployment)

## High-Level Components

1. **Application Bootstrap and Global Config**
   - *Role:* Starts the application, enables scheduled tasks, and configures CORS for cross-origin requests.
   - *Main files:* [Application.java](src/main/java/ch/uzh/ifi/hase/soprafs26/Application.java), [WebSocketConfig.java](src/main/java/ch/uzh/ifi/hase/soprafs26/config/WebSocketConfig.java)

2. **REST and WebSocket Controllers**
   - *Role:* Expose HTTP endpoints and WebSocket entry points for all game and user actions.
   - *Main files:* [GameController.java](src/main/java/ch/uzh/ifi/hase/soprafs26/controller/GameController.java), [UserController.java](src/main/java/ch/uzh/ifi/hase/soprafs26/controller/UserController.java), [GameSocketController.java](src/main/java/ch/uzh/ifi/hase/soprafs26/controller/GameSocketController.java)

3. **Domain Services (Business Logic)**
   - *Role:* Implement game phase transitions, countdown timers, authentication, translation, and social interactions (friends/invites).
   - *Main files:* [GameFlowService.java](src/main/java/ch/uzh/ifi/hase/soprafs26/service/GameFlowService.java), [GameService.java](src/main/java/ch/uzh/ifi/hase/soprafs26/service/GameService.java), [GameTimerService.java](src/main/java/ch/uzh/ifi/hase/soprafs26/service/GameTimerService.java), [UserService.java](src/main/java/ch/uzh/ifi/hase/soprafs26/service/UserService.java)

4. **Persistence Layer**
   - *Role:* Models and stores games, rounds, answers, votes, users, invites, and friendships via JPA repositories.
   - *Main files:* [Game.java](src/main/java/ch/uzh/ifi/hase/soprafs26/entity/Game.java), [Round.java](src/main/java/ch/uzh/ifi/hase/soprafs26/entity/Round.java), [Answer.java](src/main/java/ch/uzh/ifi/hase/soprafs26/entity/Answer.java), [Vote.java](src/main/java/ch/uzh/ifi/hase/soprafs26/entity/Vote.java), [GameRepository.java](src/main/java/ch/uzh/ifi/hase/soprafs26/repository/GameRepository.java)

5. **API Contract Mapping**
   - *Role:* Decouples internal domain entities from public API payloads using MapStruct mappers.
   - *Main files:* [GameStateGetDTO.java](src/main/java/ch/uzh/ifi/hase/soprafs26/rest/dto/GameStateGetDTO.java), [DTOMapper.java](src/main/java/ch/uzh/ifi/hase/soprafs26/rest/mapper/DTOMapper.java)

**Correlation summary:** Controllers receive requests and delegate to services. Services enforce business rules and mutate domain state. Entities are persisted via repositories. Updated state is returned to REST clients as DTOs and broadcast to WebSocket subscribers on the relevant game topics.

Architecture diagram: [diagrams/01_ARCHITECTURE_OVERVIEW.md](diagrams/01_ARCHITECTURE_OVERVIEW.md)

## Launch and Deployment

###  Prerequisites

- Java 17
- No external database required for local development — H2 runs in memory by default.

### Local Setup

1. Clone and enter this repository.
2. Make the Gradle wrapper executable (Linux/macOS):

```bash
chmod +x gradlew
```

3. Start the server:

```bash
./gradlew bootRun
```

4. Verify at [http://localhost:8080](http://localhost:8080) — the root endpoint should return `"The application is running."`.

### Build

```bash
./gradlew build
```

### Test

```bash
./gradlew test
```

Test coverage is tracked via JaCoCo and reported to SonarCloud. The project targets ≥ 75% coverage.

### Continuous Development Mode (Optional)

Run the build in watch mode in one terminal and the server in another:

```bash
# Terminal 1
./gradlew build --continuous -xtest

# Terminal 2
./gradlew bootRun
```

### External Dependencies

- **DeepL translation:** set the `DEEPL_API_KEY` environment variable to enable live question translation. The server starts without it, but translation endpoints will not function.
- **Database:** local development uses the embedded H2 database configured in [src/main/resources/application.properties](src/main/resources/application.properties). No setup required.

### Release / Deployment

Deployment is automated via GitHub Actions on every push to `main`:

- Workflow: [.github/workflows/main.yml](.github/workflows/main.yml)
- Pipeline stages: unit tests → JaCoCo coverage → SonarCloud analysis → Google App Engine deploy
- **Live application:** [https://sopra-fs26-group-28-server.oa.r.appspot.com](https://sopra-fs26-group-28-server.oa.r.appspot.com)

Required repository secrets:

| Secret | Purpose |
|---|---|
| `SONAR_TOKEN` | SonarCloud analysis |
| `GCP_SERVICE_CREDENTIALS` | Google App Engine deployment |
| `DEEPL_API_KEY` | Injected into `app.yaml` at deploy time |

Manual deploy (if needed):

```bash
./gradlew clean build
# then deploy using app.yaml configuration
```

See [app.yaml](app.yaml) for App Engine runtime settings.

## Roadmap

1. **WebSocket test coverage** — Add controller-level integration tests for WebSocket flows and end-to-end multiplayer scenarios to bring socket-driven game logic under automated coverage.
2. **Reconnect and host-migration resilience** — Handle mid-game disconnects gracefully: persist reconnect state, allow a new player to take over as host, and resume the round without data loss.
3. **Production database profile** — Replace the H2 in-memory store with a persistent, production-grade relational database (e.g., PostgreSQL on Cloud SQL) to support long-term statistics and session continuity.

## Authors and Acknowledgment

Core team – Group 28:

| Name | UZH Email | Matriculation Number | GitHub Username |
|---|---|---|---|
| Eneas Keller | eneasgennaro.keller@uzh.ch | 24-736-407 | EneasKe |
| Ruven Peterhans | ruvenelias.peterhans@uzh.ch | 23-728-678 | Ruven3344 |
| Luiz Hablützel | luizmartin.habluetzel@uzh.ch | 23-708-183 | luizcodes02 |
| Abraham Herzog | abrahamwalter.herzog@uzh.ch | 22-617-757 | AWHerzog |

Contribution history and weekly logs: [../sopra-fs26-group-28-client/newContributions.md](../sopra-fs26-group-28-client/newContributions.md)

**Acknowledgment:** University of Zurich SoPra FS26 course staff and the provided project starter template.

## License

This project is licensed under the **Apache License 2.0**.

See [LICENSE](LICENSE) for the full text, or visit [https://www.apache.org/licenses/LICENSE-2.0](https://www.apache.org/licenses/LICENSE-2.0).