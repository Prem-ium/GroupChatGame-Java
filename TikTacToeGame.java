import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class TikTacToeGame {
    // Generate a gameBoard for the game
    private GamePlayer[] gameBoard = new GamePlayer[9];

    // Store the current player
    GamePlayer currentPlayer;

    public boolean consumedBoardGame() {
        // Returns true if the gameBoard is filled with either X or O (TIE)
        return Arrays.stream(gameBoard).allMatch(p -> p != null);
    }
    public synchronized void markPosition(int area, GamePlayer player) {
        // Check for 3 possible failures
        if (player.enemyPlayer == null) {
            throw new IllegalStateException("Searching for an enemy player...");
        } else if (player != currentPlayer) {
            throw new IllegalStateException("Not your turn yet!");
        } else if (gameBoard[area] != null) {
            throw new IllegalStateException("This position is already occupied!");
        }
        // Fill the gameBoard with the player's mark
        gameBoard[area] = currentPlayer;

        // Switch Turns
        currentPlayer = currentPlayer.enemyPlayer;
    }
    public boolean determinePossibleWinner() {
        // Check all combinations of winning positions
        return (gameBoard[0] != null && gameBoard[0] == gameBoard[1] && gameBoard[0] == gameBoard[2])
                || (gameBoard[0] != null && gameBoard[0] == gameBoard[3] && gameBoard[0] == gameBoard[6]) || (gameBoard[0] != null && gameBoard[0] == gameBoard[4] && gameBoard[0] == gameBoard[8])
                || (gameBoard[1] != null && gameBoard[1] == gameBoard[4] && gameBoard[1] == gameBoard[7]) || (gameBoard[2] != null && gameBoard[2] == gameBoard[5] && gameBoard[2] == gameBoard[8])
                || (gameBoard[2] != null && gameBoard[2] == gameBoard[4] && gameBoard[2] == gameBoard[6]) || (gameBoard[3] != null && gameBoard[3] == gameBoard[4] && gameBoard[3] == gameBoard[5])
                || (gameBoard[6] != null && gameBoard[6] == gameBoard[7] && gameBoard[6] == gameBoard[8]);
    }

    class GamePlayer implements Runnable {
        char playerSymbol;
        GamePlayer enemyPlayer;
        Socket sock;
        Scanner input;
        PrintWriter writer;

        // Constructor
        public GamePlayer(Socket sock, char playerSymbol) {
            this.sock = sock;
            this.playerSymbol = playerSymbol;
        }

        // Run Game
        @Override
        public void run() {
            try { startGame(); } 
            catch (Exception e) {  e.printStackTrace();}
            finally { checkIfAlone(); }
        }

        private void startGame() throws IOException {
            input = new Scanner(sock.getInputStream());
            writer = new PrintWriter(sock.getOutputStream(), true);
            writer.println("WELCOME " + playerSymbol);

            
            if (playerSymbol == 'X') {
                currentPlayer = this;
                writer.println("MESSAGE Waiting for enemyPlayer to connect");
            } else {
                enemyPlayer = currentPlayer;
                enemyPlayer.enemyPlayer = this;
                enemyPlayer.writer.println("MESSAGE Your move, GamePlayer" + enemyPlayer.playerSymbol);
            }
            handleInputs();
        }

        private void handleInputs() {
            while (input.hasNextLine()) {
                var command = input.nextLine();
                if (command.startsWith("EXIT")) {
                    return;
                } else if (command.startsWith("MOVE")) {
                    fulfillRequest(Integer.parseInt(command.substring(5)));
                }
            }
        }
        private void checkIfAlone() {
            if (enemyPlayer != null && enemyPlayer.writer != null) {
                enemyPlayer.writer.println("OPPONENT_LEFT");
            }
            try {sock.close();} catch (IOException e) {}
        }
        private void fulfillRequest(int location) {
            try {
                markPosition(location, this);
                writer.println("VALID_MOVE");
                enemyPlayer.writer.println("OPPONENT_MOVED " + location);
                if (determinePossibleWinner()) {
                    writer.println("VICTORY");
                    enemyPlayer.writer.println("DEFEAT");
                } else if (consumedBoardGame()) {
                    writer.println("TIE");
                    enemyPlayer.writer.println("TIE");
                }
            } catch (IllegalStateException e) {
                writer.println("MESSAGE " + e.getMessage());
            }
        }
    }
}