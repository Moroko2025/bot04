package suitebot.ai;

import org.junit.jupiter.api.Test;
import suitebot.game.Direction;
import suitebot.game.GameState;
import suitebot.game.GameStateFactory;
import suitebot.game.Point;

import java.util.*;

import static java.lang.Thread.sleep;

class BotAnimationTest
{
    private static final SampleBotAi sampleBot = new SampleBotAi();

    private String generateGameStateString(int width, int height, int botId, Point botPosition, Set<Point> obstacles) {
        StringBuilder sb = new StringBuilder();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Point current = new Point(x, y);
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    sb.append('*'); // borders
                } else if (botPosition.equals(current)) {
                    sb.append(botId); // bot position
                } else if (obstacles.contains(current)) {
                    sb.append('*'); // obstacles + bot trail
                } else {
                    sb.append(' ');
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private void printGameState(String gameStateString) {
        System.out.print("\033[H\033[2J"); // Clear screen ANSI escape code
        System.out.flush();
        System.out.println(gameStateString);
    }

    @Test
    void animateBotWithTrail() throws InterruptedException {
        int width = 25;
        int height = 25;
        int botId = 1;
        int steps = 25;

        Random random = new Random();

        // Generate random obstacles (less density)
        Set<Point> obstacles = new HashSet<>();
        int obstacleCount = 50; // fewer obstacles for more freedom
        while (obstacles.size() < obstacleCount) {
            int x = 1 + random.nextInt(width - 2);
            int y = 1 + random.nextInt(height - 2);
            obstacles.add(new Point(x, y));
        }

        // Initial bot position
        Point botPosition = new Point(1 + random.nextInt(width - 2), 1 + random.nextInt(height - 2));
        while (obstacles.contains(botPosition)) {
            botPosition = new Point(1 + random.nextInt(width - 2), 1 + random.nextInt(height - 2));
        }

        for (int i = 0; i < steps; i++) {
            String gameStateString = generateGameStateString(width, height, botId, botPosition, obstacles);
            printGameState(gameStateString);
            sleep(300); // Control speed
            System.out.println(i);

            GameState gameState = GameStateFactory.createFromString(gameStateString);
            Direction move = sampleBot.makeMove(botId, gameState);

            // Mark current position as obstacle (simulate trail)
            obstacles.add(botPosition);

            // Move bot
            botPosition = new Point(
                    (botPosition.x + move.dx + width) % width,
                    (botPosition.y + move.dy + height) % height
            );

            // Check if bot is trapped (all directions blocked)
            boolean trapped = true;
            for (Direction direction : Direction.values()) {
                Point next = new Point(
                        (botPosition.x + direction.dx + width) % width,
                        (botPosition.y + direction.dy + height) % height
                );
                if (!obstacles.contains(next)) {
                    trapped = false;
                    break;
                }
            }
            if (trapped) {
                System.out.println("Bot is trapped! Ending animation.");
                break;
            }
        }

        // Final state
        String finalState = generateGameStateString(width, height, botId, botPosition, obstacles);
        printGameState(finalState);
    }
}