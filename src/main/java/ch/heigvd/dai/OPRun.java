package ch.heigvd.dai;

import ch.heigvd.dai.client.Client;
import ch.heigvd.dai.server.Server;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "oprun",
        description = "Client - server game of quick maths !",
        version = "oprun 1.0",
        subcommands = {OPRun.ClientCommand.class, OPRun.ServerCommand.class},
        mixinStandardHelpOptions = true)
public class OPRun {

    /**
     * Entry point
     * @param args arguments to give to the program
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new OPRun()).execute(args);
        System.exit(exitCode);
    }


    @Command(name="client", description = "launches the game in client mode",
            mixinStandardHelpOptions = true)
    static class ClientCommand implements Runnable {

        @Option(names="--ip", description = "Ip of the server to connect to", required=true)
        String serverIp;

        @Option(names="--interface", description = "Name of the network interface to use", required=true)
        String networkInterface;

        @Override
        public void run() {
            System.out.println("Launching in client mode...");
            new Client(serverIp, networkInterface).run();
        }
    }

    @Command(name="server", description = "launches game server",
            mixinStandardHelpOptions = true)
    static class ServerCommand implements Runnable {

        @Override
        public void run() {
            new Server().startServer();
        }
    }
}