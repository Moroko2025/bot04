package suitebot.game;

import java.util.List;
import java.util.Set;

public interface GameState
{
	/**
	 * Returns the width of the game plan.
	 *
	 * @return the width of the game plan
	 */
	int getPlanWidth();

	/**
	 * Returns the height of the game plan.
	 *
	 * @return the height  of the game plan
	 */
	int getPlanHeight();

	/**
	 * Returns the list of the IDs of all bots, including the dead ones.
	 *
	 * @return the list of the IDs of all bots
	 */
	List<Integer> getAllBotIds();

	/**
	 * Returns the set of the IDS of all live bots, i.e. the bots that are still active in the game.
	 *
	 * @return the set of the IDS of all live bots
	 */
	Set<Integer> getLiveBotIds();

	/**
	 * Returns the coordinates of the location of the bot on the game plan.
	 *
	 * @param botId ID of the bot
	 * @return the location of the bot or null if the bot is dead
	 * @throws IllegalArgumentException if the bot ID is unknown
	 */
	Point getBotLocation(int botId);

	/**
	 * Returns the coordinates of the location of the bot heads on the game plan.
	 *
	 * @return the set of coordinates of all bot heads
	 */
	Set<Point> getBotLocations();

	/**
	 * Returns the set of coordinates of all obstacles on the game plan excluding bot heads.
	 *
	 * @return the set of coordinates of all obstacles
	 */
	Set<Point> getObstacleLocations();

	/**
	 * Returns the coordinates of the new position in the other side of the gird
	 *
	 * @return the location of the bot
	 */
	Point getGridPosition(int botId);

	
}
