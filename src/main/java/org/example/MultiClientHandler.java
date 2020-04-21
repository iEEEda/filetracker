package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class MultiClientHandler implements Runnable {
    private Socket client;
    private BufferedReader in;
    private OutputStream out;
    private boolean check = false;
    private boolean fileCheck = false;
    private Hashtable<String, List<String>> ht;
    private List<String> bye1 = new LinkedList<>();
    private List<List<String>> bye2 = new LinkedList<>();

    public MultiClientHandler(Socket client, Hashtable<String, List<String>> ht) throws IOException {
        this.client = client;
        this.ht = ht;
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = client.getOutputStream();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String request = in.readLine();
                if (request != null) {
                    System.out.println(request);
                    if (!check) {
                        if (request.contains("HELLO")) {
                            System.out.println("HELLO received");
                            out.write("HI\n".getBytes(StandardCharsets.UTF_8));
                            out.flush();
                            check = true;
                            System.out
                                    .println("[SERVER][MultiClientHandler] Connection verified...");
                        } else {
                            System.out
                                    .println("[SERVER][MultiClientHandler] Connection dismissed...");
                            client.close();
                            break;
                        }
                    } else if (!fileCheck) {
                        if (request.contains("<")) {
                            System.out.println("File is received");
//                        <file name, file type, file size, file last modified date (DD/MM/YY), IP address, port number>
//                        <yernur, txt, 45kb, 11/22/22, 123.123.123.1, 9999>
//                        yernur txt 45kb 11/22/22 123.123.123.1 9999
                            String[] arr = request.replaceAll("[^a-zA-Z0-9./ ]", "").split(" ");
                            if (arr.length % 6 != 0) {
                                System.out.println(
                                        "[SERVER][MultiClientHandler] file submission is not proper!");
                                client.close();
                                break;
                            }
                            System.out.println(
                                    "[SERVER][MultiClientHandler] proper file submission! adding them...");
                            LinkedList<String> list = new LinkedList<>(Arrays.asList(arr));
                            System.out.println(list.toString());
                            for (int i = 0; i < list.size(); i += 7) {
                                ht.put(list.get(i), list.subList(i + 1, i + 6));
                                bye1.add(list.get(i));
                                bye2.add(list.subList(i, i + 5));
                            }
                            System.out.println("THE HASHTABLE: " + ht.toString());
                            fileCheck = true;
                            continue;
                        }

                    } else if (request.contains("BYE")) {
                        out.write("BYE BYE\n".getBytes(StandardCharsets.UTF_8));
                        out.flush();
                        int j = bye1.size();
                        for (int i = 0; i < j; i++) {
                            ht.remove(bye1.get(i), bye2.get(i));
                        }
                        System.out.println("[SERVER][MultiClientHandler] Connection finished...");
                        client.close();
                        break;
                    } else if (request.contains("SEARCH: ")) {
                        String[] arr = request.split("SEARCH: ");
                        if(arr.length==1){
                            System.out
                                    .println("[SERVER] File that you are looking for is not found...");
                            out.write("NOT FOUND\n".getBytes(StandardCharsets.UTF_8));
                            out.flush();
                        }
                        if (ht.containsKey(arr[1])) {
                            List<String> list = ht.get(arr[1]);
                            System.out.println("[SERVER] File that you are looking for is found...");
                            String temp = "<"+list.toString().substring(1,list.toString().length()-1)+">";
                            System.out.println("[SERVER]"+temp+" LIST SENT");
                            String sendIt = "FOUND: " + temp +"\n";
                            out.write(sendIt.getBytes(StandardCharsets.UTF_8));

                            out.flush();
                        } else {
                            System.out
                                    .println("[SERVER] File that you are looking for is not found...");
                            out.write("NOT FOUND\n".getBytes(StandardCharsets.UTF_8));
                            out.flush();
                        }
                    }
                }
            }

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