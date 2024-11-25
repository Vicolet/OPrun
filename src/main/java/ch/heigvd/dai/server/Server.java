package ch.heigvd.dai.server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.*;

public class Server {

    private static final int PORT = 42069;
    private static final String HOST = "localhost";
    private static final String MESSAGE = "WFP"; // Waiting For Players
    private static final int nbOperationNumbers = 4;
    private static final String[] nickNames = {"xX_EuclidSn1p3r_Xx", "ArchimedesRage", "xXx_NewtonCrush_xXx",
            "EulerXecutioner", "xX_LaplacePhantom_Xx", "GaussBlaster99", "PythagoreanPredator", "xXx_RiemannWrath_xXx",
            "FourierFlame", "DescartesDestroyer", "xX_BernoulliBlade_Xx", "CantorChaos", "xXx_TuringTyrant_xXx",
            "LeibnizLethal", "xX_HilbertHunter_Xx", "NoetherNova", "FibonacciFrenzy", "xXx_KleinCrusher_xXx",
            "RamanujanRavager", "GaloisGuardian"};


    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private CountDownLatch gameStartLatch = new CountDownLatch(1);
    private ExecutorService executor;
    private volatile boolean gameRunning = false;

    public static void main(String[] args) {
        new Server().startServer();
    }

    public void startServer() {
        while (true) {
            // Send UDP broadcast message "WFP"
            sendBroadcast();

            // Accept client connections for 1 minute
            acceptClients();

            // Check if there is at least one player
            if (!clients.isEmpty()) {
                // Start the game
                startGame();
            } else {
                System.out.println("No players connected. Restarting...");
            }

            // Close all client connections
            closeAllClients();
        }
    }

    private void sendBroadcast() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");

            byte[] buffer = MESSAGE.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddress, PORT);
            socket.send(packet);
            System.out.println("Broadcast sent");

            //Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void acceptClients() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            executor = Executors.newCachedThreadPool();
            System.out.println("Server listening on port " + PORT);

            long endTime = System.currentTimeMillis() + 60000; // Accept clients for 1 minute
            serverSocket.setSoTimeout(1000); // 1-second timeout for accept()

            while (System.currentTimeMillis() < endTime) {
                try {
                    if(clients.size() >= 20){
                        break;
                    }
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    //TODO Attribute a random nickname from the nickNames list for the client
                    clients.add(handler);
                    executor.submit(handler);
                } catch (SocketTimeoutException e) {
                    // Timeout occurred; check if time is up
                }
            }

            System.out.println("Stopped accepting clients.");

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void startGame() {
        System.out.println("Starting game with " + clients.size() + " players.");

        gameRunning = true;
        // Signal the ClientHandlers that the game is starting
        gameStartLatch.countDown();
        broadcastToClients("START");
        game();
    }

    // Méthode pour envoyer un message à tous les clients
    private void broadcastToClients(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private void game(){
        //TODO Check for the client who quit the game and handle it
        while(gameRunning){ //TODO (Countdown of M minutes not while(true))
            //TODO Create an array of X operations

            //TODO Send the operation to the connected clients CALCULATION <calculation> TCP

            //TODO Listens for the client answers ANSWER<answer> TCP

            //TODO check client answer

            //TODO if the answer is correct, send the next operation to the client and adds one point (CORRECT) TCP
            //TODO if the answer is incorrect, sends INCORRECT to the client and waits for the correct answer (INCORRECT) TCP

            //TODO repeat while the countdown is not over
        }
        //TODO Announce that the game is finished by sending LEADERBOARD<nickname1><nbPoints> <nickname2><nbPoints>,... UDP
    }

    private void closeAllClients() {
        for (ClientHandler client : clients) {
            client.close();
        }
        clients.clear();

        // Reset the gameStartLatch for the next round
        gameStartLatch = new CountDownLatch(1);
        gameRunning = false;

        // Shutdown the executor
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    static class ClientHandler implements Runnable {

        private final Socket socket;
        private final Server server;
        private volatile boolean running = true;
        private BufferedReader in;
        private BufferedWriter out;


        public ClientHandler(Socket socket, Server server) {
            this.socket = socket;
            this.server = server;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
                close();
            }
        }

        public void sendMessage(String message) {
            try {
                out.write(message + "\n");
                out.flush();
            } catch (IOException e) {
                System.out.println("Erreur lors de l'envoi du message au client : " + e.getMessage());
                close();
            }
        }

        public void close() {
            running = false;
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore exceptions during close
            }
        }

        @Override
        public void run() {
            try {
                System.out.println("[Serveur] Nouveau client connecté depuis "
                        + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

                String clientMessage = in.readLine();
                System.out.println("[Serveur a reçu des données textuelles du client] : " + clientMessage);

                // Attendre le démarrage du jeu
                server.gameStartLatch.await();

                // Informer le client que le jeu a commencé
                //sendMessage("START");

                // Logique du jeu pour le client
                while (running && server.gameRunning) {
                    // Lire les messages du client
                    String message = in.readLine();
                    if (message == null) {
                        System.out.println("Client déconnecté.");
                        //TODO Remove client from list
                        break;
                    }
                    System.out.println("Reçu du client : " + message);

                    // Traiter le message du client
                    // Par exemple, vous pouvez gérer des commandes ou des actions de jeu
                    // ...

                    // Envoyer une réponse au client
                    sendMessage("Serveur a reçu : " + message);
                }

                System.out.println("ClientHandler ferme la connexion");

            } catch (IOException e) {
                System.out.println("Erreur dans ClientHandler : " + e.getMessage());
            } catch (InterruptedException e) {
                System.out.println("ClientHandler interrompu");
                Thread.currentThread().interrupt();
            } finally {
                close();
                server.clients.remove(this);
            }
        }
    }
}
