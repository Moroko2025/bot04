package suitebot.ai;

import org.junit.jupiter.api.Test;
import suitebot.game.Direction;
import suitebot.game.GameState;
import suitebot.game.GameStateFactory;
import suitebot.game.Point;
import suitebot.strategies.AStarHeuristic;
import suitebot.ai.SampleBotAi;
import suitebot.strategies.FloodFillHeuristic;
import suitebot.strategies.MonteCarloTreeSearch;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertEquals;

class SampleBotAiTest
{
	private static SampleBotAi sampleBot = new SampleBotAi();
	@Test
	void testAvoidsObstacles()
	{
		String gameStateAsString = "***\n" +
								   "*1*\n" +
								   "   \n";

		GameState gameState = GameStateFactory.createFromString(gameStateAsString);

		assertThat(new SampleBotAi().makeMove(1, gameState)).isEqualTo(Direction.DOWN);
	}


//	@Test
//	void testTranspassGrid()
//	{
//		String gameStateAsString = "   \n"+
//								   "   \n"+
//								   "   \n";
//		GameState gameState = GameStateFactory.createFromString(gameStateAsString);
//
//		assertThat(new SampleBotAi().makeMove(1, gameState)).isEqualTo(Direction.RIGHT);
//		assertThat(new SampleBotAi().makeMove(1, gameState)).isEqualTo(Direction.RIGHT);
//		assertThat(new SampleBotAi().makeMove(1, gameState)).isEqualTo(Direction.RIGHT);
//		assertThat(new SampleBotAi().makeMove(1, gameState)).isEqualTo(Direction.RIGHT);
//		assertThat(new SampleBotAi().makeMove(1, gameState)).isEqualTo(Direction.RIGHT);
//	}


	@Test
	void testAvoidsObstacles2()
	{
		String gameStateAsString = "* **\n" +
								   "*12 \n" +
								   " 34*\n" +
								   "** *\n";

		GameState gameState = GameStateFactory.createFromString(gameStateAsString);

		assertThat(new SampleBotAi().makeMove(1, gameState)).isEqualTo(Direction.UP);
		assertThat(new SampleBotAi().makeMove(2, gameState)).isEqualTo(Direction.RIGHT);
		assertThat(new SampleBotAi().makeMove(3, gameState)).isEqualTo(Direction.LEFT);
		assertThat(new SampleBotAi().makeMove(4, gameState)).isEqualTo(Direction.DOWN);
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

	@Test
	void testMoveRequest() {
		System.out.println("first test");
		GameState gameState = GameStateFactory.createFromString("**********\n" +
				" 1 *******\n" +
				"*    *****\n" +
				"** * *****\n" +
				"* ********\n" +
				"*   ******\n" +
				"*        *\n" +
				"*        *\n" +
				"*        *\n" +
				"* ********");
		int botId = 1;
		int maxDepth = 9;
		Map<Direction, Integer> moveScores = AStarHeuristic.evaluateMoves(botId, gameState, maxDepth);
		Direction direction = sampleBot.makeMove(botId, gameState);
		System.out.println("direction: " +  direction);
		printGameState(gameState, moveScores);
//		assertEquals(29, moveScores.get(Direction.UP));
//		assertEquals(22, moveScores.get(Direction.RIGHT));
//		assertEquals(22, moveScores.get(Direction.DOWN));
//		assertEquals(0, moveScores.get(Direction.LEFT));
	}

@Test
void testMoveRequest2() {
		System.out.println("second test");
	GameState gameState = GameStateFactory.createFromString("**********\n" +
			" * *******\n" +
			"*1   *****\n" +
			"** * *****\n" +
			"* ********\n" +
			"*   ******\n" +
			"*        *\n" +
			"*        *\n" +
			"*        *\n" +
			"* ********");
	int botId = 1;
	int maxDepth = 9;
	Map<Direction, Integer> moveScores = AStarHeuristic.evaluateMoves(botId, gameState, maxDepth);
	Direction direction = sampleBot.makeMove(botId, gameState);
	System.out.println(direction);
	printGameState(gameState, moveScores);

	}
}