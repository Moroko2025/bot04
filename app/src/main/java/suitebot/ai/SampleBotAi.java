package suitebot.ai;

import com.google.common.collect.ImmutableList;
import suitebot.game.Direction;
import suitebot.game.GameState;
import suitebot.game.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import suitebot.strategies.AStarHeuristic;
import java.util.Map;
import java.util.Comparator;
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
//
//		//Available directions - based on game plan orientation, not the bot actual direction
//		List<Direction> directions = new ArrayList<>(ImmutableList.of(
//				Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN));
//		//random schuffle directions
//		Collections.shuffle(directions);
//
//		Supplier<Stream<Direction>> safeDirectionSupplier = () -> directions.stream().filter(isSafeDirection);
//
//		//selects first available free direction (random - because the direction list is in different order every time)
//		//TODO: this is just example app,
//		// you should implement exception handling, timing verification, and proper algorithm ;o) ...
//		return Stream.of(
//				safeDirectionSupplier
//		)
//				.map(Supplier::get)
//				.map(Stream::findFirst)
//				.filter(Optional::isPresent)
//				.map(Optional::get)
//				.findFirst()
//				.orElse(Direction.DOWN);
//	}
	@Override
	public Direction makeMove(int botId, GameState gameState)
	{
		this.botId = botId;
		this.gameState = gameState;

		Map<Direction, Integer> moveScores = AStarHeuristic.evaluateMoves(botId, gameState, 10);
		if (moveScores.isEmpty()) {
			// If there are no possible moves, return Down as a default
			return Direction.DOWN;
		} else {
			// Find the maximum score
			int maxScore = Collections.max(moveScores.values());

			// Find all directions with the maximum score
			List<Direction> bestDirections = new ArrayList<>();
			for (Map.Entry<Direction, Integer> entry : moveScores.entrySet()) {
				if (entry.getValue() == maxScore) {
					bestDirections.add(entry.getKey());
				}
			}

			// If there are multiple directions with the same maximum score, choose the one that avoids obstacles
			if (bestDirections.size() > 1) {
				for (Direction direction : bestDirections) {
					Point nextPosition = direction.from(gameState.getBotLocation(botId));
					if (!gameState.getObstacleLocations().contains(nextPosition) && !gameState.getBotLocations().contains(nextPosition)) {
						return direction;
					}
				}
			}

			// If no direction avoids obstacles, return the first direction with the maximum score
			return bestDirections.get(0);
		}
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
		return "Sample AI";
	}
}
