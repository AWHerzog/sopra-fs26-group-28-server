# Game Backend Implementation Checklist (Java)

This checklist is tailored to the current server structure.

## 0) Preparation

- [x] Create a feature branch (e.g., `feature/game-backend-flow`)
- [x] Run baseline tests and ensure green:
  - `./gradlew test`
- [x] Confirm API contract with frontend types:
  - `../sopra-fs26-group-28-client/app/types/game.ts`
  - `../sopra-fs26-group-28-client/app/api/websocket.ts`

## 1) Expand Game State Model

Target files:
- Existing: `src/main/java/ch/uzh/ifi/hase/soprafs26/constant/GameStatus.java`
- Existing: `src/main/java/ch/uzh/ifi/hase/soprafs26/entity/Game.java`

Tasks:
- [ ] Extend `GameStatus` beyond WAITING if needed:
  - Suggested: `WAITING`, `ANSWERING`, `VOTING`, `ROUND_RESULT`, `FINISHED`
- [ ] Add round metadata to `Game`:
  - [ ] `currentRound`
  - [ ] `maxRounds`
  - [ ] `currentQuestionId` (or question text/key)
  - [ ] `stageDeadline` (server-authoritative timeout)
- [ ] Keep existing `players` map as total scoreboard (username -> points)
- [ ] Add host/game constraints validation in service layer (do not trust DTO only)

## 2) Add Round-Level Persistence

Create new files:
- [ ] `src/main/java/ch/uzh/ifi/hase/soprafs26/entity/Round.java`
- [ ] `src/main/java/ch/uzh/ifi/hase/soprafs26/entity/Answer.java`
- [ ] `src/main/java/ch/uzh/ifi/hase/soprafs26/entity/Vote.java`

Create new repositories:
- [ ] `src/main/java/ch/uzh/ifi/hase/soprafs26/repository/RoundRepository.java`
- [ ] `src/main/java/ch/uzh/ifi/hase/soprafs26/repository/AnswerRepository.java`
- [ ] `src/main/java/ch/uzh/ifi/hase/soprafs26/repository/VoteRepository.java`

Model constraints to enforce:
- [ ] Exactly one answer per user per round
- [ ] Exactly one vote per user per round
- [ ] No vote before all required answers (or timeout)
- [ ] Optional: no self-vote rule

Implementation notes:
- [ ] Add DB-level uniqueness constraints where possible
- [ ] Keep foreign keys to game/round/user identifiers explicit

## 3) Define DTOs and Mapper Updates

Target files:
- Existing: `src/main/java/ch/uzh/ifi/hase/soprafs26/rest/dto/GameGetDTO.java`
- Existing: `src/main/java/ch/uzh/ifi/hase/soprafs26/rest/dto/GamePostDTO.java`
- Existing: `src/main/java/ch/uzh/ifi/hase/soprafs26/rest/mapper/DTOMapper.java`

Create new DTOs (suggested):
- [ ] `src/main/java/ch/uzh/ifi/hase/soprafs26/rest/dto/GameStartPostDTO.java`
- [ ] `src/main/java/ch/uzh/ifi/hase/soprafs26/rest/dto/AnswerPostDTO.java`
- [ ] `src/main/java/ch/uzh/ifi/hase/soprafs26/rest/dto/VotePostDTO.java`
- [ ] `src/main/java/ch/uzh/ifi/hase/soprafs26/rest/dto/GameStateGetDTO.java`

Tasks:
- [ ] Expose enough state in `GameGetDTO`/`GameStateGetDTO` for frontend stage rendering
- [ ] Map entities -> DTO in `DTOMapper` without leaking internal persistence details
- [ ] Add fields for round, stage, timer/deadline, submitted flags, and scores

## 4) Implement Core Game Logic in Service Layer

Target files:
- Existing: `src/main/java/ch/uzh/ifi/hase/soprafs26/service/GameService.java`

Suggested new service (recommended for clarity):
- [ ] `src/main/java/ch/uzh/ifi/hase/soprafs26/service/GameFlowService.java`

Core methods to implement:
- [ ] `startGame(gameCode, hostUser)`
- [ ] `submitAnswer(gameCode, user, payload)`
- [ ] `submitVote(gameCode, user, payload)`
- [ ] `advanceStage(gameCode)` (single transition gate)
- [ ] `computeRoundScores(gameCode, round)`
- [ ] `finishGame(gameCode)`
- [ ] `getCurrentGameState(gameCode, user)`

