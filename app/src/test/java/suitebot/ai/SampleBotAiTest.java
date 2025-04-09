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

	

    @Test
    public void testBotNavigationWithObstacles() {
        // Setup test board (10x10)
        int width = 10;
        int height = 10;
        Set<Point> obstacles = new HashSet<>();
        
        // Add static obstacles
        obstacles.add(new Point(2, 2));
        obstacles.add(new Point(2, 3));
        obstacles.add(new Point(2, 4));
        obstacles.add(new Point(7, 5));
        obstacles.add(new Point(7, 6));
        obstacles.add(new Point(7, 7));

        // Add other bots
        Map<Integer, Point> botPositions = new HashMap<>();
        botPositions.put(1, new Point(0, 0));  // Our test bot
        botPositions.put(2, new Point(3, 3));
        botPositions.put(3, new Point(6, 6));
        botPositions.put(4, new Point(8, 8));

        // Create game state
        GameState gameState = new GameState.Builder()
            .setPlanWidth(width)
            .setPlanHeight(height)
            .setObstacles(obstacles)
            .setBotPositions(botPositions)
            .build();

        SampleBotAi botAi = new SampleBotAi();

        // Test sequence of moves
        for (int i = 0; i < 10; i++) {
            Direction move = botAi.makeMove(1, gameState);
            Point newPos = move.from(gameState.getBotLocation(1));
            
            // Assertions
            assertFalse("Bot should not collide with obstacles",
                obstacles.contains(newPos));
            
            assertFalse("Bot should not collide with other bots",
                botPositions.values().contains(newPos));
            
            // Update bot position for next iteration
            botPositions.put(1, newPos);
            gameState = new GameState.Builder()
                .setPlanWidth(width)
                .setPlanHeight(height)
                .setObstacles(obstacles)
                .setBotPositions(botPositions)
                .build();
        }
    }

    @Test
    public void testBotWraparoundBehavior() {
        // Setup smaller board (5x5) to force wrap-around
        int width = 5;
        int height = 5;
        Set<Point> obstacles = new HashSet<>();
        
        // Add obstacles creating a corridor
        obstacles.add(new Point(1, 1));
        obstacles.add(new Point(1, 2));
        obstacles.add(new Point(1, 3));
        obstacles.add(new Point(3, 1));
        obstacles.add(new Point(3, 2));
        obstacles.add(new Point(3, 3));

        Map<Integer, Point> botPositions = new HashMap<>();
        botPositions.put(1, new Point(0, 0));
        botPositions.put(2, new Point(4, 4));

        GameState gameState = new GameState.Builder()
            .setPlanWidth(width)
            .setPlanHeight(height)
            .setObstacles(obstacles)
            .setBotPositions(botPositions)
            .build();

        SampleBotAi botAi = new SampleBotAi();
        Direction move = botAi.makeMove(1, gameState);
        Point newPos = move.from(gameState.getBotLocation(1));

        assertTrue("Bot should handle wrap-around correctly",
            newPos.x >= 0 && newPos.x < width &&
            newPos.y >= 0 && newPos.y < height);
    }

    @Test
    public void testBotObstacleAvoidance() {
        // Setup board with obstacle trap
        int width = 7;
        int height = 7;
        Set<Point> obstacles = new HashSet<>();
        
        // Create U-shaped trap
        obstacles.add(new Point(2, 1));
        obstacles.add(new Point(2, 2));
        obstacles.add(new Point(2, 3));
        obstacles.add(new Point(3, 3));
        obstacles.add(new Point(4, 3));
        obstacles.add(new Point(4, 2));
        obstacles.add(new Point(4, 1));

        Map<Integer, Point> botPositions = new HashMap<>();
        botPositions.put(1, new Point(3, 2));  // Bot in potential trap
        botPositions.put(2, new Point(0, 0));

        GameState gameState = new GameState.Builder()
            .setPlanWidth(width)
            .setPlanHeight(height)
            .setObstacles(obstacles)
            .setBotPositions(botPositions)
            .build();

        SampleBotAi botAi = new SampleBotAi();
        Direction move = botAi.makeMove(1, gameState);
        Point newPos = move.from(gameState.getBotLocation(1));

        assertFalse("Bot should escape from trap",
            obstacles.contains(newPos));
    }
}