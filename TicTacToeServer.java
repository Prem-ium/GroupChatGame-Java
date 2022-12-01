import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
// Server class to handle client requests
public class TicTacToeServer {
    private static final int PORT = 35801;
    public static void main(String[] args) throws Exception {
        try 
        (
            // Try with Resources ServerSocket
            var sock = new ServerSocket(PORT)
        ) {
            try{
                // Display the Server's IP Address for Client to Connect
                System.out.println("Server IP Address: " + InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                System.out.println("Unable to get Server IP Address");
            }
            System.out.printf("Tic-Tac-Toe Server is running on Port %d %nWaiting on clients to connect.%n", PORT);
            var playerQueue = Executors.newFixedThreadPool(200);
            while (true) {
                // Create a new instance of the Game per 2 Clients
                TikTacToeGame game = new TikTacToeGame();

                try {
                    System.out.println();
                    // Get Client X's IP Address
                    var clientX = sock.accept();
                    System.out.println("Player X Client IP Address: " + clientX.getInetAddress().getHostAddress());
                    playerQueue.execute(game.new GamePlayer(clientX, 'X'));

                    // Get Client O's IP Address
                    var clientO = sock.accept();
                    System.out.println("Player O Client IP Address: " + clientX.getInetAddress().getHostAddress());
                    playerQueue.execute(game.new GamePlayer(clientO, 'O'));
                } catch (Exception e) {
                    System.out.println("Unable to connect to client(s)");
                    e.printStackTrace();
                }
                // Continue looping and looking for new clients afterwards
            }
        }
    }
    // Used to run the Server when TESTING using QuickStart.java
    TicTacToeServer(){
        // Call the main method
        try {
            main(null);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}