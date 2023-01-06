/**
 * This file simulates the reception of messages from the roborio. At the moment it sends a series of messages extremely fast to make sure that the web interface is able to cope.
 */

const fs = require('fs');
const websocket = require('ws');
const _ = require('lodash');

/**
 * Create a new websocket server instance.
 */
const server = new websocket.Server({
  port: 5803,
});

console.log(`Websocket server running on port ${server.port}`);

/**
 * List of currently active sockets to send messages to.
 */
let sockets = [];

server.on('connection', function (socket) {
  console.log('new connection');
  sockets.push(socket);
  try {
    // Read contents of the file.
    const data = fs.readFileSync('./test/logFile.txt', 'utf-8');

    // Split the contents by new line.
    const lines = data.split(/\r?\n/);

    // Print all lines
    lines.slice(0, 50).forEach((line) => {
      socket.send(line);
    });
  } catch (err) {
    console.error(err);
  }
});
// When a socket closes, or disconnects, remove it from the array.
server.on('close', function (socket) {
  sockets = sockets.filter((s) => s !== socket);
});
