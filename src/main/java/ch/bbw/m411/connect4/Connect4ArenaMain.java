package ch.bbw.m411.connect4;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Plays a game of Connect Four on a 4x7 board (a variation of the original 6x7 board).
 * The pieces fall straight down, occupying the lowest available space within the column.
 */
public class Connect4ArenaMain {
    //private static final Logger log = LoggerFactory.getLogger(Connect4ArenaMain.class);

    static final int WIDTH = 7;

    static final int HEIGHT = 4;

    static final int NOMOVE = -1;

    public static void main(String[] args) {
        new Connect4ArenaMain().play(new HumanPlayer(), new Connect4AlphaBetaPlayer(10));
    }

    static String toDebugString(Stone[] board) {
        var sb = new StringBuilder();
        for (int r = 0; r < HEIGHT; r++) {
            for (int c = 0; c < WIDTH; c++) {
                var value = board[r * WIDTH + c];
                sb.append(value == null ? "." : (value == Stone.RED ? "X" : "O"));
            }
            sb.append("-");
        }
        return sb.toString();
    }

    Connect4Player play(Connect4Player red, Connect4Player blue) {
        if (red == blue) {
            throw new IllegalStateException("must be different players (simply create two instances)");
        }
        var board = new Stone[WIDTH * HEIGHT];
        red.initialize(Arrays.copyOf(board, board.length), Stone.RED);
        blue.initialize(Arrays.copyOf(board, board.length), Stone.BLUE);
        var lastMove = NOMOVE;
        var currentPlayer = red;
        for (int round = 0; round < board.length; round++) {
            var currentColor = currentPlayer == red ? Stone.RED : Stone.BLUE;
            System.out.println(HumanPlayer.toPrettyString(board) + currentColor + " to play next...");
            lastMove = currentPlayer.play(lastMove);
            if (lastMove < 0 || lastMove >= WIDTH * HEIGHT ||
                    board[lastMove] != null && (lastMove < WIDTH || board[lastMove - WIDTH] != null)) {
                throw new IllegalStateException("cannot play to position " + lastMove + " @ " + toDebugString(board));
            }
            board[lastMove] = currentColor;
            if (isWinning(board, currentColor)) {
                System.out.println(
                        HumanPlayer.toPrettyString(board) + "...and the winner is: " + currentColor + " @ " + toDebugString(board));
                return currentPlayer;
            }
            currentPlayer = currentPlayer == red ? blue : red;
        }
        System.out.println(HumanPlayer.toPrettyString(board) + "...it's a DRAW @ " + toDebugString(board));
        return null; // null implies a draw
    }

    static boolean isWinning(Stone[] board, Stone forColor) {

        for (int i = 0; i < 7; i++) {
            if (board[i] == forColor && board[i] == board[i + 7] && board[i + 7] == board[i + 14] && board[i + 14] == board[i + 21]) {
                return true;
            }
        }

        for (int i = 3; i < 7; i++) {
            if (board[i] == forColor && board[i] == board[i + 6] && board[i + 6] == board[i + 12] && board[i + 12] == board[i + 18])
                return true;
        }

        for (int i = 0; i < 4; i++) {
            if (board[i] == forColor && board[i] == board[i + 8] && board[i + 8] == board[i + 16] && board[i + 16] == board[i + 24]) {
                return true;
            }
        }

        for (int i = 0; i < 22; i += 7) {
            for (int j = i; j < i + 4; j++) {
                if (board[j] == forColor && board[j] == board[j + 1] && board[j + 1] == board[j + 2] && board[j + 2] == board[j + 3]) {
                    return true;
                }
            }
        }
        return false;
    }

    public enum Stone {
        RED, BLUE;

        public Stone opponent() {
            return this == RED ? BLUE : RED;
        }
    }

    public interface Connect4Player {

        /**
         * Called before the game starts and guaranteed to only be called once per livetime of the player.
         *
         * @param board       the starting board, usually an empty board.
         * @param colorToPlay the color of this player
         */
        void initialize(Stone[] board, Stone colorToPlay);

