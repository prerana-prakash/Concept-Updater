package com.pp.concept.updater.server.update.check;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;

import com.pp.concept.updater.client.ClientReadingServerSocket;

/**
 * This is the class that constantly listens to the {@link ClientReadingServerSocket}.
 * @author Prerana Prakash
 */
public class ServerReadingClientSocket extends Thread {

    private Socket client = null;
    // Hard coded to two. This is the new version available from server for update
    private final Integer SERVER_VERSION = 2;
    private URL downloadUrl;
    private final LogManager LOG_MANAGER = LogManager.getLogManager();

    /**
     * @param clientSocket - is the socket between {@link ClientReadingServerSocket} and {@link UpdateServer}
     */
    public ServerReadingClientSocket(final Socket clientSocket) {
        this.client = clientSocket;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            // location of the update file hosted on the {@link DownloadServer}
            downloadUrl = new URL("http://localhost:5600/indexServer.xml"); //$NON-NLS-1$
            String incomingData;
            final HashMap<Socket, Object[]> streamMap = UpdateServer.getServerMap();

            // the stream to read from
            final Object[] toRead = streamMap.get(client);
            final ObjectInputStream in = (ObjectInputStream) toRead[1];

            while (true) {

                incomingData = (String) in.readObject();
                System.out.println("Received message from Client: " + incomingData); //$NON-NLS-1$
                final int clientVersion = Integer.parseInt(incomingData);

                if (clientVersion < SERVER_VERSION) {
                    SendData(client, SERVER_VERSION.toString());
                    SendData(client, downloadUrl.toString());
                } else {
                    SendData(client, new Integer(0).toString());
                }
            }
        } catch (final SocketException e) {
            LOG_MANAGER.getLogger(ServerReadingClientSocket.class.getName()).log(Level.SEVERE,
                    "I/O exception while opening socket server", e);
        } catch (final IOException e) {
            LOG_MANAGER.getLogger(ServerReadingClientSocket.class.getName()).log(Level.SEVERE,
                    "I/O exception while starting server", e);
        } catch (final ClassNotFoundException e) {
            LOG_MANAGER.getLogger(ServerReadingClientSocket.class.getName()).log(Level.SEVERE, "clann not found", e);
        }

    }

    private void SendData(final Socket clientSocket, final String message) {
        final Object[] toWrite = UpdateServer.getServerMap().get(clientSocket);
        final ObjectOutputStream out = (ObjectOutputStream) toWrite[0];
        try {
            out.writeObject(message);
            out.flush();
        } catch (final IOException e) {
            LOG_MANAGER.getLogger(ServerReadingClientSocket.class.getName()).log(Level.SEVERE,
                    "I/O exception while sending data to client", e);
        }
    }
}
