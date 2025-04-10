package suitebot.strategies;

import org.junit.jupiter.api.Test;
import suitebot.game.Direction;
import suitebot.game.GameState;
import suitebot.game.GameStateFactory;
import suitebot.game.Point;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AStarHeuristicTest {
    // The game state prints only last digit of the evaluation for readability ot the table
    private void printGameState(GameState gameState, Map<Direction, Integer> moveScores) {
        int width = gameState.getPlanWidth();
        int height = gameState.getPlanHeight();
        Set<Point> obstacles = gameState.getObstacleLocations();
        Point botLocation = gameState.getBotLocation(1);

        char[][] grid = new char[height][width];
        for (char[] row : grid) {
            Arrays.fill(row, ' ');
        }

        for (Point obs : obstacles) {
            grid[obs.y][obs.x] = '*';
        }

        grid[botLocation.y][botLocation.x] = 'B';

        for (Direction dir : moveScores.keySet()) {
            Point next = dir.from(botLocation);
            grid[next.y][next.x] = Character.forDigit(moveScores.get(dir) % 10, 10);
        }

        for (char[] row : grid) {
            System.out.println(new String(row));
        }
    }

    @Test
    void testEvaluateMoves_SimpleCase() {
        GameState gameState = GameStateFactory.createFromString("**********\n" +
                "*1       *\n" +
                "*        *\n" +
                "*        *\n" +
                "*        *\n" +
                "*        *\n" +
                "*        *\n" +
                "*        *\n" +
                "*        *\n" +
                "**********");
        int botId = 1;
        int maxDepth = 9;
        Map<Direction, Integer> moveScores = AStarHeuristic.evaluateMoves(botId, gameState, maxDepth);

        System.out.println(moveScores);
        printGameState(gameState, moveScores);
        assertEquals(49, moveScores.get(Direction.RIGHT));
        assertEquals(46, moveScores.get(Direction.DOWN));
        assertEquals(0, moveScores.get(Direction.LEFT));
        assertEquals(0, moveScores.get(Direction.UP));
    }

    @Test
    void testEvaluateMoves_ObstacleBlockingPath() {
        GameState gameState = GameStateFactory.createFromString("**********\n" +
                "*1   *****\n" +
                "** *******\n" +
                "**     ***\n" +
                "**********\n" +
                "*****    *\n" +
                "*        *\n" +
                "*        *\n" +
                "*        *\n" +
                "**********");
        int botId = 1;
        int maxDepth = 9;
        Map<Direction, Integer> moveScores = AStarHeuristic.evaluateMoves(botId, gameState, maxDepth);
        printGameState(gameState, moveScores);
        assertEquals(10, moveScores.get(Direction.RIGHT));
        assertEquals(0, moveScores.get(Direction.DOWN));
    }

    @Test
    void testEvaluateMoves_MultiplePaths() {
        GameState gameState = GameStateFactory.createFromString("* ********\n" +
                "*1   *****\n" +
                "*  *******\n" +
                "**********\n" +
                "**********\n" +
                "*   ******\n" +
                "*        *\n" +
                "*        *\n" +
                "*        *\n" +
                "* ********");
        int botId = 1;
        int maxDepth = 9;
        Map<Direction, Integer> moveScores = AStarHeuristic.evaluateMoves(botId, gameState, maxDepth);
        printGameState(gameState, moveScores);
        assertEquals(29, moveScores.get(Direction.UP));
        assertEquals(22, moveScores.get(Direction.RIGHT));
        assertEquals(22, moveScores.get(Direction.DOWN));
        assertEquals(0, moveScores.get(Direction.LEFT));
    }
}

