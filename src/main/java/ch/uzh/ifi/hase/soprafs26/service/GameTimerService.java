package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameTimerService {

    private static final Logger log = LoggerFactory.getLogger(GameTimerService.class);

    private final GameRepository gameRepository;
    private final GameFlowService gameFlowService;

    public GameTimerService(GameRepository gameRepository, GameFlowService gameFlowService) {
        this.gameRepository = gameRepository;
        this.gameFlowService = gameFlowService;
    }

    @Scheduled(fixedDelay = 2000)
    public void advanceExpiredGames() {
        List<GameStatus> activeStatuses = List.of(GameStatus.ANSWERING, GameStatus.VOTING, GameStatus.ROUND_RESULT);
        List<Game> expired = gameRepository.findExpiredGames(LocalDateTime.now(), activeStatuses);
        for (Game game : expired) {
            try {
                gameFlowService.advanceStage(game.getCode());
            } catch (Exception e) {
                log.warn("Timer auto-advance failed for game {}: {}", game.getCode(), e.getMessage());
            }
        }
    }
}
