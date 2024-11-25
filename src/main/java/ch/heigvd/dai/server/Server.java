package ch.heigvd.dai.server;

import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        int port = 12345; // Port d'écoute

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running on port " + port);

            // Accepter une connexion client
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected!");

            // Streams pour la communication
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Lecture d'un message envoyé par le client
            String clientMessage = in.readLine();
            System.out.println("Client says: " + clientMessage);

            // Réponse au client
            out.println("Hello from Server! Your message was: " + clientMessage);

            // Fermer les connexions
            clientSocket.close();
            System.out.println("Client disconnected.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
