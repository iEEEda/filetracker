//Aida Eduard, Yenur Sabyrzhanov
package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class FileHandler implements Runnable {
    private Socket client;
    private BufferedReader in;
    private OutputStream out;
    private File directory;

    public FileHandler(Socket client, File directory) throws IOException {
        this.client = client;
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = client.getOutputStream();
        this.directory = directory;
    }

    @Override
    public void run() {
        try {
                String line = in.readLine();
                if (line.startsWith("DOWNLOAD: ")) {
                    System.out.println("Client wants to download a file");
                    out.write("FILE: \n".getBytes(StandardCharsets.UTF_8));
                    String[] fileInfo = line.substring(10).split(", ");
//            byte[] filebytes;
                    File file = new File(directory.getPath() + "\\" + fileInfo[0] + "." + fileInfo[1]);
                    DataOutputStream dos = new DataOutputStream(out);
                    FileInputStream fis = new FileInputStream(file);
                    byte[] buf = new byte[4096];
                    if (file.exists()) {
                        System.out.println("File exists");
                        int read;
                        System.out.println(file.toPath());
//              filebytes = Files.readAllBytes(file.toPath());
//              out.write(filebytes, 0, filebytes.length);
//              out.flush();
                        while ((read = fis.read(buf)) > 0) {
                            System.out.println(read);
                            dos.write(buf, 0, read);
                        }
                        System.out.println("Done.");
                    }
                    fis.close();
                    dos.close();
                }


                client.close();


        } catch (IOException e) {
            System.err.println("[MultiClientHandler] IOException in MultiClientHandler!");
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}