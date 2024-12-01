package ch.heigvd.dai;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "oprun",
        description = "Client - server game of quick maths !",
        version = "oprun 1.0",
        subcommands = {OpRunApp.Client.class, OpRunApp.Server.class},
        mixinStandardHelpOptions = true)
public class OpRunApp {

    /**
     * Entry point
     * @param args arguments to give to the program
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new OpRunApp()).execute(args);
        System.exit(exitCode);
    }


    @Command(name="client", description = "launches the game in client mode",
            mixinStandardHelpOptions = true)
    static class Client implements Runnable {

        @Option(names="--ip", description = "Ip of the server to connect to", required=true)
        String serverIp;

        @Override
        public void run() {
            System.out.println("Launching in client mode...");
            System.out.println("Server IP: " + serverIp);
        }
    }

    @Command(name="server", description = "launches game server",
            mixinStandardHelpOptions = true)
    static class Server implements Runnable {

        @Override
        public void run() {
            System.out.println("Launching in server mode...");
        }
    }
}