        /**
         * Perform a next move, will only be called if the Game is not over yet.
         * Each player has to keep an internal state of the 4x7 board, wher the 0-index is on the bottom row.
         * The index-layout looks as:
         * <pre>
         * 30 31 32 33 34 35 36
         * 14 15 16 17 18 19 29
         *  7  8  9 10 11 12 13
         *  0  1  2  3  4  5  6
         * </pre>
         *
         * @param opponendPlayed the last index where the opponent played to (in range 0 - width*height exclusive)
         *                       or -1 if this is the first move.
         * @return an index to play to (in range 0 - width*height exclusive)
         */
        int play(int opponendPlayed);
    }

    /**
     * An abstract helper class to keep track of a board (and whatever we or the opponent played).
     */
    public abstract static class DefaultPlayer implements Connect4Player {

        Stone[] board;

        Stone myColor;

        @Override
        public void initialize(Stone[] board, Stone colorToPlay) {
            this.board = board;
            myColor = colorToPlay;
        }

        @Override
        public int play(int opponendPlayed) {
            if (opponendPlayed != NOMOVE) {
                board[opponendPlayed] = myColor.opponent();
            }
            var playTo = play();
            board[playTo] = myColor;
            return playTo;
        }

        /**
         * Givent the current {@link #board}, find a suitable position-index to play to.
         *
         * @return the position to play to as defined by {@link Connect4Player#play(int)}.
         */
        abstract int play();

    }

    public static class HumanPlayer extends DefaultPlayer {

        static String toPrettyString(Stone[] board) {
            var sb = new StringBuilder();
            for (int r = HEIGHT - 1; r >= 0; r--) {
                for (int c = 0; c < WIDTH; c++) {
                    var index = r * WIDTH + c;
                    if (board[index] == null) {
                        if (index < WIDTH || board[index - WIDTH] != null) {
                            sb.append("\033[37m" + index + "\033[0m ");
                            if (index < 10) {
                                sb.append(" ");
                            }
                        } else {
                            sb.append("\033[37m.\033[0m  ");
                        }
                    } else if (board[index] == Stone.RED) {
                        sb.append("\033[1;31mX\033[0m  ");
                    } else {
                        sb.append("\033[1;34mO\033[0m  ");
                    }
                }
                sb.append("\n");
            }
            return sb.toString();
        }

        @Override
        int play() {
            System.out.println("where to to put the next " + myColor + "?");
            var scanner = new Scanner(System.in, StandardCharsets.UTF_8);
            return Integer.parseInt(scanner.nextLine());
        }

    }

    public static class GreedyPlayer extends DefaultPlayer {

        @Override
        int play() {
            for (int c = 0; c < WIDTH; c++) {
                for (int r = 0; r < HEIGHT; r++) {
                    var index = r * WIDTH + c;
                    if (board[index] == null) {
                        return index;
                    }
                }
            }
            throw new IllegalStateException("cannot play at all");
        }
    }

    public static class Connect4AlphaBetaPlayer extends DefaultPlayer {

        int bestMove = NOMOVE;

        int cutOffNodeCount = 0;

        int maxDepth;

        ArrayList<Integer> possibleMoves;

        public Connect4AlphaBetaPlayer(int depth) {
            maxDepth = depth;
        }

        @Override
        int play() {
            long startTime = System.currentTimeMillis(); // start time of move

            cutOffNodeCount = 0;

            alphaBeta(myColor, board, maxDepth, -10000, 10000);  // alphaBeta player

            long endTime = System.currentTimeMillis(); // end time of move
            long duration = endTime - startTime; // calculate difference (move time)

            System.out.println("\033[31m Execution time: " + duration + " milliseconds");
            System.out.println("\033[34m Cut offs: " + cutOffNodeCount);

            return bestMove;
        }

