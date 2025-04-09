package suitebot.ai;

import org.junit.jupiter.api.RepeatedTest;
import suitebot.game.Direction;
import suitebot.game.GameState;
import suitebot.game.GameStateFactory;
import suitebot.game.Point;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicBotAiTest
{
    private static final SampleBotAi sampleBot = new SampleBotAi();

    private String generateStructuredGameState(int width, int height, int botId, Point botPosition) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Point current = new Point(x, y);

                // Walls at the borders
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    sb.append('*');
                }
                // Bot position
                else if (botPosition.equals(current)) {
                    sb.append(botId);
                }
                // Random internal obstacles (~30% chance)
                else if (random.nextDouble() < 0.3) {
                    sb.append('*');
                }
                // Open space
                else {
                    sb.append(' ');
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private Point randomOpenPosition(int width, int height, Set<Point> occupied) {
        Random random = new Random();
        Point position;
        do {
            int x = 1 + random.nextInt(width - 2); // avoid borders
            int y = 1 + random.nextInt(height - 2);
            position = new Point(x, y);
        } while (occupied.contains(position));
        return position;
    }

    @RepeatedTest(15)
    void testBotAvoidsObstaclesInStructuredGrid() {
        int width = 10;
        int height = 10;
        int botId = 1;

        Set<Point> occupied = new HashSet<>();

        Point botPosition = randomOpenPosition(width, height, occupied);
        occupied.add(botPosition);

        String gameStateString = generateStructuredGameState(width, height, botId, botPosition);
        System.out.println(gameStateString);

        GameState gameState = GameStateFactory.createFromString(gameStateString);
        Direction move = sampleBot.makeMove(botId, gameState);

        Point nextPosition = move.from(botPosition);
        Point wrappedPosition = new Point(
                (nextPosition.x + width) % width,
                (nextPosition.y + height) % height
        );

        // The move must not go into a wall
        Set<Point> obstacles = gameState.getObstacleLocations();
        assertThat(obstacles).doesNotContain(wrappedPosition);
    }
}
