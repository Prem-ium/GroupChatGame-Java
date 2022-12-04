import java.io.*;
import java.net.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer implements Runnable {
    private ArrayList<ConnectionHandler> clients = new ArrayList<ConnectionHandler>();
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public ChatServer() {
        clients = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try{
            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TicTacToeServer.main(null);
                    } catch (Exception e) {
                        System.out.println("Error: " + e);
                    }
                }
            });
            t1.start();
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                clients.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            shutDown();
        }

    }

    public void brodcast(String message) {
        for (ConnectionHandler client : clients) {
            if (client != null) {
                client.sendMessage(message);
            }
        }
    }

    public void shutDown() {
        try {
            done = true;
            pool.shutdown();
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler client : clients) {
                client.shutDown();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String name;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.printf("Commands:%n/nick <name> - Change Username%n/tictactoe - Start TicTacToe game.%n(More coming soon?)%n%n");
                while (name == null) {
                    out.println("Enter a name:");
                    name = in.readLine();
                }
                out.println("Welcome " + name);
                System.out.println(name + " has joined the chat");
                brodcast(name + " has joined the chat");
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/nick")) {
                        if (message.length() > 6) {
                            String newName = message.substring(6);
                            brodcast(name + " is now known as " + newName);
                            name = newName;
                        } else {
                            out.println("Invalid name");
                        }
                    } else if (message.startsWith("/tictactoe")) {
                        //String ip =(message.length() > 11) ? "localhost": message.substring(11, message.length());
                        brodcast(name + " has started searching a game of Tic Tac Toe");


                        Thread t2 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    TicTacToeClient.main(new String[] { "localHost" });
                                } catch (Exception e) {
                                    System.out.println("Error: " + e);
                                }
                            }
                        });

                        t2.start();

                    } else if (message.startsWith("/quit") || message.equals("/exit") || message.equals("/q")) {
                        brodcast(name + " has left the chat");
                        shutDown();
                    }
                    System.out.println(name + ": " + message);
                    brodcast(name + ": " + message);
                }
            } catch (Exception e) {
                shutDown();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutDown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {}
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.run();
    }
}
