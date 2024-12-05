package ch.heigvd.dai.client;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {

    private static String SERVER_HOST; // Adresse du serveur
    private static final int UDP_PORT = 42069; // Port UDP
    private static final int TCP_PORT = 42069; // Port TCP
    private static final String MULTICAST_ADDRESS = "239.165.14.215";
    private static String NETWORK_INTERFACE;

    // prevents developper from instanciating client without server ip
    private Client(){}

    public Client(String server_ip, String network_interface){
        SERVER_HOST = server_ip;
        NETWORK_INTERFACE = network_interface;
    }

    /**
     * Méthode principale pour exécuter le client.
     */
    public static void run() {
        try (MulticastSocket udpSocket = new MulticastSocket(UDP_PORT);) {

            if(NETWORK_INTERFACE.equals("CHANGE-ME!!!"))
                throw new RuntimeException("bad network interface");

            InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
            InetSocketAddress multicastGroup = new InetSocketAddress(multicastAddress, UDP_PORT);
            NetworkInterface networkInterface = NetworkInterface.getByName(NETWORK_INTERFACE);
            udpSocket.joinGroup(multicastGroup, networkInterface);
            // Étape 1 : Écouter les messages de statut du serveur via UDP

            System.out.println("Waiting for server broadcasts...");

            while (true) {
                // Recevoir le statut du serveur via UDP
                String serverStatus = receiveUdpMessage(udpSocket);
                System.out.println("Server status: " + serverStatus);

                if (serverStatus.equals("STATUS WAITING FOR PLAYERS")) {
                    // Si le serveur est prêt, tenter de rejoindre une partie
                    if(!joinGame())
                    {
                        System.out.println("Bye bye");
                        //TODO: close
                        return;
                    }

                } else if (serverStatus.equals("STATUS GAME IN PROGRESS")) {
                    System.out.println("Game in progress. Waiting...");
                } else if (serverStatus.startsWith("LEADERBOARD")) {
                    // Afficher les classements après un round
                    System.out.println("Leaderboard: " + serverStatus.substring(11));
                }

                Thread.sleep(500); // Attente entre les checks
            }
        } catch (Exception e) {
            if(NETWORK_INTERFACE.equals("CHANGE-ME!!!"))
                System.out.println("Please change the IntelliJ configuration to use your network interface name!");
            else
                e.printStackTrace();
        }
    }

    /**
     * Connecte le client au serveur pour rejoindre une partie.
     *
     * @return true si la connexion est acceptée, sinon false
     */
    private static boolean joinGame() {
        try (Socket tcpSocket = new Socket(SERVER_HOST, TCP_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(tcpSocket.getOutputStream(), StandardCharsets.UTF_8), true);
            Scanner scanner = new Scanner(System.in)){

            System.out.println("Connecting to server via TCP...");


            // Lire la réponse du serveur
            String response = in.readLine();
            if (!response.startsWith("NICKNAME ")) {

                System.out.println("Failed to join game. Response: " + response);
                return false;
            }
            String nickname = response.substring(9);
            System.out.println("Joined game as " + nickname);



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
                    // Fin de la partie
                    System.out.println("The round has ended.");
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("Failed to join game: " + e.getMessage());
            return false;
        }
        return true;
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
