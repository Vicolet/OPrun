# OPrun

## Introduction
OPrun is a localhost mutiplayer calculation solver game in the console. the goal is to solve as many calculations as possible within a given time to earn points.
The player with most points at the end wins
There can be a total of 20 players connected at the same time.

## How to play
### Game flow
In order to connect to the server, the clients must be launched before the server. If the client is launched after the server then it must wait for the next round to join the game.
The server lets a bit of time for the clients to connect and then starts the game. The clients receive a calculation and must solve it as fast as possible to earn a point. A bad answer doesn't have any effect, you just have to try again. After a given time, the server stops the game and the leaderboard is displayed for each client. The player with the most points at the end of the round wins. \
After the leaderboard are displayed, the server listens for connections again and accepts all the new players for a total of 20 players connected to the same game.

### Prerequisites
#### Java Development Kit (JDK) 11 or higher
Download and install at this address: \
https://www.oracle.com/ch-de/java/technologies/javase/jdk11-archive-downloads.html \
\
Verify installation:
```bash 
java -version
```

#### Docker
Download and install at this address: \
https://docs.docker.com/engine/ \
\
Verify installation:
```bash
docker --version
```

### Set up build
#### Docker network
Use this command to create a network so the containers can communicate with each others:
```bash
docker network create \
  --driver bridge \
  --subnet=127.0.0.0/24 \
  game-network
```
#### Docker image
With the existing Dockerfile in the project build the image with the following command:
```bash
docker build -t oprun -f Dockerfile .
```

### Set up run
Use the following command to first run the client:\
Note: be sure to change the interface to suit your configuration. You can check your working interfaces with following command:

Windows:
```bash
netsh interface show interface
```
And then run the client with the correct interface:\
Note: if you use multiple clients, make sure to also change the --name for each client.

### Build and run with maven
#### Build
Windows:
```bash
./mvnw.cmd package clean
```

Unix:
```bash
./mvnw package clean
```

#### Run

Client:
```bash
java -jar oprun.jar client --ip <server ip> --interface <name of network interface to use>
```

Server:
```bash
java -jar oprun.jar server
```

### Run with docker
Pull the container:
```bash
docker pull ghcr.io/nyaaw/oprun
```

while it's downloading, you can setup the docker network to make the two instances communicate:

```bash
docker network create --opt com.docker.network.bridge.name=oprunif oprunnet
```

When the image has been downloaded, run with:

```bash
docker run --rm -it \
  --network oprunnet \
  --name client \
  ghcr.io/nyaaw/oprun \
  client --ip server --interface oprunif
```
Once all the needed clients have been launched, you can then run the server:
```bash
docker run --rm -it \
  --network oprunnet \
  --name server \
  ghcr.io/nyaaw/oprun \
  server
```
The game will launch and the clients will receive their first calculation.

### Example
After launching a client:
```code
Launching in client mode...
Waiting for server broadcasts
```
And then the server:
```code
Broadcast sent
Server listening on port 42069
[Serveur] Nouveau client connecté depuis 127.0.0.1:44018
```
The client will display:
```code
Launching in client mode...
Waiting for server broadcasts
Connecting to server via TCP...
joined game as xX_EuclidSn1p3r_Xx
game started! Solve the calculations.
```
After a minute, the server launches the game and client will display its first calculation:
```code
Solve: 16+13*5-27
Your answer:
```
And finally after the given time the timer is stopped and the leaderboard is displayed:
```code
Leaderboard: <xX_EuclidSn1p3r_Xx><2>
```
All the clients disconnect, and wait for the server to start a new round. This allows new players to join the game.

## Contributing
### Prerequisites
Installation of JDK 11 or higher and Docker mentioned above.
#### Git
git is required to contribute to the project.\
Clone the repository: 
```bash
git clone https://github.com/Vicolet/OPrun
```

#### Maven
if not included in your IDE, download and install at this address: \
https://maven.apache.org/download.cgi \
Verify installation:
```bash
mvn -version
```

### Workflow
This section explains about the different classes in the project and their functions in order to understand it better.

#### Client.java
The Client class is simply structured: 
- First create a new instance of Client()
- StartClient() is the entry point where the client is connected to the multicast group
- RunRound() is the function called by a background thread that discusses with the server in TCP
- receiveUdpMessage() is a util function

#### Server.java
The Server class is structured differently with the main class and its subclass:
- First create a new instance of Server():
- startServer() is the entry point where different methods are called:
  - sendBroadcast() that sends only one broadcast on the subnetwork of docker
  - acceptClients() that waits for the clients to connect
    - run() in ClientHandler that manages the discussion with the client
  - startGame() that manages the time taken in the round

The remaining functions are util functions.

#### Operation.java
This class creates random operations and return them as a string. We've set these rules to make the game funnier and not to difficult:
- MAX_NUMBER = 10 defines the maximum of operations the class can handle. You can set this between 2 and 10 in the constructor.
- MAX_NUMBER_BOUND = 50 defines the maximum that a number can reach. High numbers are difficult to calculate and the game loses it's fun if this constant is too high
- MAX_MULT_COEFF = 15 defines the maximum a number that multiplies another can get to. For example 15 * 50 would be a limit with our defined rules
- We've created this class so it as has maximum of one multiplication in a calculation to avoid high complexity, but you can change it in the getRandomOperators function







