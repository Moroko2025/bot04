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
        //Available directions - based on game plan orientation, not the bot actual direction
        List<Direction> directions = new ArrayList<>(ImmutableList.of(
                Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN));
        //random schuffle directions
        Collections.shuffle(directions);
        Supplier<Stream<Direction>> safeDirectionSupplier = () -> directions.stream().filter(isSafeDirection);
        //selects first available free direction (random - because the direction list is in different order every time)
        //TODO: this is just example app,
        // you should implement exception handling, timing verification, and proper algorithm ;o) ...
        return Stream.of(
                safeDirectionSupplier
        )
                .map(Supplier::get)
                .map(Stream::findFirst)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(Direction.DOWN);
    }
    private Point destination(Direction direction)
    {
        Point botLocation = gameState.getBotLocation(botId);
        /**
         * TODO: this method does not care about game plan is without borders
         */
        Point stepDestination = direction.from(botLocation);
        return stepDestination;
    }
    @Override
    public String getName()
    {
        return "Sample AI";
    }
}