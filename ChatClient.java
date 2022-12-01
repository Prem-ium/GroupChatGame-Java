import java.io.IOException;
import java.io.*;
import java.net.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class ChatClient implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private String ip;

    public ChatClient(String ip) {
        this.ip = ip;
    }

    @Override
    public void run() {
        try {
            client = new Socket(ip, 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }
        } catch (IOException e) {
            shutDown();
        } catch (Exception e) {
            shutDown();
        }
    }

    public void shutDown() {
        try {
            done = true;
            in.close();
            out.close();

            if (!client.isClosed()) {
                client.close();
            }
        } catch(NullPointerException e){
            System.out.println("NullPointerException occured. Is the server running?");
        }catch (IOException e) { // ignore
        }
    }

    class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = inReader.readLine();
                    if (message.equals("/quit") || message.equals("/exit") || message.equals("/bye") || message.equals("/q")) {
                        out.println(message);
                        inReader.close();
                        shutDown();
                        done = true;
                    } else {
                        out.println(message);
                    }
                }
                while (true) {
                    String message = in.readLine();
                    System.out.println(message);
                }
            } catch (IOException e) {
                shutDown();
            }
        }
    }

    public static void main(String[] args) {
        String serverIP = (args.length == 0) ? ("localhost") : (args[0]);
        ChatClient client = new ChatClient(serverIP);
        client.run();
    }
}
