package ch.heigvd.dai.server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import ch.heigvd.dai.server.Operation;

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
    private int nickNameIndex = 0;
    private List<Operation> operationsList;

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
                    if(clients.size() >= 20 || nickNameIndex > nickNames.length){
                        System.out.println("Max clients reached");
                        break;
                    }
                    Socket clientSocket = serverSocket.accept();
                    String nickname = nickNames[nickNameIndex++];
                    ClientHandler handler = new ClientHandler(clientSocket, this, nickname);
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

        generateOperationsList();

        // Signal the ClientHandlers that the game is starting
        gameStartLatch.countDown();
        broadcastToClients("START");

        long gameDuration = 2 * 60 * 1000; //TODO To define
        long gameEndTime = System.currentTimeMillis() + gameDuration;

        // Lancer la logique du jeu
        game(gameEndTime);

        // Après la fin du jeu
        gameRunning = false;

        // Construire et envoyer le classement
        sendLeaderboard();
    }

    // Méthode pour envoyer un message à tous les clients
    private void broadcastToClients(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private void generateOperationsList() {
        // Déterminer le nombre d'opérations à générer
        int operationsCount = 100; // Choisissez une valeur suffisamment grande
        operationsList = new ArrayList<>(operationsCount);
        for (int i = 0; i < operationsCount; i++) {
            operationsList.add(new Operation(nbOperationNumbers));
        }
    }

    public Operation getOperation(int index) {
        if (index < operationsList.size()) {
            return operationsList.get(index);
        } else {
            // Générer une nouvelle opération si on atteint la fin de la liste
            Operation newOp = new Operation(nbOperationNumbers);
            operationsList.add(newOp);
            return newOp;
        }
    }

    private void game(long gameEndTime) {
        while (System.currentTimeMillis() < gameEndTime && gameRunning) {
            try {
                Thread.sleep(1000); // Attendre un moment
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void sendLeaderboard() {
        // Construire le classement
        StringBuilder leaderboard = new StringBuilder("LEADERBOARD");
        // Trier les clients par score décroissant
        List<ClientHandler> sortedClients = new ArrayList<>(clients);
        sortedClients.sort(Comparator.comparingInt(ClientHandler::getScore).reversed());
        for (ClientHandler client : sortedClients) {
            leaderboard.append("<").append(client.getNickname()).append(">")
                    .append("<").append(client.getScore()).append(">");
        }
        // Envoyer le classement à tous les clients
        broadcastToClients(leaderboard.toString());
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
        private final String nickname;
        private int score = 0;
        private int operationIndex;

        public ClientHandler(Socket socket, Server server, String nickname) {
            this.socket = socket;
            this.server = server;
            this.nickname = nickname;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
                close();
            }
        }

        public String getNickname() {
            return nickname;
        }

        public int getScore() {
            return score;
        }

        public void sendMessage(String message) {
            try {
                out.write(message + "\n");
                out.flush();
            } catch (IOException e) {
                System.out.println("Error : " + e.getMessage());
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

                // Envoyer le pseudo au client
                sendMessage("NICKNAME:" + nickname);

                // Envoyer la première opération
                Operation currentOperation = server.getOperation(operationIndex);
                sendMessage(currentOperation.toString());

                // Logique du jeu pour le client
                while (running && server.gameRunning) {
                    // Lire la réponse du client
                    String message = in.readLine();
                    if (message == null) {
                        System.out.println("Client déconnecté.");
                        break;
                    }
                    System.out.println("Reçu du client [" + nickname + "] : " + message);

                    // Traiter la réponse du client
                    try {
                        int clientAnswer = Integer.parseInt(message.trim());
                        int correctAnswer = currentOperation.getResult();
                        if (clientAnswer == correctAnswer) {
                            score++;
                            operationIndex++;
                            // Envoyer la prochaine opération
                            currentOperation = server.getOperation(operationIndex);
                            sendMessage(currentOperation.toString());
                        } else {
                            // Envoyer "INCORRECT" et renvoyer la même opération
                            sendMessage("INCORRECT");
                            // On ne change pas l'opération actuelle
                        }
                    } catch (NumberFormatException e) {
                        // Le client a envoyé un nombre invalide
                        sendMessage("INVALID INPUT");
                    }
                }

                System.out.println("ClientHandler ferme la connexion pour " + nickname);

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
