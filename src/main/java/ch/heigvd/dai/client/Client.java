package ch.heigvd.dai.client;

import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        String host = "localhost"; // Adresse du serveur
        int port = 12345;         // Port du serveur

        try (Socket socket = new Socket(host, port)) {
            System.out.println("Connected to the server");

            // Streams pour la communication
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Envoyer un message au serveur
            String message = "Hello Server!";
            out.println(message);
            System.out.println("Message sent to server: " + message);

            // Lire la réponse du serveur
            String serverResponse = in.readLine();
            System.out.println("Server says: " + serverResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
