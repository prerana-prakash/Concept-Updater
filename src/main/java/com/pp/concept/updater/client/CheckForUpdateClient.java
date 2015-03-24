package com.pp.concept.updater.client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.pp.concept.updater.server.update.check.UpdateServer;

/**
 * This class serves as the application client which sends messages to the {@link UpdateServer} to check if there is an
 * available newer version.
 * @author Prerana Prakash
 */
public class CheckForUpdateClient {
    /**
     * The ClientMap. Map of Client's socket along with its input and output streams. Creating a map enables better
     * binding of the input and output streams.
     */
    static HashMap<Socket, Object[]> clientMap = new HashMap<Socket, Object[]>();
    private static LogManager LOG_MANAGER;
    static Logger logger;

    /**
     * Client implementation starts here
     * @param args command line args
     * @throws IOException
     * @throws SecurityException
     */
    public static void main(final String args[]) {
        setup("CheckForUpdateClient");
        final CheckForUpdateClient applicationClientObj = new CheckForUpdateClient();
        applicationClientObj.connect();
    }

    /**
     * This method is used for establishing a connection with the {@link UpdateServer} and sending its current version
     * to check for updates
     */
    public void connect() {
        final Object clientStreams[] = new Object[2];
        try {
            // Create a client socket and connect to server at 127.0.0.1 port 5000
            final Socket clientSocket = new Socket("127.0.0.1", 5000); //$NON-NLS-1$
            logger.log(Level.INFO, "Client connected to server.");
            // establish input and output streams for the socket.
            clientStreams[0] = new ObjectOutputStream(clientSocket.getOutputStream());
            clientStreams[1] = new ObjectInputStream(clientSocket.getInputStream());
            clientMap.put(clientSocket, clientStreams);

            // sending current version to the server.
            logger.log(Level.INFO, "Sent Version no: 1 to Server");
            sendData(clientSocket, "1"); //$NON-NLS-1$

            // Creating another thread to listen to messages from server. This is used for further communication.
            final ClientReadingServerSocket readThreadServerSocket = new ClientReadingServerSocket(clientSocket);
            readThreadServerSocket.start();

        } catch (final IOException ex) {
            logger.log(Level.SEVERE, "I/O exception while establishing connection to server");
        }
    }

    /**
     * @return ClientMap: client's input stream and output stream map
     */
    public static HashMap<Socket, Object[]> getClientMap() {
        return clientMap;
    }

    /**
     * @param clientMapStreams - client's input stream and output stream maps
     */
    public static void setClientMap(final HashMap<Socket, Object[]> clientMapStreams) {
        clientMap = clientMapStreams;
    }

    private void sendData(final Socket clientSocket, final String message) {
        final Object[] toWrite = getClientMap().get(clientSocket);
        final ObjectOutputStream out = (ObjectOutputStream) toWrite[0];
        try {
            out.writeObject(message);
            out.flush();
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "I/O exception while establishing connection to server");
        }
    }

    private static void setup(final String className) {
        LOG_MANAGER = LogManager.getLogManager();
        FileHandler fh;
        // delete the file if it exists
        try {
            final File file = new File("Client_Log.txt");
            if (file.exists()) {
                file.delete();
            }
            fh = new FileHandler("Client_Log.txt");
            logger = Logger.getLogger(CheckForUpdateClient.class.getSimpleName());
            LOG_MANAGER.addLogger(logger);
            logger.setLevel(Level.INFO);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
        } catch (final SecurityException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }
}