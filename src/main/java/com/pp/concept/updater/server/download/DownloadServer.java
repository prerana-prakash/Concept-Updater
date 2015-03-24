package com.pp.concept.updater.server.download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;

import com.pp.concept.updater.client.CheckForUpdateClient;
import com.pp.concept.updater.server.update.check.UpdateServer;

/**
 * This Server is the location which hosts the file which needs to be downloaded in order to perform the update. The URL
 * of the file hosted on this server is send by {@link UpdateServer} to the {@link CheckForUpdateClient} if there is a
 * new version available
 * @author Prerana Prakash
 */
public class DownloadServer {
    /**
     * The ServerMap. Map of server's socket along with its input and output streams. Creating a map enables better
     * binding of the input and output streams.
     */
    static HashMap<Socket, Object[]> serverMap = new HashMap<Socket, Object[]>();
    private final LogManager LOG_MANAGER = LogManager.getLogManager();

    /**
     * Download Server implementation
     * @param args - system args
     */
    public static void main(final String args[]) {
        final DownloadServer downloadServerObj = new DownloadServer();
        downloadServerObj.connect();
    }

    /**
     * This method is where the DownloadServer creates a socket and starts listening for the
     * {@link CheckForUpdateClient}'s request to download the updated file
     */
    public void connect() {
        final Object streams[] = new Object[2];
        Socket sock = null;
        try {
            // the file to be downloaded as part of the update
            final String downloadFileURL = this.getClass().getClassLoader().getResource("indexServer.xml").toString(); //$NON-NLS-1$

            // Create a server socket at port 5600
            final ServerSocket serverSock = new ServerSocket(5600);
            System.out.println("download server started"); //$NON-NLS-1$

            // Server goes into a permanent loop accepting connections from clients
            while (true) {
                // Listens for a connection to be made to this socket and accepts it
                sock = serverSock.accept();
                System.out.println("Client accepted " + sock); //$NON-NLS-1$

                // establish input and output streams for the socket.
                streams[1] = new ObjectInputStream(sock.getInputStream());
                streams[0] = new ObjectOutputStream(sock.getOutputStream());
                serverMap.put(sock, streams);

                // sends the file to the client
                sendFile(sock, downloadFileURL);
                sock.close();
            }
        } catch (final IOException e) {
            LOG_MANAGER.getLogger(DownloadServer.class.getName()).log(Level.SEVERE,
                    "I/O exception while starting download server", e);
        }
    }

    /**
     * Method to send the file to the client as part of the update
     * @param client - socket to send
     * @param filename - file to send
     */
    public void sendFile(final Socket client, final String filename) {
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        OutputStream outputStream = null;
        try {
            final File myFile = new File(filename);
            final byte[] mybytearray = new byte[(int) myFile.length()];
            fileInputStream = new FileInputStream(myFile);

            bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.read(mybytearray, 0, mybytearray.length);

            outputStream = client.getOutputStream();
            System.out.println("Sending " + filename + "(" + mybytearray.length + " bytes)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            outputStream.write(mybytearray, 0, mybytearray.length);
            outputStream.flush();
            System.out.println("Done."); //$NON-NLS-1$

            bufferedInputStream.close();
            outputStream.close();
        } catch (final IOException e) {
            LOG_MANAGER.getLogger(DownloadServer.class.getName()).log(Level.SEVERE,
                    "I/O exception while sending update to client", e);
        }
    }

    /**
     * @return server's map of streams
     */
    public static HashMap<Socket, Object[]> getServerMap() {
        return serverMap;
    }

    /**
     * @param serverMapOfStreams set server's map of streams
     */
    public static void setServerMap(final HashMap<Socket, Object[]> serverMapOfStreams) {
        serverMap = serverMapOfStreams;
    }
}