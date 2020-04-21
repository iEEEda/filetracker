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
    public MultiClientHandler(Socket client, Hashtable<String, List<String>> ht) throws IOException {
        this.client = client;
        this.ht = ht;
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = client.getOutputStream();
    }

    @Override
    public void run() {
        try{
            while(true){
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
                            for (int i = 0; i < list.size(); i += 6) {
                                ht.put(list.get(i), list.subList(i, i + 5));
                            }
                            System.out.println("THE HASHTABLE: " + ht.toString());
                            fileCheck = true;
                        }

                    } else if (request.contains("BYE")) {
                        out.write("BYE BYE\n".getBytes(StandardCharsets.UTF_8));
                        out.flush();
                        System.out.println("[SERVER][MultiClientHandler] Connection finished...");
                        client.close();
                        break;
                    } else if (request.contains("SEARCH: ")) {
                        String[] arr = request.split("SEARCH: ");
                        if (ht.containsKey(arr[1])) {
                            LinkedList<String> list = (LinkedList<String>) ht.get(arr[2]);
                            System.out
                                .println("[SERVER] File that you are looking for is found...");
                            String sendIt = "FOUND: " + list.toString();
                            out.write(sendIt.getBytes(StandardCharsets.UTF_8));
                            out.flush();
                        } else {
                            System.out
                                .println("[SERVER] File that you are looking for is not found...");
                            out.write("NOT FOUND".getBytes(StandardCharsets.UTF_8));
                            out.flush();
                        }
                    }
                }
            }

        }catch (IOException e) {
            System.err.println("[MultiClientHandler] IOException in MultiClientHandler!");
            e.printStackTrace();
        } finally{
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
//        while (true) {
//
//
//
//            if (!isHello()){
//                client.close();
//                out.close();
//                in.close();
//                continue;
//            }
//            listener.setSoTimeout(1000);
//
//            if(!isFile()){
//                client.close();
//                out.close();
//                in.close();
//                continue;
//            }
//            while (true){
//                if (isBye()){
//                    client.close();
//                    out.close();
//                    in.close();
//                    break;
//                }
//                listener.setSoTimeout(1000);
//                BufferedReader input = new BufferedReader(new InputStreamReader(in));
//                String request = input.readLine();
//                System.out.println(request);
//                String[] arr;
//                String client_checker;
//                if (request != null) {
//                    arr = request.split(" ");
//                    client_checker = arr[1].substring(1);
//                    if (client_checker.equals("SEARCH: ")) {
//                        if(ht.containsKey(arr[2])){
//                            LinkedList<String> list = (LinkedList<String>) ht.get(arr[2]);
//                            System.out.println("[SERVER] File that you are looking for is found...");
//                            String sendIt = "FOUND: "+ list.toString();
//                            out.write(sendIt.getBytes(StandardCharsets.UTF_8));
//                        }else{
//                            System.out.println("[SERVER] File that you are looking for doesn't exist...");
//                            out.write("NOT FOUND".getBytes(StandardCharsets.UTF_8));
//                        }
//
//                    }
//                }
//
//            }
//
//        }
//        public static boolean isHello() throws IOException {
//            BufferedReader input = new BufferedReader(new InputStreamReader(in));
//            String request = input.readLine();
//            System.out.println(request);
//            String[] arr;
//            String client_checker;
//            if (request != null) {
//                arr = request.split(" ");
//                client_checker = arr[1].substring(1);
//                if (client_checker.equals("HELLO")) {
//                    System.out.println("[SERVER] Connection verified...");
//                    out.flush();
//                    out.write("HI".getBytes(StandardCharsets.UTF_8));
//                    System.out.println("[SERVER] Connection established...");
//                    return true;
//                } else {
//                    System.out.println("[SERVER] NO GREETINGS...");
//                    System.out.println("[SERVER] Connection dismissed...");
//                    return false;
//                }
//            }
//            return false;
//        }
//        public static boolean isBye() throws IOException {
//            BufferedReader input = new BufferedReader(new InputStreamReader(in));
//            String request = input.readLine();
//            System.out.println(request);
//            String[] arr;
//            String client_checker;
//            if (request != null) {
//                arr = request.split(" ");
//                client_checker = arr[1].substring(1);
//                if (client_checker.equals("BYE")) {
//                    System.out.println("[SERVER] Disconnection requested...");
//                    out.flush();
//                    out.write("BYE".getBytes(StandardCharsets.UTF_8));
//                    System.out.println("[SERVER] Connection ended...");
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//            return false;
//        }
//        public static boolean isFile() throws IOException {
//            client = listener.accept();
//            in = client.getInputStream();
//            BufferedReader input = new BufferedReader(new InputStreamReader(in));
//            String files = input.readLine();
//            System.out.println(files);
//            if(files==null){
//                return false;
//            }
//            //: <file type, file size, file last modified date (DD/MM/YY), IP address, port number>
//            // String[] arr= files.replaceAll("[^a-zA-Z0-9./ ]", "").split(" ");
//            String[] arr= files.replaceAll("[^a-zA-Z0-9./ ]","").split(" ");
//            arr[1] = arr[1].substring(1);
//            LinkedList<String> list = new LinkedList<>(Arrays.asList(arr));
//            list.removeFirst();
//            list.removeLast();
//            System.out.println(list.toString());
//            for (int i=0;i<list.size();i+=6){
//                ht.put(list.get(i),list.subList(i,i+5));
//            }
//            System.out.println("THE HASHTABLE: "+ht.toString());
////    System.out.println(Arrays.toString(arr));
////    System.out.println("ARR");
////    System.out.println(arr[1+1]);
////    System.out.println(arr[1+2]);
////    System.out.println(arr[1+3]);
////    System.out.println(arr[1+4]);
////    System.out.println(arr[1+5]);
////    System.out.println(arr[1]);
////    System.out.println("END");
////    for(int i=1;i<=arr.length-2;i+=6){   System.out.println(arr[i]);
////      List<String> l = new ArrayList<String>(5);
////      l.add(arr[i+1]);
////      l.add(arr[i+2]);
////      l.add(arr[i+3]);
////      l.add(arr[i+4]);
////      l.add(arr[i+5]);
////      ht.put(arr[i],l);
////    }
//            return true;
//        }


    }
}
