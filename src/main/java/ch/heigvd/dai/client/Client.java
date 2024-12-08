package ch.heigvd.dai.client;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client extends Thread{

    private String SERVER_HOST; // Adresse du serveur
    private final int UDP_PORT = 42069; // Port UDP
    private final int TCP_PORT = 42069; // Port TCP
    private final String MULTICAST_ADDRESS = "239.165.14.215";
    private String NETWORK_INTERFACE;
    private AtomicBoolean runGame = new AtomicBoolean(false);

    // prevents developper from instanciating client without server ip
    private Client(){}

    public Client(String server_ip, String network_interface){
        SERVER_HOST = server_ip;
        NETWORK_INTERFACE = network_interface;
    }

    /**
     * Méthode principale pour exécuter le client.
     */
    public void startClient() {
        try (MulticastSocket udpSocket = new MulticastSocket(UDP_PORT);) {

            if(NETWORK_INTERFACE.equals("CHANGE-ME!!!"))
                throw new RuntimeException("bad network interface");

            InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
            InetSocketAddress multicastGroup = new InetSocketAddress(multicastAddress, UDP_PORT);
            NetworkInterface networkInterface = NetworkInterface.getByName(NETWORK_INTERFACE);
            udpSocket.joinGroup(multicastGroup, networkInterface);


            // launches the game round thread (run method)
            this.start();

            System.out.println("Waiting for server broadcasts...");
            while (true) {
                // Recevoir le statut du serveur via UDP
                String serverStatus = receiveUdpMessage(udpSocket);

                if (serverStatus.equals("STATUS WAITING FOR PLAYERS")) {
                    // Si le serveur est prêt, signaler au thread de jeu de se connecter en TCP
                    runGame.set(true);
                } else if (serverStatus.startsWith("LEADERBOARD")) {

                    // mettre fin au thread de jeu
                    runGame.set(false);

                    // Afficher les classements après un round
                    System.out.println();
                    System.out.println();
                    System.out.println("Leaderboard: " + serverStatus.substring(11));
                }
            }
        } catch (Exception e) {
            if(NETWORK_INTERFACE.equals("CHANGE-ME!!!"))
                System.out.println("Please change the IntelliJ configuration to use your network interface name!");
            else
                e.printStackTrace();
        }
    }

    @Override
    public void run(){
        try{
            while(true){
                if(runGame.get())
                    runRound();
                else
                    Thread.sleep(300);
            }
        } catch (InterruptedException e) {
            // terminate normally
        }
    }

    public void runRound() {
        try (Socket tcpSocket = new Socket(SERVER_HOST, TCP_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(tcpSocket.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connecting to server via TCP...");

            // Lire la réponse du serveur
            String response = in.readLine();
            if (!response.startsWith("NICKNAME ")) {

                System.out.println("Failed to join game. Response: " + response);
                return;
            }
            String nickname = response.substring(9);
            System.out.println("Joined game as " + nickname);

            System.out.println("Game started! Solve the calculations.");

            String serverMessage;
            boolean correct = true;
            while (true) {

                if (correct) {
                    serverMessage = in.readLine();
                    if (serverMessage == null)
                        break;
                    if (!serverMessage.startsWith("CALCULATION "))
                        continue;

                    // Recevoir une opération
                    String calculation = serverMessage.substring(12); // Enlever "CALCULATION "
                    System.out.println("Solve: " + calculation);
                }

                // Lire la réponse de l'utilisateur
                System.out.print("Your answer: ");
                String answer;

                while (!br.ready()) {
                    if (!runGame.get())
                        return;
                    Thread.sleep(200);
                }
                answer = br.readLine();

                // Envoyer la réponse au serveur
                out.println("ANSWER " + answer);

                // Lire la validation du serveur
                String validation = in.readLine();
                if (validation == null) {
                    // server closed the connection
                    break;
                }

                if (validation.equals("CORRECT")) {
                    System.out.println("Correct!");
                    correct = true;
                } else if (validation.equals("INCORRECT")) {
                    System.out.println("Incorrect. Try again.");
                    correct = false;
                }
            }
            System.out.println("Exited game");
        } catch (IOException e) {
            System.out.println("Failed to join game: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            // exit normally
        } finally {
            runGame.set(false);
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