//package suitebot.strategies;
//
//import org.junit.jupiter.api.Test;
//import suitebot.game.Direction;
//import suitebot.game.GameState;
//import suitebot.game.GameStateFactory;
//import suitebot.game.Point;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//class AStarHeuristicTest {
//    // The game state prints only last digit of the evaluation for readability of the table
//    private void printGameState(GameState gameState, Map<Direction, Integer> moveScores) {
//        int width = gameState.getPlanWidth();
//        int height = gameState.getPlanHeight();
//        Set<Point> obstacles = gameState.getObstacleLocations();
//        Point botLocation = gameState.getBotLocation(1);
//
//        char[][] grid = new char[height][width];
//        for (char[] row : grid) {
//            Arrays.fill(row, ' ');
//        }
//
//        for (Point obs : obstacles) {
//            grid[obs.y][obs.x] = '*';
//        }
//
//        grid[botLocation.y][botLocation.x] = 'B';
//
//        for (Direction dir : Direction.values()) {
//            Point next = dir.from(botLocation);
//            // Wrap around if needed
//            next = new Point((next.x + width) % width, (next.y + height) % height);
//            if (moveScores.containsKey(dir)) {
//                grid[next.y][next.x] = Character.forDigit(moveScores.get(dir) % 10, 10);
//            }
//        }
//
//        System.out.println("Game board with score values (last digit):");
//        for (char[] row : grid) {
//            System.out.println(new String(row));
//        }
//
//        // Print the actual move the bot would make based on highest score
//        Direction bestMove = moveScores.entrySet().stream()
//                .max(Map.Entry.comparingByValue())
//                .map(Map.Entry::getKey)
//                .orElse(null);
//
//        System.out.println("\nBest move: " + bestMove);
//        System.out.println("Direction scores: " + moveScores);
//    }
//
//    @Test
//    void testEvaluateMoves_SimpleCase() {
//        GameState gameState = GameStateFactory.createFromString("**********\n" +
//                "*1       *\n" +
//                "*        *\n" +
//                "*        *\n" +
//                "*        *\n" +
//                "*        *\n" +
//                "*        *\n" +
//                "*        *\n" +
//                "*        *\n" +
//                "**********");
//        int botId = 1;
//        int maxDepth = 9;
//        Map<Direction, Integer> moveScores = AStarHeuristic.evaluateMoves(botId, gameState, maxDepth);
//
//        printGameState(gameState, moveScores);
//        assertEquals(49, moveScores.get(Direction.RIGHT));
//        assertEquals(46, moveScores.get(Direction.DOWN));
//        assertEquals(0, moveScores.get(Direction.LEFT));
//        assertEquals(0, moveScores.get(Direction.UP));
//    }
//
//    @Test
//    void testEvaluateMoves_ObstacleBlockingPath() {
//        GameState gameState = GameStateFactory.createFromString("**********\n" +
//                "*1   *****\n" +
//                "** *******\n" +
//                "**     ***\n" +
//                "**********\n" +
//                "*****    *\n" +
//                "*        *\n" +
//                "*        *\n" +
//                "*        *\n" +
//                "**********");
//        int botId = 1;
//        int maxDepth = 9;
//        Map<Direction, Integer> moveScores = AStarHeuristic.evaluateMoves(botId, gameState, maxDepth);
//
//        printGameState(gameState, moveScores);
//        assertEquals(10, moveScores.get(Direction.RIGHT));
//        assertEquals(0, moveScores.get(Direction.DOWN));
//    }
//
//    @Test
//    void testEvaluateMoves_MultiplePaths() {
//        GameState gameState = GameStateFactory.createFromString("* ********\n" +
//                "*1   *****\n" +
//                "*  *******\n" +
//                "**********\n" +
//                "**********\n" +
//                "*   ******\n" +
//                "*        *\n" +
//                "*        *\n" +
//                "*        *\n" +
//                "* ********");
//        int botId = 1;
//        int maxDepth = 9;
//        Map<Direction, Integer> moveScores = AStarHeuristic.evaluateMoves(botId, gameState, maxDepth);
//        printGameState(gameState, moveScores);
//        assertEquals(29, moveScores.get(Direction.UP));
//        assertEquals(22, moveScores.get(Direction.RIGHT));
//        assertEquals(22, moveScores.get(Direction.DOWN));
//        assertEquals(0, moveScores.get(Direction.LEFT));
//    }
//
//    @Test
//    void testEvaluateMoves_ComplexMaze() {
//        // Creating a more complex maze scenario with multiple possible paths
//        GameState gameState = GameStateFactory.createFromString(
//                "**********\n" +
//                        "* *   *  *\n" +
//                        "* * * * **\n" +
//                        "* * * *  *\n" +
//                        "*1  * *  *\n" +
//                        "*** *   **\n" +
//                        "*   *** **\n" +
//                        "* *      *\n" +
//                        "* ** * * *\n" +
//                        "**********");
//
//        int botId = 1;
//        // Try different depths to see how the evaluation changes
//        int[] depths = {15, 20};
//
//        for (int depth : depths) {
//            System.out.println("\n===== Testing with depth: " + depth + " =====");
//            Map<Direction, Integer> moveScores = AStarHeuristic.evaluateMoves(botId, gameState, depth);
//            printGameState(gameState, moveScores);
//
//            // Track the best direction over iterations
//            Direction bestMove = moveScores.entrySet().stream()
//                    .max(Map.Entry.comparingByValue())
//                    .map(Map.Entry::getKey)
//                    .orElse(null);
//
//            System.out.println("At depth " + depth + ", best move is: " + bestMove);
//        }
//    }
//}
