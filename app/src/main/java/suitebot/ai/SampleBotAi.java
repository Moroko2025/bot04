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

	@Override
	public Direction makeMove(int botId, GameState gameState)
	{
		this.botId = botId;
		this.gameState = gameState;

		Map<Direction, Integer> moveScores = AStarHeuristic.evaluateMoves(botId, gameState, 10);
		if (moveScores.isEmpty()) {
			return Direction.DOWN;
		} else {
			// maximum score
			int maxScore = Collections.max(moveScores.values());

			// Find all the directions with maximum score
			List<Direction> bestDirections = new ArrayList<>();
			for (Map.Entry<Direction, Integer> entry : moveScores.entrySet()) {
				if (entry.getValue() == maxScore) {
					bestDirections.add(entry.getKey());
				}
			}

			// If there are multiple directions with the same maximum score, choose the one that we know it's not headed to one
			if (bestDirections.size() > 1) {
				for (Direction direction : bestDirections) {
					Point nextPosition = direction.from(gameState.getBotLocation(botId));
					if (!gameState.getObstacleLocations().contains(nextPosition) && !gameState.getBotLocations().contains(nextPosition)) {
						return direction;
					}
				}
			}

			// if no one is found avoiding obstacles, just return the first one.
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
		return "Chill guys";
	}
}
