public class QuickStart {
    // NOTE: This ATTEMPTS to quick start a new game for TESTING, however it does
    // not always work.
    // If a NullPointerException is thrown, just restart the program and it usually
    // works.
    // THIS IS FOR TESTING ONLY. The game works 100% of the time when you run it
    // from the command line.
    public static void main(String[] args) {
        // Call the main method
        try {
            // TicTacToeServer.main(null);
            String localHost = "localhost";
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

            Thread t2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TicTacToeClient.main(new String[] { localHost });
                    } catch (Exception e) {
                        System.out.println("Error: " + e);
                    }
                }
            });

            Thread t3 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TicTacToeClient.main(new String[] { localHost });
                    } catch (Exception e) {
                        System.out.println("Error: " + e);
                    }
                }
            });
            t1.start();
            t2.start();
            Thread.sleep(2000);
            t3.start();
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    QuickStart() {
        // Call the main method
        try {
            main(null);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}
