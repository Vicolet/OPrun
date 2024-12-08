# OPrun

## Introduction
OPrun is a localhost mutiplayer calculation solver game in the console. the goal is to solve as many calculations as possible within a given time to earn points.
The player with most points at the end wins
There can be a total of 20 players connected at the same time.

## How to play
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

#### Docker network

### Set up

### Game flow
In order to connect to the server, the clients must be launched before the server. If the client is launched after the server then it must wait for the next round to join the game.
The server lets a bit of time for the clients to connect and then starts the game. The clients receive a calculation and must solve it as fast as possible to earn a point. A bad answer doesn't have any effect, you just have to try again. After a given time, the server stops the game and the leaderboard is displayed for each client. The player with the most points at the end of the round wins. \
After the leaderboard are displayed, the server listens for connections again and accepts all the new players for a total of 20 players connected to the same game.

### Example



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

### Environment

### Build

### Run



