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
		Map<Direction, Integer> floodFillScores = FloodFillHeuristic.evaluateMoves(botId, gameState, 20);
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
			score += (step + 1) * 0.5;
			score += random.nextDouble() * 0.1;
		}
		int freeSpacesAtEnd = 0;
		for (Direction dir : Direction.values()) {
			Point checkPos = dir.from(currentPos);
			checkPos = wrapAround(checkPos, gameState.getPlanWidth(), gameState.getPlanHeight());
			if (!obstacles.contains(checkPos) && !simulatedPath.contains(checkPos)) {
				freeSpacesAtEnd++;
			}
		}
		score += freeSpacesAtEnd * 2.0;
		return score;
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