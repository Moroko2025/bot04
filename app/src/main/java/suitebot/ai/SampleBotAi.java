package suitebot.ai;
import com.google.common.collect.ImmutableList;
import suitebot.game.Direction;
import suitebot.game.GameState;
import suitebot.game.ImmutableGameState;
import suitebot.game.Point;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import suitebot.strategies.*;

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

		Map<Direction, Integer> astar= AStarHeuristic.evaluateMoves(botId, gameState, 15);
		Map<Direction, Integer> monte = MonteCarloTreeSearch.evaluateMoves(botId, gameState, 15,50);
		Map<Direction, Integer> flood = FloodFillHeuristic.evaluateMoves(botId, gameState, 15);


		if (astar.isEmpty() && monte.isEmpty() && flood.isEmpty()) {
			return Direction.DOWN;
		} else {

			int maxScoreStar = Collections.max(astar.values());
			System.out.println("Max scores a*: "+ maxScoreStar);

			int maxScoreFlood = Collections.max(flood.values());
			System.out.println("Max score flood: "+ maxScoreFlood);

			int maxScoreMonte = Collections.max(monte.values());
			System.out.println("Max score monte: "+ maxScoreMonte);

			if (Math.max(maxScoreStar, maxScoreFlood) == maxScoreStar){
				/**
				 * this one is for astar
				 * */
				// Find all the directions with maximum score
				List<Direction> bestDirectionsAstar = new ArrayList<>();
				for (Map.Entry<Direction, Integer> entry : astar.entrySet()) {
					if (entry.getValue() == maxScoreStar) {
						bestDirectionsAstar.add(entry.getKey());
					}
				}
				System.out.println("Best directions star: "+ bestDirectionsAstar);
				// If there are multiple directions with the same maximum score, choose the one that we know it's not headed to one
				if (bestDirectionsAstar.size() > 1) {
					for (Direction directionA : bestDirectionsAstar) {
						Point nextPosition = directionA.from(gameState.getBotLocation(botId));
						if (!gameState.getObstacleLocations().contains(nextPosition) && !gameState.getBotLocations().contains(nextPosition)) {
							System.out.println("1 Direction star: "+ directionA);
							return directionA;
						}
					}
				}
				// if no one is found avoiding obstacles, just return the first one.
				System.out.println("2 Direction star: "+ maxScoreStar);
				return bestDirectionsAstar.get(0);
			}

			else if (Math.max(maxScoreFlood, maxScoreMonte) == maxScoreMonte){
				/**
				 * this one is for monte
				 * */
				// Find all the directions with maximum score
				List<Direction> bestDirectionsMonte = new ArrayList<>();
				for (Map.Entry<Direction, Integer> entry : monte.entrySet()) {
					if (entry.getValue() == maxScoreMonte) {
						bestDirectionsMonte.add(entry.getKey());
					}
				}
				System.out.println("Best directions monte: "+ bestDirectionsMonte);
				// If there are multiple directions with the same maximum score, choose the one that we know it's not headed to one
				if (bestDirectionsMonte.size() > 1) {
					for (Direction directionM : bestDirectionsMonte) {
						Point nextPosition = directionM.from(gameState.getBotLocation(botId));
						if (!gameState.getObstacleLocations().contains(nextPosition) && !gameState.getBotLocations().contains(nextPosition)) {
							System.out.println("1 Direction monte: "+ directionM);
							return directionM;
						}
					}
				}
				// if no one is found avoiding obstacles, just return the first one.
				System.out.println("2 Direction monte: "+ maxScoreMonte);
				return bestDirectionsMonte.get(0);
			}
			else {
				/***
				 *
				 * this one is for flood
				 */
				// Find all the directions with maximum score
				List<Direction> bestDirectionsFlood = new ArrayList<>();
				for (Map.Entry<Direction, Integer> entry : flood.entrySet()) {
					if (entry.getValue() == maxScoreFlood) {
						bestDirectionsFlood.add(entry.getKey());
					}
				}
				System.out.println("Best directions Flood: "+ bestDirectionsFlood);
				// If there are multiple directions with the same maximum score, choose the one that we know it's not headed to one
				if (bestDirectionsFlood.size() > 1) {
					for (Direction directionF : bestDirectionsFlood) {
						Point nextPosition = directionF.from(gameState.getBotLocation(botId));
						if (!gameState.getObstacleLocations().contains(nextPosition) && !gameState.getBotLocations().contains(nextPosition)) {
							System.out.println("1 Direction Flood: "+ directionF);
							return directionF;
						}
					}
				}
				// if no one is found avoiding obstacles, just return the first one.
				System.out.println("2 Direction Flood: "+ maxScoreFlood);
				return bestDirectionsFlood.get(0);
			}






//			maxx = Math.max(bestDirectionsFlood.get(0), bestDirectionsMonte.get(0))
//			return Math.max(bestDirectionsAstar.get(0), maxx);
//			ArrayList<Map<Direction,Integer>> fi = new ArrayList<Map<Direction,Integer>>() {
//			};
//
//			fi.add(astar);
//			fi.add(monte);
//			fi.add(flood);
//
//			HashMap<Direction,ArrayList<Map<Direction,Integer>>> fin = new HashMap<Direction, ArrayList<Map<Direction,Integer>>>();
//			fin.put(bestDirectionsFlood.get(0), fi);

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
