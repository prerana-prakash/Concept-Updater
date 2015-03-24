package com.pp.concept.updater.client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.commons.io.IOUtils;

import com.pp.concept.updater.server.update.check.UpdateServer;

/**
 * This is the class that constantly listens to the {@link UpdateServer}.
 * @author Prerana Prakash
 */
public class ClientReadingServerSocket extends Thread {
    private Socket server = null;
    // hard coded to 1 in the beginning. Will be changed every time the server updates
    private Integer currentVersion = 1;
    private String downloadUrl;
    private Integer portForDownload;
    private String addressForDownload;
    private LogManager LOG_MANAGER;
    static Logger logger;

    /**
     * @param clientSocket socket between {@link CheckForUpdateClient} and {@link UpdateServer}
     */
    public ClientReadingServerSocket(final Socket clientSocket) {
        this.server = clientSocket;
        setup("ClientReadingServerSocket"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            final HashMap<Socket, Object[]> streams = CheckForUpdateClient.getClientMap();
            String incomingData;
            final Object[] stream = streams.get(server);
            final ObjectInputStream inputStream = (ObjectInputStream) stream[1];
            Integer serverVersion;

            while (true) {

                incomingData = (String) inputStream.readObject();
                logger.log(Level.INFO, "received this data from server: " + incomingData); //$NON-NLS-1$

                serverVersion = Integer.parseInt(incomingData);
                // server version 0 signifies that there is not current update available
                while (serverVersion == 0) {
                    Thread.sleep(3000);
                    logger.log(Level.INFO, "Sent Version no: " + currentVersion); //$NON-NLS-1$
                    sendData(server, currentVersion.toString());

                    incomingData = (String) inputStream.readObject();
                    logger.log(Level.INFO, "received version number: " + incomingData); //$NON-NLS-1$
                    serverVersion = Integer.parseInt(incomingData);
                }

                // getting the updated version (should be >0) from the server
                logger.log(Level.INFO, "server version is > client version so need to update"); //$NON-NLS-1$
                incomingData = (String) inputStream.readObject();

                // getting the download URL (non null)
                downloadUrl = incomingData.toString();
                logger.log(Level.INFO, "URL read is " + downloadUrl); //$NON-NLS-1$

                // create a back up of the file
                final boolean isBackupSuccess = createBackup();
                if (isBackupSuccess) {
                    downloadFile();
                    logger.log(Level.INFO, "File downloaded successfully :) "); //$NON-NLS-1$

                    // updating the current version of the client to the updated version from the server
                    currentVersion = serverVersion;
                    logger.log(Level.INFO, "client version updated to : " + currentVersion); //$NON-NLS-1$
                }

                // continue sending current version of the client
                sendData(server, currentVersion.toString());
                logger.log(Level.INFO, "Sent Version no: " + currentVersion + " to Server"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } catch (final SocketException e) {
            logger.log(Level.SEVERE, "Server failed " + server); //$NON-NLS-1$
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "I/O exception while establishing connection to server" + e); //$NON-NLS-1$
        } catch (final ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Class not found" + e); //$NON-NLS-1$
        } catch (final InterruptedException e) {
            logger.log(Level.SEVERE, "Interrupted ex" + e); //$NON-NLS-1$
        }

    }

    private boolean createBackup() {
        logger.log(Level.INFO, "Backing up Client information"); //$NON-NLS-1$
        //final String clientFileName = this.getClass().getClassLoader().getResource("indexClient.xml").toString(); //$NON-NLS-1$
        //final File file = new File(clientFileName);
        //return file.renameTo(new File(clientFileName + ".bak")); //$NON-NLS-1$
        
        final InputStream inputFile = ClientReadingServerSocket.class.getResourceAsStream("/".concat("indexClient.xml"));
        String contents = new String(IOUtils.toByteArray(inputFile));
        inputFile.close();
        return contents;
        
    }

    private void downloadFile() {
        int current = 0;
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        Socket clientSocketDownload = null;
        int bytesRead;

        try {
            // get the URL and the filename and the port
            // getURL, port and filename
            final HashMap<Socket, Object[]> clientMapDownload = new HashMap<Socket, Object[]>();

            // creating a new connection to {@link DownloadServer}
            clientSocketDownload = new Socket("127.0.0.1", 5600); //$NON-NLS-1$
            logger.log(Level.INFO, "Connected to DownloadServer"); //$NON-NLS-1$

            // set up download streams.
            final Object streamsForDownload[] = new Object[2];
            streamsForDownload[0] = new ObjectOutputStream(clientSocketDownload.getOutputStream());
            streamsForDownload[1] = new ObjectInputStream(clientSocketDownload.getInputStream());
            clientMapDownload.put(clientSocketDownload, streamsForDownload);

            // receive file from {@link DownloadServer}
            final byte[] bytearray = new byte[10000];
            final InputStream inputStream = clientSocketDownload.getInputStream();
            final String clientFileName = this.getClass().getClassLoader().getResource("indexClient.xml").toString(); //$NON-NLS-1$
            fileOutputStream = new FileOutputStream(clientFileName);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bytesRead = inputStream.read(bytearray, 0, bytearray.length);
            current = bytesRead;

            do {
                bytesRead = inputStream.read(bytearray, current, (bytearray.length - current));
                if (bytesRead >= 0) {
                    current += bytesRead;
                }
            } while (bytesRead > -1);

            bufferedOutputStream.write(bytearray, 0, current);
            bufferedOutputStream.flush();
            logger.log(Level.INFO, "File " + " downloaded (" + current + " bytes read)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            fileOutputStream.close();
            bufferedOutputStream.close();
            clientSocketDownload.close();
        } catch (final UnknownHostException e) {
            logger.log(Level.SEVERE, "UnknownHostException " + e); //$NON-NLS-1$
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "IOException" + e); //$NON-NLS-1$
        }
    }

    private void sendData(final Socket clientSocket, final String message) {
        final Object[] toWrite = CheckForUpdateClient.getClientMap().get(clientSocket);
        final ObjectOutputStream out = (ObjectOutputStream) toWrite[0];
        try {
            out.writeObject(message);
            out.flush();
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "IOException" + e); //$NON-NLS-1$
        }
    }

    /**
     * Extracts the ip address and the port from the download URL provided by the {@link UpdateServer}
     * "http://localhost:5600/indexServer.xml"
     * @param downloadUrlUpdate URL sent from {@link UpdateServer}
     */
    private void setContentsFromURL(final String downloadUrlUpdate) {
        final String[] splitURLbyColon = downloadUrlUpdate.split(":"); //$NON-NLS-1$
        // get the ip address
        final String[] splitURLbySlash = splitURLbyColon[1].split("//"); //$NON-NLS-1$
        addressForDownload = splitURLbySlash[1];

        final String[] splitbySlash = splitURLbyColon[2].split("/"); //$NON-NLS-1$
        portForDownload = Integer.parseInt(splitbySlash[0]);

    }

    private void setup(final String className) {
        LOG_MANAGER = LogManager.getLogManager();
        FileHandler fh;
        // delete the file if it exists
        try {
            fh = new FileHandler("Client_Log.txt"); //$NON-NLS-1$
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