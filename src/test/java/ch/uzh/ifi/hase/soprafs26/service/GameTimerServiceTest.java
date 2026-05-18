package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

public class GameTimerServiceTest {

    @Test
    public void advanceExpiredGames_callsAdvanceStage() {
        GameRepository repo = mock(GameRepository.class);
        GameFlowService flow = mock(GameFlowService.class);

        Game g = new Game();
        g.setCode("ABC123");

        when(repo.findExpiredGames(any(LocalDateTime.class), anyList()))
                .thenReturn(List.of(g));

        GameTimerService timer = new GameTimerService(repo, flow);
        timer.advanceExpiredGames();

        verify(flow, times(1)).advanceStage("ABC123");
    }

    @Test
    public void advanceExpiredGames_handlesExceptionsGracefully() {
        GameRepository repo = mock(GameRepository.class);
        GameFlowService flow = mock(GameFlowService.class);

        Game g1 = new Game(); g1.setCode("GOOD");
        Game g2 = new Game(); g2.setCode("BAD");

        when(repo.findExpiredGames(any(LocalDateTime.class), anyList()))
                .thenReturn(List.of(g1, g2));

        when(flow.advanceStage("GOOD")).thenReturn(null);
        when(flow.advanceStage("BAD")).thenThrow(new RuntimeException("boom"));

        GameTimerService timer = new GameTimerService(repo, flow);
        // should not throw
        timer.advanceExpiredGames();

        verify(flow).advanceStage("GOOD");
        verify(flow).advanceStage("BAD");
    }
}
