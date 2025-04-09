package suitebot.ai;

import suitebot.game.Direction;
import suitebot.game.GameState;
import suitebot.game.Move;

public interface BotAi
{
	/**
	 * Returns the move that the AI intends to play.
	 *
	 * @param botId ID of the bot operated by the AI
	 * @param gameState current game state
	 * @return the move that the AI intends to play
	 */
	Direction makeMove(int botId, GameState gameState);

	/**
	 * Returns the name of the bot.
	 *
	 * @return the name of the bot
	 */
	String getName();


}
