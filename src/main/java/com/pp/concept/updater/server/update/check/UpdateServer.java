package com.pp.concept.updater.server.update.check;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;

import com.pp.concept.updater.client.CheckForUpdateClient;

/**
 * This Server performs the check on the client version. If the client's version is up-to-date it sends it 0 else it
 * sends it the new version along with the URL of the update.
 * @author Prerana Prakash
 */
public class UpdateServer {
    /**
     * The ServerMap.Map of server's socket along with its input and output streams. Creating a map enables better
     * binding of the input and output streams.
     */
    static HashMap<Socket, Object[]> serverMap = new HashMap<Socket, Object[]>();
    private final LogManager LOG_MANAGER = LogManager.getLogManager();

    /**
     * Update Server implementation
     * @param args - system args
     */
    public static void main(final String args[]) {

        final UpdateServer updateServerObj = new UpdateServer();
        updateServerObj.connect();
    }

    /**
     * This method is where the server creates a socket and starts listening for the {@link CheckForUpdateClient} check
     * for updates
     */
    public void connect() {
        final Object streams[] = new Object[2];
        try {
            // Create a server socket at port 5000
            final ServerSocket serverSock = new ServerSocket(5000);
            System.out.println("server started"); //$NON-NLS-1$

            // Server goes into a permanent loop accepting connections from clients.
            // There may be more than one {@link CheckForUpdateClient}
            while (true) {

                // Listens for a connection to be made to this socket and accepts it
                final Socket sock = serverSock.accept();
                System.out.println("Client accepted " + sock); //$NON-NLS-1$

                // establish input and output streams for the socket.
                streams[1] = new ObjectInputStream(sock.getInputStream());
                streams[0] = new ObjectOutputStream(sock.getOutputStream());
                serverMap.put(sock, streams);

                // Creating another thread to listen to messages from {@link CheckForUpdateClient}. This is used for
                // further communication.
                final ServerReadingClientSocket readThreadClient = new ServerReadingClientSocket(sock);
                readThreadClient.start();
            }
        } catch (final IOException e) {
            LOG_MANAGER.getLogger(UpdateServer.class.getName()).log(Level.SEVERE,
                    "I/O exception while starting update server", e);
        }
    }

    /**
     * @return ServerMap - server's input stream and output stream map
     */
    public static HashMap<Socket, Object[]> getServerMap() {
        return serverMap;
    }

    /**
     * @param serverMapOfStreams - server's input stream and output stream map
     */
    public static void setServerMap(final HashMap<Socket, Object[]> serverMapOfStreams) {
        serverMap = serverMapOfStreams;
    }
}