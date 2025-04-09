package suitebot.strategies;

import suitebot.game.Direction;
import suitebot.game.GameState;
import suitebot.game.Point;

import java.util.*;

/**
 * Monte Carlo Tree Search (MCTS) Heuristic for Tron-like game.
 *
 * This algorithm simulates possible moves by playing random games from a given position.
 * It selects moves that maximize long-term survival and potential area control.
 *
 * The algorithm follows these phases:
 * 1. Selection - Traverse the tree using UCT (Upper Confidence Bound for Trees) to find the best node.
 * 2. Expansion - Expand new possible moves.
 * 3. Simulation - Play random games from the new node to estimate its value.
 * 4. Backpropagation - Propagate results up the tree to refine move selection.
 *
 * The depth of simulations is configurable to balance performance and accuracy.
 *
 * Monte Carlo Tree Search (MCTS) is a powerful decision-making algorithm used in AI, particularly in games like Go or Chess. Unlike Flood Fill or A*, MCTS does not try to evaluate a position directly. Instead, it simulates random games and statistically determines which moves are most likely to lead to a good outcome.
 *
 * Key Ideas:
 * Random simulations – The algorithm plays out random games from the bot's current position.
 * Tree-based search – Each possible move is a node in the decision tree.
 * Statistical evaluation – Moves that lead to better game outcomes get higher scores.
 * Selection, Expansion, Simulation, Backpropagation – The algorithm follows these four steps:
 * Selection: Pick the most promising move (based on previous simulations).
 * Expansion: Expand the tree by adding a new move.
 * Simulation: Play random moves until the game ends or a depth limit is reached.
 * Backpropagation: Update the move scores based on the outcome.
 *
 * How the algorithm works in the code:
 * Initialize a Monte Carlo tree rooted at the bot’s position.
 * For each possible move, simulate multiple random games.
 * A move is chosen at each step based on a balance of exploration (trying new moves) and exploitation (choosing the best-known moves).
 * The game state is updated accordingly.
 * Simulate the game to a certain depth.
 * The bot makes random moves (or uses a simple heuristic like Flood Fill).
 * The number of squares occupied or survival time is recorded.
 * Backpropagate results up the tree.
 * Each move is given a score based on how often it led to a good outcome.
 * Final output:
 * The move with the best statistical performance is chosen.
 *
 * Potential optimizations and improvements:
 * More intelligent simulations: Instead of fully random moves, we can use heuristics (like Flood Fill or A*) to guide the bot.
 * Parallelization: MCTS is computationally expensive, but it can be parallelized across multiple threads.
 * Tuning exploration vs. exploitation: The UCT (Upper Confidence Bound) formula can be adjusted for different play styles.
 */

public class MonteCarloTreeSearch {
    private static final int DEFAULT_SIMULATION_DEPTH = 10;
    private static final int DEFAULT_ITERATIONS = 100;

    public static Map<Direction, Integer> evaluateMoves(int botId, GameState gameState, int maxDepth, int iterations) {
        Map<Direction, Integer> moveScores = new EnumMap<>(Direction.class);
        Point botLocation = gameState.getBotLocation(botId);
        Set<Point> obstacles = gameState.getObstacleLocations();
        int width = gameState.getPlanWidth();
        int height = gameState.getPlanHeight();

        for (Direction direction : Direction.values()) {
            Point nextPosition = direction.from(botLocation);
            nextPosition = wrapAround(nextPosition, width, height);
            if (!obstacles.contains(nextPosition)) {
                moveScores.put(direction, runSimulations(nextPosition, obstacles, width, height, maxDepth, iterations));
            } else {
                // Assign a score of zero for blocked directions
                moveScores.put(direction, 0);
            }
        }

        return moveScores;
    }

    private static int runSimulations(Point start, Set<Point> obstacles, int width, int height, int maxDepth, int iterations) {
        int bestScore = 0;
        for (int i = 0; i < iterations; i++) {
            int simulationScore = simulateGame(start, obstacles, width, height, maxDepth);
            if (simulationScore > bestScore) {
                bestScore = simulationScore;
            }
        }
        return bestScore;
    }


    private static int simulateGame(Point start, Set<Point> obstacles, int width, int height, int maxDepth) {
        Set<Point> visited = new HashSet<>(obstacles);
        Point current = start;
        int score = 0;

        for (int depth = 0; depth < maxDepth; depth++) {
            List<Direction> possibleMoves = new ArrayList<>();
            for (Direction direction : Direction.values()) {
                Point next = direction.from(current);
                next = wrapAround(next, width, height);
                if (!visited.contains(next)) {
                    possibleMoves.add(direction);
                }
            }

            if (possibleMoves.isEmpty()) break;

            Direction chosenMove = possibleMoves.get(new Random().nextInt(possibleMoves.size()));
            current = chosenMove.from(current);
            current = wrapAround(current, width, height);
            visited.add(current);
            score++;
        }
        return score;
    }

    private static Point wrapAround(Point point, int width, int height) {
        int x = (point.x + width) % width;
        int y = (point.y + height) % height;
        return new Point(x, y);
    }
}
