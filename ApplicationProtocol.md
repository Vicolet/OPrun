# OPrun application protocol

## Section 1 - Overview

Oprun is a little multiplayer game written in java where you compete against others 
to get the best results in a mental math challenge.

The OPrun protocol is the communication protocol used between the client instances and the server 
program.

It defines how one could implement their own client in their preffered langage to play
OPrun on the official servers.

## Section 2 - Transport protocol
The OPrun protocol is a text-only protocol. It uses both UDP for broadcasting messages to 
all players and TCP for communicating with each specific client. The port to be used for 
both protocols is 42069

Every message must be encoded in UTF-8 and delimited by a newline character
(`\n`). The messages are treated as text messages.

### UDP

The messages sent in UDP are only messages sent by the server meant for all users, or for
clients seeking to initiate a connexion.

The server sends a status broadcast every 2 seconds.

At the start of a game, the server sends a special broadcast.

At the end of a game, the server sends a special broadcast and the leaderboards.

### TCP

The client should listen for UDP game status messages, then initiate a TCP connection if 
the server status is "waiting for new players".  
The server will then respond with a good code and attribute a random nickname to the player.

If the server is not in a waiting state, the client's connexion is closed with a bad code.

If the next game already has 20 players registered, the client's connexion is closed with a bad code.

When the server starts the game, it sends a message the first mental math operation to all the connected
clients and handles every client in its own thread.

The client then responds with the operation answer, the answer must be numerical (signed) and not exceed 10 characters

The server checks whether the answer is correct and sends a code according to it.
- if the answer is correct, the server gives a good code, then sends a new message with the next operation.
- if the answer is incorrect, the server gives a bad code then waits for the next answer.

The game loop goes on in every TCP connection until the game timer ends. Then all the TCP connexions are closed
and the rest of the communication is done unilaterally by the server in UDP.

when a client has disconnected, the server must close the connection and remove
the client from the list of connected clients.

## Section 3 - Messages

### Server status broadcast

The server broadcasts a status message

#### Protocol
UDP

#### Message
```
STATUS <status>
```

#### Parameters
`<status>` can be:
- `WAITING FOR PLAYERS`: Waiting for players for next round

#### Response
No response expected.

### Join the next game round

The client requests to join the next game round.

#### Protocol
TCP

#### Message
No message is sent. The client simply opens a connexion in TCP that should always be accepted by the server.  
The client expects a response.

#### Response

- `NICKNAME <nickname>`: the client has been granted access to the next round, the client's nickname for this round is `<nickname>`
- `ERROR <code>`: an error occurred during the join. The error code is an
  integer between 1 and 2 inclusive. The error codes are as follow:
  - 1: the server is not in a waiting state
  - 2: the next round is full
  The TCP connexion is closed.

### Round start broadcast

the server broadcasts a message to start a round

#### Protocol
UDP

#### Message
```
ROUND START
```
#### Response

No response expected.

### Calculation dialog

The server sends a calculation to a client and expects their answer. This is the round's game loop.

#### Protocol
TCP

#### 1. Message from the server
```
CALCULATION <calculation>
```

#### 2. Answer from the client
```
ANSWER <answer>
```

#### 3. Validation from the server
```
CORRECT
```
or
```
INCORRECT
```

#### Loop
After that, if the answer was correct, the server adds points to the client and sends them a new calculation (loop to 1.)  
if the answer was incorrect, the server waits for another `ANSWER` message from the client. (loop to 2.)

### Game End

To end the round, the servers simply closes the TCP connection with every client. 

The server doesn't expects any responses after that.

#### Protocol
TCP

### Leaderboard broadcast

The server broadcasts the leaderboards of the finished round.

#### Protocol
UDP

#### Message
```
LEADERBOARD <nickname 1> <number of points of player 1> <nickname 2> <number of points of player 2> ...
```
The server should sort the nickname by decroissant number of points, so that the first player in the leaderboard
wins the round.

#### Response
No response expected.

## Section 4 - Examples

### Functional example

![Functional example](./images/example-1-functional-example.png)

### Join the server with a duplicate client name

![CJoin the server with a duplicate client name example](./images/example-2-join-the-server-with-a-duplicate-client-name.png)

### Send a message to an unconnected recipient

![Send a message to an unconnected recipient example](./images/example-3-send-a-message-to-an-unconnected-recipient.png)

### Send a message that is too long

![Send a message that is too long example](./images/example-4-send-a-message-that-is-too-long.png)