        private int alphaBeta(Stone myColor, Stone[] board, int depth, int alpha, int beta) {
            setPossibleMoves();

            // check if opponent wins
            if (isWinning(board, myColor.opponent())) {
                return -1000;  // we already lost :(
            }

            if (depth == 0 || possibleMoves.size() == 0) {
                return rate(myColor); // rate move
            }

            int max = alpha;

            // iterate through possible moves
            for (int move : possibleMoves) {

                board[move] = myColor; // play a stone

                int currentValue = -alphaBeta(myColor.opponent(), board, depth - 1, -beta, -max);

                board[move] = null; // revert the last move

                if (depth == maxDepth) {
                    System.out.printf("Index: " + move + " Value: " + currentValue + "\n");
                }

                if (currentValue > max) {
                    max = currentValue;
                    if (depth == maxDepth) {
                        bestMove = move; // a bit of a hack: we have to return a position (not a score)
                    }

                }
                if (max >= beta) {
                    cutOffNodeCount++;
                    break; // alpha-beta pruning
                }
            }
            return max;
        }

        public void setPossibleMoves() {
            possibleMoves = new ArrayList<>();
            int row = 3;
            for (int i = 0; i < 7; i++) {
                //little hack to improve performance
                //change sorting order
                if (i % 2 == 0) {
                    row += i;
                } else {
                    row -= i;
                }
                for (int j = row; j < 28; j += 7) {
                    if (board[j] == null) {
                        possibleMoves.add(j);
                        break;
                    }
                }
            }
        }

        public int rate(Stone myColor) {
            int[] points = {3, 4, 6, 7, 6, 4, 3
                    , 2, 4, 6, 7, 6, 4, 2
                    , 2, 4, 6, 7, 6, 4, 2
                    , 3, 4, 6, 7, 6, 4, 3
            };
            int totalPoints = 0;
            for (int i = 0; i < 28; i++) {
                if (board[i] == myColor) {
                    totalPoints += points[i];
                } else if (board[i] == myColor.opponent()) {
                    totalPoints -= points[i];
                }
            }
            return totalPoints;
        }
    }

    public static class Connect4MinMaxPlayer extends DefaultPlayer {

        int bestMove = NOMOVE;

        int nodeCount;

        int cutOffNodeCount = 0;

        int maxDepth;

        ArrayList<Integer> possibleMoves;

        public Connect4MinMaxPlayer(int depth) {
            maxDepth = depth;
        }

        @Override
        int play() {
            long startTime = System.currentTimeMillis();

            cutOffNodeCount = 0;
            possibleMoves = new ArrayList<>();

            minMax(myColor, maxDepth); // min max Player

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            System.out.println("\033[31m Execution time: " + duration + " milliseconds");
            System.out.println("\033[34m Cut offs: " + cutOffNodeCount);

            return bestMove;
        }

        long minMax(Stone myColor, int depth) {
            setPossibleMoves();
            nodeCount++;

            if (isWinning(board, myColor.opponent())) {
                return -10000;  // we already lost :(
            }

            if (possibleMoves.size() == 0) {
                return rate(myColor);  // it's a draw
            }

            if (depth == 0) {
                return rate(myColor);
            }

            long bestValue = Integer.MIN_VALUE;
            for (int i : possibleMoves) {
                if (board[i] == null) { // we can play here
                    board[i] = myColor; // play a stone

                    var currentValue = -minMax(myColor.opponent(), depth - 1);

                    board[i] = null; // revert the last move
                    if (depth == maxDepth) {
                        System.out.printf("Index: " + i + " Value: " + currentValue + "\n");
                    }
                    if (currentValue > bestValue) {
                        bestValue = currentValue;
                        if (depth == maxDepth) {
                            bestMove = i; // a bit of a hack: we have to return a position (not a score)
                        }
                    }
                }
            }
            return bestValue;
        }

        public void setPossibleMoves() {
            possibleMoves = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                for (int j = i; j < 28; j += 7) {
                    if (board[j] == null) {
                        possibleMoves.add(j);
                        break;
                    }
                }
            }
        }

        public int rate(Stone myColor) {
            int[] points = {3, 4, 6, 7, 6, 4, 3
                    , 2, 4, 6, 7, 6, 4, 2
                    , 2, 4, 6, 7, 6, 4, 2
                    , 3, 4, 6, 7, 6, 4, 3
            };
            int totalPoints = 0;
            for (int i = 0; i < 28; i++) {
                if (board[i] == myColor) {
                    totalPoints += points[i];
                } else if (board[i] == myColor.opponent()) {
                    totalPoints -= points[i];
                }
            }
            return totalPoints;
        }
    }
}
