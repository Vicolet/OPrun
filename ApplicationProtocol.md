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

### Join the server

The client sends a join message to the server indicating the client's username.

#### Request

```text
JOIN <name>
```

- `name`: the name of the client

#### Response

- `OK`: the client has been granted access to the server
- `ERROR <code>`: an error occurred during the join. The error code is an
  integer between 1 and 1 inclusive. The error codes are as follow:
  - 1: the client's name is already in use

### List connected clients

The client sends a message to the server to request the list of connected
clients.

#### Request

```text
LIST
```

#### Response

- `CLIENTS <client1> <client2> <client3> ...`: the list of connected clients.
  The clients are separated by a space.

### Send a message

The client sends a message to the server indicating the recipient of the
message. The server is then responsible for sending the message to the
recipient.

#### Request

```text
SEND <recipient> <message>
```

#### Response

- `OK`: the message has been successfully sent
- `ERROR <code>`: an error occurred while sending the message. The error code is
  an integer between 1 and 2 inclusive. The error codes are as follow:
  - 1: the recipient is not connected
  - 2: the message exceeds 100 characters

### Receive a message

The server sends a message to the recipient indicating the sender of the
message. The client is then responsible for displaying the received message.

#### Request

```text
RECEIVE <message> <sender>
```

- `message`: the received message
- `sender`: the name of the message sender

#### Response

None.

## Section 4 - Examples

### Functional example

![Functional example](./images/example-1-functional-example.png)

### Join the server with a duplicate client name

![CJoin the server with a duplicate client name example](./images/example-2-join-the-server-with-a-duplicate-client-name.png)

### Send a message to an unconnected recipient

![Send a message to an unconnected recipient example](./images/example-3-send-a-message-to-an-unconnected-recipient.png)

### Send a message that is too long

![Send a message that is too long example](./images/example-4-send-a-message-that-is-too-long.png)
