package suitebot.strategies;

import suitebot.game.Direction;
import suitebot.game.GameState;
import suitebot.game.Point;

import java.util.*;

/**
 * Flood Fill Heuristic for Tron-like game
 *
 * Flood Fill is a simple but effective algorithm used to evaluate how much free space a bot can potentially occupy from its current position. It is commonly used in pathfinding and area estimation problems.
 *
 * Key Ideas:
 * It simulates expansion from the bot’s position.
 * A queue-based approach (BFS) is used to explore free squares.
 * The number of reachable squares is counted.
 * The process is repeated for each possible move direction to estimate which move leads to the largest open area.
 * How the algorithm works in the code:
 * Iterate over all possible directions (UP, DOWN, LEFT, RIGHT).
 *
 * Determine the bot's next position.
 * If the position is free, run the flood fill algorithm.
 * Flood Fill exploration:
 *
 * A queue (BFS approach) is used to expand from the given position.
 * All reachable free squares are counted while avoiding obstacles.
 * The search can be limited to a max depth to prevent excessive computation.
 * Final output:
 *
 * A score is assigned to each move based on the depth of reachable squares.
 * The move leading to the largest free space is considered the best choice.
 *
 * Potential optimizations and improvements:
 * Optimized queue handling to make the BFS faster.
 * Adding opponent-awareness to avoid areas where enemies are likely to trap the bot.
 * Combining it with other heuristics (e.g., A* for path prioritization).
 */

public class FloodFillHeuristic {
    public static Map<Direction, Integer> evaluateMoves(int botId, GameState gameState, int maxDepth) {
        Map<Direction, Integer> moveScores = new EnumMap<>(Direction.class);
        Point botLocation = gameState.getBotLocation(botId);
        Set<Point> obstacles = gameState.getObstacleLocations();
        Set<Point> botObstacles = gameState.getBotLocations();

//        // Add the bot locations as obstacles
        botObstacles.addAll(obstacles);

        int width = gameState.getPlanWidth();
        int height = gameState.getPlanHeight();

        for (Direction direction : Direction.values()) {
            Point nextPosition = wrapAround(direction.from(botLocation), width, height);
            if (!obstacles.contains(nextPosition)) {
                moveScores.put(direction, floodFillForMaxDepth(nextPosition, obstacles, width, height, maxDepth));
            } else {
                moveScores.put(direction, 0);
            }
        }
        return moveScores;
    }

    private static Point wrapAround(Point point, int width, int height) {
        return new Point((point.x + width) % width, (point.y + height) % height);
    }

    private static int floodFillForMaxDepth(Point start, Set<Point> obstacles, int width, int height, int maxDepth) {
        Set<Point> visited = new HashSet<>();
        Queue<Point> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        int depth = 0;
        while (!queue.isEmpty() && depth < maxDepth) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                Point p = queue.poll();
                for (Direction direction : Direction.values()) {
                    Point next = wrapAround(direction.from(p), width, height);
                    if (!visited.contains(next) && !obstacles.contains(next)) {
                        queue.add(next);
                        visited.add(next);
                    }
                }
            }
            depth++; // Increment depth after exploring all points at this level
        }
        return depth; // Return the maximum depth reached
    }
}
