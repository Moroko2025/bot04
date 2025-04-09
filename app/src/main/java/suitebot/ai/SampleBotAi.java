package suitebot.ai;
import com.google.common.collect.ImmutableList;
import suitebot.game.Direction;
import suitebot.game.GameState;
import suitebot.game.ImmutableGameState;
import suitebot.game.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import suitebot.strategies.AStarHeuristic;
import suitebot.strategies.FloodFillHeuristic;

import java.util.Map;
import java.util.Comparator;
import java.util.HashSet;

/**
 * Sample AI. The AI has some serious flaws, which is intentional.
 */
public class SampleBotAi implements BotAi
{
	/**
	 * Your bot id
	 */
	private int botId;
	/**
	 * The data of the game on the basis of which you choose your move
	 */
	private GameState gameState;


	private final Predicate<Direction> isSafeDirection = direction -> (
			!gameState.getObstacleLocations().contains(destination(direction)) &&
			!gameState.getBotLocations().contains(destination(direction))
	);

	/**
	 * If a random safe move can be made (one that avoids any obstacles), do it;
	 * otherwise, go down.
	 */

	@Override
	public Direction makeMove(int botId, GameState gameState) {
        // Combine obstacles with other bots
        Set<Point> allObstacles = new HashSet<>(gameState.getObstacleLocations());
        gameState.getLiveBotIds().stream()
            .filter(id -> id != botId)
            .map(gameState::getBotLocation)
            .forEach(allObstacles::add);

        // Create enhanced state
        GameState enhancedState = ImmutableGameState.builder(gameState)
            .setObstacles(allObstacles)
            .build();

        // Get A* move evaluations
        Map<Direction, Integer> moveScores = AStarHeuristic.evaluateMoves(
            botId, 
            enhancedState, 
            15
        );

        // Select best direction
        return moveScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElseGet(() -> findSafeMove(botId, gameState));
    }

    private Direction findSafeMove(int botId, GameState state) {
        Point current = state.getBotLocation(botId);
        return Arrays.stream(Direction.values())
            .filter(dir -> isSafeMove(dir.from(current), state))
            .findFirst()
            .orElse(Direction.DOWN);
    }

    private boolean isSafeMove(Point point, GameState state) {
        return !state.getObstacleLocations().contains(point) &&
               !state.getBotLocations().contains(point);
    }

	//flood fill 
	// @Override
    // public Direction makeMove(int botId, GameState gameState) {
    //     // Create enhanced obstacles including other bots
    //     Set<Point> allObstacles = new HashSet<>(gameState.getObstacleLocations());
    //     gameState.getLiveBotIds().stream()
    //         .filter(id -> id != botId)
    //         .map(gameState::getBotLocation)
    //         .forEach(allObstacles::add);

    //     // Create enhanced game state
    //     GameState enhancedState = ImmutableGameState.builder(gameState)
    //         .setObstacles(allObstacles)
    //         .build();

    //     // Get move scores using FloodFillHeuristic
    //     Map<Direction, Integer> moveScores = FloodFillHeuristic.evaluateMoves(
    //         botId, 
    //         enhancedState, 
    //         MAX_DEPTH
    //     );

    //     // Select best move
    //     return moveScores.entrySet().stream()
    //         .max(Map.Entry.comparingByValue())
    //         .map(Map.Entry::getKey)
    //         .orElseGet(() -> findSafeMove(botId, gameState));
    // }

    // private Direction findSafeMove(int botId, GameState state) {
    //     return Arrays.stream(Direction.values())
    //         .filter(dir -> isValidMove(state, botId, dir))
    //         .findFirst()
    //         .orElse(Direction.DOWN);
    // }

    // private boolean isValidMove(GameState state, int botId, Direction dir) {
    //     Point next = dir.from(state.getBotLocation(botId));
    //     return !state.getObstacleLocations().contains(next) &&
    //            !state.getBotLocations().contains(next);
    // }



	private Point destination(Direction direction)
	{
		Point botLocation = gameState.getBotLocation(botId);

		Point stepDestination = direction.from(botLocation);
		int width = gameState.getPlanWidth();
		int height = gameState.getPlanHeight();
//		int x = (stepDestination.x + width) % width;
//		int y = (stepDestination.y + height) % height;

		stepDestination = new Point(
				(stepDestination.x + width) % width,
				(stepDestination.y + height) % height
		);
		return stepDestination;

	}

	@Override
	public String getName()
	{
		return "Chill guys";
	}
}
