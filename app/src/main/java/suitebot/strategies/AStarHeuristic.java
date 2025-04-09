package suitebot.strategies;

/**
 * A* Search Heuristic for Tron-like game.
 *
 * This algorithm evaluates possible moves using the A* search algorithm.
 * It tries to find paths from the bot's current position to the largest open space.
 * The cost function favors moves that maximize reachable free squares while avoiding obstacles.
 *
 * The heuristic used is the Manhattan distance, considering wrap-around board behavior.
 *
 * A* is a search algorithm that finds the shortest path from a start point to a goal. However, in this game,
 * it is not used to find a specific goal but rather to estimate the best direction based on the number of reachable squares.
 *
 * Key Ideas:
 * Evaluating directions – The algorithm tries to determine which move will allow the bot to claim the most free squares.
 * Priority Queue – We use this data structure to explore the best options first.
 * Cost function (g + h):
 * g (cost so far) – The number of steps taken from the start position.
 * h (heuristic estimate) – The estimated distance to free areas (based on Manhattan distance).
 * Processing possible moves – The algorithm evaluates each possible direction.
 * How the algorithm works in the code:
 * It evaluates all possible directions (UP, DOWN, LEFT, RIGHT).
 *
 * It calculates where the bot would move next (considering wrap-around board behavior).
 * If the target square is free, it runs A* from that position.
 * A explores the area from the given position:*
 *
 * A priority queue is used to order squares based on their g + h cost.
 * Free neighboring squares are added and processed until the reachable area is fully explored.
 * Final output:
 *
 * For each direction, the algorithm calculates a score = the number of reachable squares.
 * The direction with the highest score is likely the best move.
 *
 * Potential optimizations and improvements:
 * Considering opponents: The algorithm currently ignores where opponents are—it could include a penalty for areas near enemy bots.
 * Limiting search depth: We can restrict the maximum number of steps to avoid excessive computation.
 * Advanced heuristics: For example, incorporating the probability of survival in different areas.
 */

import suitebot.game.Direction;
import suitebot.game.GameState;
import suitebot.game.Point;

import java.util.*;

public class AStarHeuristic {
    public static Map<Direction, Integer> evaluateMoves(int botId, GameState gameState, int maxDepth) {
        Map<Direction, Integer> moveScores = new EnumMap<>(Direction.class);
        Point botLocation = gameState.getBotLocation(botId);
        Set<Point> obstacles = gameState.getObstacleLocations();
        Set<Point> botObstacles = gameState.getBotLocations();

//        // Add the bot locations as obstacles
        botObstacles.addAll(obstacles);
//

        int width = gameState.getPlanWidth();
        int height = gameState.getPlanHeight();

        for (Direction direction : Direction.values()) {
            Point nextPosition = direction.from(botLocation);
            nextPosition = wrapAround(nextPosition, width, height);
            if (!obstacles.contains(nextPosition)) {
                int score = aStarSearch(nextPosition, obstacles, width, height, maxDepth);
                moveScores.put(direction, score);
            } else {
                moveScores.put(direction, 0); // Ensure all directions have a value
            }
        }
        return moveScores;
    }

    private static int aStarSearch(Point start, Set<Point> obstacles, int width, int height, int maxDepth) {
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost + n.heuristic));
        Set<Point> visited = new HashSet<>();
        queue.add(new Node(start, 0, estimateDistance(start, width, height)));
        visited.add(start);

        int maxReachable = 0;
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.cost >= maxDepth) continue;
            maxReachable++;

            for (Direction direction : Direction.values()) {
                Point next = direction.from(current.position);
                next = wrapAround(next, width, height);

                if (!visited.contains(next) && !obstacles.contains(next)) {
                    int heuristic = estimateDistance(next, width, height);
                    queue.add(new Node(next, current.cost + 1, heuristic));
                    visited.add(next);
                }
            }
        }

        return maxReachable;
    }

    private static int estimateDistance(Point p, int width, int height) {
        return Math.min(p.x, width - p.x - 1) + Math.min(p.y, height - p.y - 1);
    }

    private static Point wrapAround(Point point, int width, int height) {
        int x = (point.x + width) % width;
        int y = (point.y + height) % height;
        return new Point(x, y);
    }

    private static class Node {
        Point position;
        int cost;
        int heuristic;

        Node(Point position, int cost, int heuristic) {
            this.position = position;
            this.cost = cost;
            this.heuristic = heuristic;
        }
    }
}
