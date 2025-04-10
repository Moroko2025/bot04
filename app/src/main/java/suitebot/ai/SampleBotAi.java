package suitebot.ai;
import com.google.common.collect.ImmutableList;
import suitebot.game.Direction;
import suitebot.game.GameState;
import suitebot.game.ImmutableGameState;
import suitebot.game.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import suitebot.strategies.AStarHeuristic;
import suitebot.strategies.FloodFillHeuristic;

import java.util.Map;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;
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

//	@Override
//	public Direction makeMove(int botId, GameState gameState)
//	{
//		this.botId = botId;
//		this.gameState = gameState;
//
//		Map<Direction, Integer> moveScores = AStarHeuristic.evaluateMoves(botId, gameState, 12);
//		if (moveScores.isEmpty()) {
//			return Direction.DOWN;
//		} else {
//			// maximum score
//			int maxScore = Collections.max(moveScores.values());
//
//			// Find all the directions with maximum score
//			List<Direction> bestDirections = new ArrayList<>();
//			for (Map.Entry<Direction, Integer> entry : moveScores.entrySet()) {
//				if (entry.getValue() == maxScore) {
//					bestDirections.add(entry.getKey());
//				}
//			}
//
//			// If there are multiple directions with the same maximum score, choose the one that we know it's not headed to one
//			if (bestDirections.size() > 1) {
//				for (Direction direction : bestDirections) {
//					Point nextPosition = direction.from(gameState.getBotLocation(botId));
//					if (!gameState.getObstacleLocations().contains(nextPosition) && !gameState.getBotLocations().contains(nextPosition)) {
//						return direction;
//					}
//				}
//			}
//
//			// if no one is found avoiding obstacles, just return the first one.
//			return bestDirections.get(0);
//		}
//	}
//	@Override
//	public Direction makeMove(int botId, GameState gameState) {
//		this.botId = botId;
//		this.gameState = gameState;
//
//		// Use the A* heuristic to evaluate each possible move
//		Map<Direction, Integer> moveScores = AStarHeuristic.evaluateMoves(botId, gameState, 20); // maxDepth of 15
//
//		// Filter out unsafe directions (those that would lead to immediate collision)
//		List<Direction> safeDirections = moveScores.entrySet().stream()
//				.filter(entry -> isSafeDirection.test(entry.getKey()))
//				.sorted(Map.Entry.<Direction, Integer>comparingByValue().reversed()) // Sort by score in descending order
//				.map(Map.Entry::getKey)
//				.collect(Collectors.toList());
//
//		// If there are safe directions, choose the one with the highest score
//		if (!safeDirections.isEmpty()) {
//			return safeDirections.get(0);
//		}
//
//		// If no safe directions, fall back to the original random method
//		List<Direction> directions = new ArrayList<>(ImmutableList.of(
//				Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN));
//		Collections.shuffle(directions);
//		return directions.stream()
//				.filter(isSafeDirection)
//				.findFirst()
//				.orElse(Direction.DOWN);
//	}
	public Direction makeMove(int botId, GameState gameState) {
		this.botId = botId;
		this.gameState = gameState;

		// Use A* heuristic with depth 20
		Map<Direction, Integer> moveScores = FloodFillHeuristic.evaluateMoves(botId, gameState, 18);

		// Get current location
		Point botLocation = gameState.getBotLocation(botId);
		Set<Point> allObstacles = new HashSet<>(gameState.getObstacleLocations());

		// Add other bots as obstacles to avoid
		for (Point botPos : gameState.getBotLocations()) {
			if (!botPos.equals(botLocation)) {
				allObstacles.add(botPos);
			}
		}

		// Create a priority list that will consider:
		// 1. Safety (not hitting anything)
		// 2. Score (preferring higher scores)
		// 3. Free space in immediate vicinity (avoiding tight corridors)
		List<Direction> prioritizedDirections = new ArrayList<>();

		// Create a map to store free spaces in each direction
		Map<Direction, Integer> freeSpaceCount = new EnumMap<>(Direction.class);

		for (Direction dir : Direction.values()) {
			Point nextPos = dir.from(botLocation);
			nextPos = wrapAround(nextPos, gameState.getPlanWidth(), gameState.getPlanHeight());

			// Skip immediate collisions
			if (allObstacles.contains(nextPos)) {
				continue;


			}

			// Calculate free spaces in immediate vicinity (1 step in each direction)
			int freeSpaces = 0;
			for (Direction checkDir : Direction.values()) {
				Point checkPos = checkDir.from(nextPos);
				checkPos = wrapAround(checkPos, gameState.getPlanWidth(), gameState.getPlanHeight());
				if (!allObstacles.contains(checkPos)) {
					freeSpaces++;
				}
			}

			freeSpaceCount.put(dir, freeSpaces);
			prioritizedDirections.add(dir);
		}

		// Sort directions by:
		// 1. A* score (higher is better)
		// 2. Free space count (higher is better)
		if (!prioritizedDirections.isEmpty()) {
			prioritizedDirections.sort((dir1, dir2) -> {
				int score1 = moveScores.getOrDefault(dir1, 0);
				int score2 = moveScores.getOrDefault(dir2, 0);

				if (score1 != score2) {
					return Integer.compare(score2, score1); // Higher score first
				}

				// If scores are equal, prefer direction with more free spaces
				return Integer.compare(freeSpaceCount.getOrDefault(dir2, 0),
						freeSpaceCount.getOrDefault(dir1, 0));
			});

			return prioritizedDirections.get(0);
		}

		// If no good moves, try random directions as a last resort
		List<Direction> directions = new ArrayList<>(Arrays.asList(
				Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN));
		Collections.shuffle(directions);

		for (Direction dir : directions) {
			Point nextPos = dir.from(botLocation);
			nextPos = wrapAround(nextPos, gameState.getPlanWidth(), gameState.getPlanHeight());
			if (!allObstacles.contains(nextPos)) {
				return dir;
			}
		}

		// If all else fails
		return Direction.DOWN;
	}

	// Helper method to handle wrap-around
	private Point wrapAround(Point point, int width, int height) {
		int x = (point.x + width) % width;
		int y = (point.y + height) % height;
		return new Point(x, y);
	}

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