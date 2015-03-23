README

1. Unzip the contents
2. Import it in eclipse

3. change the contents in the IndexServer.xml
4. Also keep the indexClient.xml (don’t delete). Can change its contents as well. Make sure the two xml files have different data.

5. Navigate to UpdateServer.java inside:   src/com/dell/server/update/check. Run the UpdateServer.java as a java application
6. Navigate to DownloadServer.java inside: src/com/dell/server/download. Run the DownloadServer.java as a java application
7. Navigate to CheckForUpdateClient.java in src/com/dell/client. Run CheckForUpdateClient.java as a java application

8. Refresh the project
9. Client_Log.txt would show all the logs from the CheckForUpdateClient.java
10. Can also check the contents of IndexClient.xml after the download is performed to verify that two xml files match.

11. Need to kill the process to stop the servers and the clients. (This was done assuming the client and the servers never stop, the client continues to check for updates after a specified interval)