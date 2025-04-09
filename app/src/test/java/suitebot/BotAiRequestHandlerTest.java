package suitebot;

import suitebot.ai.SampleBotAi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import suitebot.ai.BotAi;
import suitebot.game.Direction;
import suitebot.game.GameState;
import suitebot.game.GameState.*;
import suitebot.game.GameStateFactory;
import suitebot.game.Point;
import suitebot.strategies.AStarHeuristic;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BotAiRequestHandlerTest
{
	private static final Direction AI_DIRECTION = Direction.LEFT;
	private static final String AI_NAME = "My AI";

	private BotRequestHandler REQUEST_HANDLER;

	private static SampleBotAi sampleBot = new SampleBotAi();

	@BeforeEach
	void setUp()
	{
		BotAi botAi = new BotAi()
		{
			@Override
			public Direction makeMove(int botId, GameState gameState)
			{
				return AI_DIRECTION;
			}

			@Override
			public String getName()
			{
				return AI_NAME;
			}
		};

		REQUEST_HANDLER = new BotRequestHandler(botAi);
	}

	@Test
	void testNameRequest() throws Exception
	{
		assertThat(REQUEST_HANDLER.processRequest(BotRequestHandler.NAME_REQUEST)).isEqualTo(AI_NAME);
	}

	private void printGameState(GameState gameState, Map<Direction, Integer> moveScores) {
		int width = gameState.getPlanWidth();
		int height = gameState.getPlanHeight();
		Set<Point> obstacles = gameState.getObstacleLocations();
		Point botLocation = gameState.getBotLocation(1);

		char[][] grid = new char[height][width];
		for (char[] row : grid) {
			Arrays.fill(row, ' ');
		}

		for (Point obs : obstacles) {
			grid[obs.y][obs.x] = '*';
		}

		grid[botLocation.y][botLocation.x] = 'B';

		for (Direction dir : moveScores.keySet()) {
			Point next = dir.from(botLocation);
			grid[next.y][next.x] = Character.forDigit(moveScores.get(dir) % 10, 10);
		}

		for (char[] row : grid) {
			System.out.println(new String(row));
		}
	}



}
