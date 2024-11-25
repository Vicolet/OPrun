package ch.heigvd.dai.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    private static final String SERVER_HOST = "localhost"; // Adresse du serveur
    private static final int UDP_PORT = 42069; // Port UDP
    private static final int TCP_PORT = 42069; // Port TCP

    /**
     * Méthode principale pour exécuter le client.
     */
    public static void run() {
        try {
            // Étape 1 : Écouter les messages de statut du serveur via UDP
            DatagramSocket udpSocket = new DatagramSocket();
            System.out.println("Waiting for server broadcasts...");

            while (true) {
                String serverStatus = receiveUdpMessage(udpSocket);
                System.out.println("Server status: " + serverStatus);

                if (serverStatus.equals("STATUS WAITING FOR PLAYERS")) {
                    // Si le serveur est prêt, établir une connexion TCP
                    if (joinGame()) {
                        // Étape 2 : Jouer au jeu en TCP
                        playGame();
                        break;
                    }
                } else if (serverStatus.equals("STATUS GAME IN PROGRESS")) {
                    System.out.println("Game in progress. Waiting...");
                } else if (serverStatus.startsWith("LEADERBOARD")) {
                    System.out.println("Leaderboard: " + serverStatus.substring(11));
                }

                Thread.sleep(2000); // Attente entre les checks
            }

            udpSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Connecte le client au serveur pour rejoindre une partie.
     *
     * @return true si la connexion est acceptée, sinon false
     */
    private static boolean joinGame() {
        try (Socket tcpSocket = new Socket(SERVER_HOST, TCP_PORT)) {
            System.out.println("Connecting to server via TCP...");
            BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

            // Lire la réponse du serveur
            String response = in.readLine();
            if (response.startsWith("OK")) {
                String nickname = response.split(" ")[1];
                System.out.println("Joined game as " + nickname);
                return true;
            } else if (response.startsWith("ERROR")) {
                int errorCode = Integer.parseInt(response.split(" ")[1]);
                if (errorCode == 1) {
                    System.out.println("Error: Server is not in a waiting state.");
                } else if (errorCode == 2) {
                    System.out.println("Error: The next round is full.");
                }
                return false;
            }
        } catch (IOException e) {
            System.out.println("Failed to join game: " + e.getMessage());
        }
        return false;
    }

    /**
     * Joue au jeu de calcul mental selon le protocole OPrun.
     */
    private static void playGame() {
        try (Socket tcpSocket = new Socket(SERVER_HOST, TCP_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
             PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Game started! Solve the calculations.");

            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                if (serverMessage.startsWith("CALCULATION")) {
                    // Recevoir une opération
                    String calculation = serverMessage.substring(12); // Enlever "CALCULATION "
                    System.out.println("Solve: " + calculation);

                    // Lire la réponse de l'utilisateur
                    System.out.print("Your answer: ");
                    String answer = scanner.nextLine();

                    // Envoyer la réponse au serveur
                    out.println("ANSWER " + answer);

                    // Lire la validation du serveur
                    String validation = in.readLine();
                    if (validation.equals("CORRECT")) {
                        System.out.println("Correct!");
                    } else if (validation.equals("INCORRECT")) {
                        System.out.println("Incorrect. Try again.");
                    }
                } else if (serverMessage.equals("END ROUND")) {
                    System.out.println("The round has ended.");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Connection lost: " + e.getMessage());
        }
    }

    /**
     * Méthode pour recevoir un message UDP.
     *
     * @param socket le socket UDP
     * @return le message reçu
     * @throws IOException en cas d'erreur
     */
    private static String receiveUdpMessage(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength());
    }
}