Safety checks:
- [ ] Validate authenticated user is in game
- [ ] Validate host-only actions (start/end)
- [ ] Validate action allowed in current stage
- [ ] Return consistent exceptions (`ResponseStatusException` + message)

Transaction boundaries:
- [ ] Keep mutation methods transactional
- [ ] Ensure idempotency where retries can happen (socket reconnect/retry)

## 5) Expose REST Endpoints

Target files:
- Existing: `src/main/java/ch/uzh/ifi/hase/soprafs26/controller/GameController.java`

Add endpoints (example contract):
- [ ] `POST /games/{code}/start`
- [ ] `POST /games/{code}/answers`
- [ ] `POST /games/{code}/votes`
- [ ] `GET /games/{code}/state`
- [ ] Optional admin/host: `POST /games/{code}/advance`

Controller tasks:
- [ ] Keep controller thin (auth extraction + delegation only)
- [ ] Reuse `UserService.checkTokenAuthenticity(...)`
- [ ] Return DTOs, not entities

## 6) WebSocket Broadcasts for Live Updates

Target files:
- Existing: `src/main/java/ch/uzh/ifi/hase/soprafs26/controller/GameSocketController.java`
- Existing: `src/main/java/ch/uzh/ifi/hase/soprafs26/config/WebSocketConfig.java`
- Existing sender location: `src/main/java/ch/uzh/ifi/hase/soprafs26/service/GameService.java`

Tasks:
- [ ] Standardize one topic per game: `/topic/game/{code}`
- [ ] Broadcast updated game state after every successful mutation
- [ ] Replace/remove placeholder socket handler in `GameSocketController`
- [ ] Keep server as source of truth; frontend can always re-fetch via REST

## 7) Timeout and Auto-Progress

Suggested new component:
- [ ] `src/main/java/ch/uzh/ifi/hase/soprafs26/service/GameSchedulerService.java`

Tasks:
- [ ] Add configurable stage duration in properties
- [ ] On deadline, auto-advance stage if requirements unmet
- [ ] Prevent duplicate stage-advance race conditions
- [ ] Broadcast state after auto-transition

Optional config target:
- [ ] `src/main/resources/application.properties`

## 8) Error Handling and Validation

Target files:
- Existing: `src/main/java/ch/uzh/ifi/hase/soprafs26/exceptions/GlobalExceptionAdvice.java`

Tasks:
- [ ] Keep error responses consistent across endpoints
- [ ] Add clear messages for rule violations:
  - answer already submitted
  - vote already submitted
  - invalid stage
  - user not in game
  - unauthorized host action

## 9) Testing (Required)

Existing tests to extend:
- [ ] `src/test/java/ch/uzh/ifi/hase/soprafs26/service/GameServiceTest.java`

Create new tests:
- [ ] `src/test/java/ch/uzh/ifi/hase/soprafs26/controller/GameControllerTest.java`
- [ ] `src/test/java/ch/uzh/ifi/hase/soprafs26/service/GameFlowServiceTest.java`

Test cases checklist:
- [ ] create game -> join game -> start game happy path
- [ ] submit answer valid/duplicate/wrong stage
- [ ] submit vote valid/duplicate/self-vote (if forbidden)
- [ ] scoring correctness per round
- [ ] final game completion and winner determination
- [ ] websocket broadcast triggered after mutations (service-level)
- [ ] timeout auto-progress behavior

Run commands:
- [ ] `./gradlew test`
- [ ] `./gradlew build`

## 10) Integration and Rollout

- [ ] Verify frontend pages can progress across all stages:
  - waiting -> answer -> voting -> solution/result -> next round -> finish
- [ ] Verify reconnect flow: REST state fetch + websocket resume
- [ ] Confirm no regression in existing user/game creation flows
- [ ] Update README or API docs with new endpoints and payload examples

## Definition of Done

- [ ] A full multiplayer game runs from waiting room to final result entirely server-driven
- [ ] Stage transitions and scoring are enforced by backend rules
- [ ] Invalid actions are rejected with clear HTTP errors
- [ ] Frontend receives live updates and recovers after reconnect
- [ ] Test suite covers happy path + critical edge cases
