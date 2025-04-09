package suitebot.ai;

import org.junit.jupiter.api.Test;
import suitebot.game.Direction;
import suitebot.game.GameState;
import suitebot.game.GameStateFactory;

import static org.assertj.core.api.Assertions.assertThat;

class SampleBotAiTest
{
	@Test
	void testAvoidsObstacles()
	{
		String gameStateAsString = "***\n" +
								   "*1*\n" +
								   "   \n";

		GameState gameState = GameStateFactory.createFromString(gameStateAsString);

		assertThat(new SampleBotAi().makeMove(1, gameState)).isEqualTo(Direction.DOWN);
	}


	@Test
	void testTranspassGrid()
	{
		String gameStateAsString = "     \n"+
								   "     \n"+
								   "     \n";
		GameState gameState = GameStateFactory.createFromString(gameStateAsString);

		assertThat(new SampleBotAi().makeMove(1, gameState)).isEqualTo(Direction.RIGHT);
		assertThat(new SampleBotAi().makeMove(1, gameState)).isEqualTo(Direction.RIGHT);
		assertThat(new SampleBotAi().makeMove(1, gameState)).isEqualTo(Direction.RIGHT);
		assertThat(new SampleBotAi().makeMove(1, gameState)).isEqualTo(Direction.RIGHT);
		assertThat(new SampleBotAi().makeMove(1, gameState)).isEqualTo(Direction.RIGHT);
		assertThat(new SampleBotAi().makeMove(1, gameState)).isEqualTo(Direction.RIGHT);
	}


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
}