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
		// Get current location
		Point botLocation = gameState.getBotLocation(botId);
		// Get obstacles including other bots
		Set<Point> allObstacles = new HashSet<>(gameState.getObstacleLocations());
		for (Point botPos : gameState.getBotLocations()) {
			if (!botPos.equals(botLocation)) {
				allObstacles.add(botPos);
			}
		}
		// Step 1: Use FloodFill to evaluate immediate moves (maintain original strategy)
		Map<Direction, Integer> floodFillScores = FloodFillHeuristic.evaluateMoves(botId, gameState, 20);
		// Step 2: Use Monte Carlo simulation to improve move selection
		Map<Direction, Double> monteCarloScores = runMonteCarloSimulation(botLocation, allObstacles);
		// Combine both strategies
		List<Direction> validDirections = new ArrayList<>();
		Map<Direction, Double> combinedScores = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			Point nextPos = dir.from(botLocation);
			nextPos = wrapAround(nextPos, gameState.getPlanWidth(), gameState.getPlanHeight());
			// Skip immediate collisions
			if (allObstacles.contains(nextPos)) {
				continue;
			}
			validDirections.add(dir);
			// Calculate free spaces (from original code)
			int freeSpaces = countFreeSpaces(nextPos, allObstacles);
			// Combine scores with weights (adjust weights for fine-tuning)
			double floodFillScore = floodFillScores.getOrDefault(dir, 0) * 1.0;
			double monteCarloScore = monteCarloScores.getOrDefault(dir, 0.0) * 5.0; // Higher weight for Monte Carlo
			double freeSpaceScore = freeSpaces * 0.5;
			combinedScores.put(dir, floodFillScore + monteCarloScore + freeSpaceScore);
		}
		// Find best direction
		if (!validDirections.isEmpty()) {
			Direction bestDir = validDirections.get(0);
			double bestScore = combinedScores.getOrDefault(bestDir, 0.0);
			for (Direction dir : validDirections) {
				double score = combinedScores.getOrDefault(dir, 0.0);
				if (score > bestScore) {
					bestScore = score;
					bestDir = dir;
				}
			}
			return bestDir;
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
	/**
	 * Count free spaces around a position
	 */
	private int countFreeSpaces(Point position, Set<Point> obstacles) {
		int freeSpaces = 0;
		for (Direction checkDir : Direction.values()) {
			Point checkPos = checkDir.from(position);
			checkPos = wrapAround(checkPos, gameState.getPlanWidth(), gameState.getPlanHeight());
			if (!obstacles.contains(checkPos)) {
				freeSpaces++;
			}
		}
		return freeSpaces;
	}
	/**
	 * Run Monte Carlo simulations to evaluate each possible move
	 */
	private Map<Direction, Double> runMonteCarloSimulation(Point botLocation, Set<Point> obstacles) {
		Map<Direction, Double> scores = new EnumMap<>(Direction.class);
		int numSimulations = 500; // Adjust based on performance needs
		// For each possible direction
		for (Direction dir : Direction.values()) {
			Point nextPos = dir.from(botLocation);
			nextPos = wrapAround(nextPos, gameState.getPlanWidth(), gameState.getPlanHeight());
			// Skip immediate collisions
			if (obstacles.contains(nextPos)) {
				scores.put(dir, 0.0);
				continue;
			}
			double totalScore = 0.0;
			// Run multiple random simulations from this position
			for (int i = 0; i < numSimulations; i++) {
				totalScore += simulateRandomPath(nextPos, obstacles, 20); // Simulate 20 steps ahead
			}
			scores.put(dir, totalScore / numSimulations);
		}
		return scores;
	}
	/**
	 * Simulate a random path from a position and return survival score
	 */
	private double simulateRandomPath(Point startPos, Set<Point> originalObstacles, int depth) {
		// Clone obstacles to avoid modifying the original set
		Set<Point> obstacles = new HashSet<>(originalObstacles);
		Point currentPos = new Point(startPos.x, startPos.y);
		double score = 0.0;
		Random random = new Random();
		// Add starting position to the path (simulating bot's trail)
		Set<Point> simulatedPath = new HashSet<>();
		simulatedPath.add(new Point(currentPos.x, currentPos.y));
		// Run the simulation for specified depth
		for (int step = 0; step < depth; step++) {
			// Get valid directions (those that don't lead to immediate collision)
			List<Direction> validDirections = new ArrayList<>();
			for (Direction dir : Direction.values()) {
				Point nextPos = dir.from(currentPos);
				nextPos = wrapAround(nextPos, gameState.getPlanWidth(), gameState.getPlanHeight());
				if (!obstacles.contains(nextPos) && !simulatedPath.contains(nextPos)) {
					validDirections.add(dir);
				}
			}
			// If no valid directions, we're trapped
			if (validDirections.isEmpty()) {
				return score;
			}
			// Choose random direction from valid ones
			Direction randomDir = validDirections.get(random.nextInt(validDirections.size()));
			currentPos = randomDir.from(currentPos);
			currentPos = wrapAround(currentPos, gameState.getPlanWidth(), gameState.getPlanHeight());
			// Add position to simulated path
			simulatedPath.add(new Point(currentPos.x, currentPos.y));
			// Add to score - higher scores for later steps (survived longer)
			score += (step + 1) * 0.5;
			// Add some randomness to score to prevent getting stuck in local maxima
			score += random.nextDouble() * 0.1;
		}
		// Add final position evaluation - count free spaces around the final position
		int freeSpacesAtEnd = 0;
		for (Direction dir : Direction.values()) {
			Point checkPos = dir.from(currentPos);
			checkPos = wrapAround(checkPos, gameState.getPlanWidth(), gameState.getPlanHeight());
			if (!obstacles.contains(checkPos) && !simulatedPath.contains(checkPos)) {
				freeSpacesAtEnd++;
			}
		}
		// Bonus for ending in a position with more free spaces
		score += freeSpacesAtEnd * 2.0;
		return score;
	}
	// Helper method to handle wrap-around (unchanged)
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