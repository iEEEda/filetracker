//Aida Eduard, Yenur Sabyrzhanov
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
    private int fileCheck = 0;
    private Hashtable<String, List<String>> ht;
    private List<String> bye1 = new LinkedList<>();
    private List<String> bye2 = new LinkedList<>();

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
                    if (!check) {
                        if (request.contains("HELLO")) {
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
                    } else if (fileCheck!=5) {
                        if (request.startsWith("<")) {
                            fileCheck++;
                            String[] arr = request.replaceAll("[^a-zA-Z0-9./, ]", "").split(",");
                            if (arr.length % 6 != 0) {
                                System.out.println(
                                        "[SERVER][MultiClientHandler] submission was incorrect... TRY AGAIN!");

                                client.close();
                                continue;
                            }
                            for (int i = 1; i < arr.length; i++) {
                                arr[i] = arr[i].replaceAll(" ", "");
                            }
                            System.out.println(
                                    "[SERVER][MultiClientHandler] proper file submission! adding them...");
                            LinkedList<String> list = new LinkedList<>(Arrays.asList(arr));
                            String value = "<"+list.subList(1,6).toString().substring(1,list.subList(1,6).toString().length()- 1)+">";
                            if(ht.containsKey(list.get(0))){
                                if(ht.get(list.get(0)).contains(value)){
                                    System.out.println("[SERVER][MultiClientHandler] duplicate file submission! ignoring it...");
                                    continue;
                                }
                                List<String> update = ht.get(list.get(0));
                                update.add(value);
                                ht.put(list.get(0),update);
                                bye1.add(list.get(0));
                                bye2.add(value);
                            }else{
                                List<String> linkedList = new LinkedList<>();
                                linkedList.add(value);
                                ht.put(list.get(0), linkedList);
                                bye1.add(list.get(0));
                                bye2.add(value);
                            }
                            continue;
                        } else if (fileCheck==0 && !request.contains("<")){
                            System.out.println(
                                    "[SERVER][MultiClientHandler] No uploaded files...");
                            client.close();
                            System.out
                                    .println("[SERVER][MultiClientHandler] Connection dismissed...");
                            break;
                        }

                    }
                    if (request.startsWith("BYE")) {
                        int j = bye1.size();
                        System.out.println(ht.toString());
                        System.out.println(j);
                        for (int i = 0; i < j; i++) {
                            List<String> delete = ht.get(bye1.get(i));
                            delete.remove(bye2.get(i));
                            if (!delete.isEmpty()) {
                                ht.put(bye1.get(i), delete);
                            } else {
                                ht.remove(bye1.get(i));
                            }
                        }
                        System.out.println("[SERVER][MultiClientHandler] Connection finished...");
                        client.close();
                        break;
                    }

                    if (request.contains("SEARCH: ")) {
                        String[] arr = request.split("SEARCH: ");
                        if(arr.length==0){
                            System.out
                                    .println("[SERVER] File that you are looking for is not found...");
                            out.write("NOT FOUND\n".getBytes(StandardCharsets.UTF_8));
                            out.flush();
                            continue;
                        }
                        if (ht.containsKey(arr[1])) {
                            List<String> list = ht.get(arr[1]);
                            System.out.println("[SERVER] File that you are looking for is found...");
                            System.out.println("[SERVER]"+list.toString().substring(1,list.toString().length()-1)+" LIST SENT");
                            String sendIt = "FOUND: " + list.toString().substring(1,list.toString().length()-1)+"\n";
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