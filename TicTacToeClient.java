import java.awt.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.imageio.ImageIO;

public class TicTacToeClient {
    private static final int PORT = 35801;
    private JFrame frame = new JFrame("Tic-Tac-Toe");
 
    private JPanel titlePanel = new JPanel();
    private JLabel textfield = new JLabel();
    private JLabel helperMessage = new JLabel("Good news, you've connected to the server!");

    // Create TicTacToe gameBoard
    private Box[] gameBoard = new Box[9];
    private Box selSquare;

    private Socket socket;
    private Scanner input;
    private PrintWriter output;

    private void setIcon() {
		// Allows for JPane to use an icon from URL 
		try {
			URL url = new URL("https://upload.wikimedia.org/wikipedia/commons/thumb/3/32/Tic_tac_toe.svg/400px-Tic_tac_toe.svg.png");
			Image image = ImageIO.read(url);
			frame.setIconImage(image);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public TicTacToeClient(String serverAddress) throws Exception {
        // Apply Icon to JFrame
        setIcon();
        // Setup Textfield
        textfield.setText("Tic-Tac-Toe");
		textfield.setOpaque(true);
		textfield.setFont(new Font("Ink Free", Font.BOLD, 75));
		textfield.setBackground(new Color(0, 0,10));
		textfield.setForeground(new Color(123, 50, 250));
		textfield.setHorizontalAlignment(JLabel.CENTER);
        // Show a colorful title
		titlePanel.setLayout(new BorderLayout());
		titlePanel.setBounds(0, 0, 900, 200);
		titlePanel.add(textfield);
        // Add the title to the JFrame
        frame.add(titlePanel, BorderLayout.NORTH);
        // Setup the gameBoard
        socket = new Socket(serverAddress, PORT);
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream(), true);
        helperMessage.setBackground(Color.black);
        frame.getContentPane().add(helperMessage, BorderLayout.SOUTH);
        frame.setLocation((int)(Math.random() * 400) + 100, 100);
        frame.setLocation(frame.getX() + (int)(Math.random() * 400) + 100, frame.getY());
        var gameBoardPanel = new JPanel();
        gameBoardPanel.setBackground(Color.black);
        // 3 rows, 3 columns, 2 horizontal gaps, 2 vertical gaps
        gameBoardPanel.setLayout(new GridLayout(3, 3, 2, 2));
        for (var i = 0; i < gameBoard.length; i++) {
            final int j = i;
            // On click of a square, send the square number to the server, so that server can update the gameBoard
            gameBoard[i] = new Box();
            gameBoard[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    selSquare = gameBoard[j];
                    output.println("MOVE " + j);
                }
            });
            gameBoardPanel.add(gameBoard[i]);
        }
        frame.getContentPane().add(gameBoardPanel, BorderLayout.CENTER);
    }
    public void play() throws Exception {
        // Receive messages from server
        try {
            var serverMessage = input.nextLine();
            var symbol = serverMessage.charAt(8);
            var enemySymbol = symbol == 'X' ? 'O' : 'X';
            frame.setTitle("Tic-Tac-Toe Playing as: " + symbol);

            while (input.hasNextLine()) {
                serverMessage = input.nextLine();
                if (serverMessage.startsWith("VALID_MOVE")) {
                    helperMessage.setText("Valid move, please wait");
                    selSquare.updateText(symbol, 0);
                    selSquare.repaint();
                } else if (serverMessage.startsWith("OPPONENT_MOVED")) {
                    var loc = Integer.parseInt(serverMessage.substring(15));
                    gameBoard[loc].updateText(enemySymbol, 1);
                    gameBoard[loc].repaint();
                    helperMessage.setText("Opponent moved, your turn, Player " + symbol);
                } else if (serverMessage.startsWith("MESSAGE")) {
                    helperMessage.setText(serverMessage.substring(8));
                } else if (serverMessage.startsWith("VICTORY")) {
                    textfield.setText(String.format("You WON, %c!", symbol));
                    helperMessage.setText("Nicely done, you won!");
                    JOptionPane.showMessageDialog(frame, "You vanquished your opponent!");
                    break;
                } else if (serverMessage.startsWith("DEFEAT")) {
                    textfield.setText(String.format("You LOST, %c.", symbol));
                    helperMessage.setText("Better luck next time...");
                    JOptionPane.showMessageDialog(frame, "You were not victorious.");
                    break;
                } else if (serverMessage.startsWith("TIE")) {
                    textfield.setText(String.format("You TIED W/ %c.", enemySymbol));
                    helperMessage.setText("Evenly matched, you tied!");
                    JOptionPane.showMessageDialog(frame, "Tie");
                    break;
                } else if (serverMessage.startsWith("OPPONENT_LEFT")) {
                    textfield.setText(String.format("You WON by forfeit, %c!", symbol));
                    helperMessage.setText("Guess they were scared...");
                    JOptionPane.showMessageDialog(frame, "Other player ragequit/left! YOU WIN by forfeit!");
                    break;
                }
            }
            output.println("EXIT");
        } catch (Exception e) { e.printStackTrace();
        } finally {
            socket.close();
            frame.dispose();
        }
    }
    static class Box extends JPanel {
        JLabel label = new JLabel();
        public Box() {
            setBackground(Color.white);
            setLayout(new GridBagLayout());
            label.setFont(new Font("Arial", Font.BOLD, 80));
            add(label);
        }
        public void updateText(char text, int player) {
            label.setForeground(player == 0 ? Color.BLUE : Color.RED);
            label.setText(text + "");
        }
    }
    public static void main(String[] args) throws Exception {
        while (true) {
            // If no command line args are given, we connect to the local host.
            String serverIP = (args.length == 0) ? ("localhost") : (args[0]);

            // Create the client and start it running.
            TicTacToeClient client_window = new TicTacToeClient(serverIP);

            // Frame stuff
            client_window.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            client_window.frame.setSize(600, 600);
            client_window.frame.setVisible(true);
            client_window.play();
        }
    }
}
