package org.example;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.LinkedList;

public class FT {

  private static ServerSocket listener;
  private static Socket client;
  private static OutputStream out;
  private static InputStream in;
  private static final String IP = "192.168.1.1";
  private static final int PORT = 9999;
  private static Hashtable<String, List<String>> ht = new Hashtable<>();


  public static boolean isHello() throws IOException {
    BufferedReader input = new BufferedReader(new InputStreamReader(in));
    String request = input.readLine();
    System.out.println(request);
    String[] arr;
    String client_checker;
    if (request != null) {
      arr = request.split(" ");
      client_checker = arr[1].substring(1);
      if (client_checker.equals("HELLO")) {
        System.out.println("[SERVER] Connection verified...");
        out.flush();
        out.write("HI".getBytes(StandardCharsets.UTF_8));
        System.out.println("[SERVER] Connection established...");
        return true;
      } else {
        System.out.println("[SERVER] NO GREETINGS...");
        System.out.println("[SERVER] Connection dismissed...");
        return false;
      }
    }
    return false;
  }
  public static boolean isBye() throws IOException {
    BufferedReader input = new BufferedReader(new InputStreamReader(in));
    String request = input.readLine();
    System.out.println(request);
    String[] arr;
    String client_checker;
    if (request != null) {
      arr = request.split(" ");
      client_checker = arr[1].substring(1);
      if (client_checker.equals("BYE")) {
        System.out.println("[SERVER] Disconnection requested...");
        out.flush();
        out.write("BYE".getBytes(StandardCharsets.UTF_8));
        System.out.println("[SERVER] Connection ended...");
        return true;
      } else {
        return false;
      }
    }
    return false;
  }
  public static boolean isFile() throws IOException {
    client = listener.accept();
    in = client.getInputStream();
    BufferedReader input = new BufferedReader(new InputStreamReader(in));
    String files = input.readLine();
    System.out.println(files);
    if(files==null){
      return false;
    }
    //: <file type, file size, file last modified date (DD/MM/YY), IP address, port number>
     // String[] arr= files.replaceAll("[^a-zA-Z0-9./ ]", "").split(" ");
      String[] arr= files.replaceAll("[^a-zA-Z0-9./ ]","").split(" ");
    arr[1] = arr[1].substring(1);
    LinkedList<String> list = new LinkedList<>(Arrays.asList(arr));
    list.removeFirst();
    list.removeLast();
    System.out.println(list.toString());
    for (int i=0;i<list.size();i+=6){
        ht.put(list.get(i),list.subList(i,i+5));
    }
    System.out.println("THE HASHTABLE: "+ht.toString());
//    System.out.println(Arrays.toString(arr));
//    System.out.println("ARR");
//    System.out.println(arr[1+1]);
//    System.out.println(arr[1+2]);
//    System.out.println(arr[1+3]);
//    System.out.println(arr[1+4]);
//    System.out.println(arr[1+5]);
//    System.out.println(arr[1]);
//    System.out.println("END");
//    for(int i=1;i<=arr.length-2;i+=6){   System.out.println(arr[i]);
//      List<String> l = new ArrayList<String>(5);
//      l.add(arr[i+1]);
//      l.add(arr[i+2]);
//      l.add(arr[i+3]);
//      l.add(arr[i+4]);
//      l.add(arr[i+5]);
//      ht.put(arr[i],l);
//    }
    return true;
  }
  public FT(){
    try {
      Socket socket = new Socket(IP,PORT);
    } catch (IOException e) {

      e.printStackTrace();
    }
  }
  public static void main(String[] args) throws IOException {
    FT server = new FT();
    listener = new ServerSocket(PORT);
    System.out.println("[SERVER] created, waiting for clients...");
                  while (true) {
                    client = listener.accept();
                    System.out.println("[SERVER] Connected to client...");
                    out = client.getOutputStream();
                    in = client.getInputStream();
                    if (!isHello()){
                      client.close();
                      out.close();
                      in.close();
                      continue;
                    }
                      listener.setSoTimeout(1000);

                      if(!isFile()){
                      client.close();
                      out.close();
                      in.close();
                      continue;
                    }
                              while (true){
                                if (isBye()){
                                  client.close();
                                  out.close();
                                  in.close();
                                  break;
                                }
                                listener.setSoTimeout(1000);
                                  BufferedReader input = new BufferedReader(new InputStreamReader(in));
                                  String request = input.readLine();
                                  System.out.println(request);
                                  String[] arr;
                                  String client_checker;
                                  if (request != null) {
                                      arr = request.split(" ");
                                      client_checker = arr[1].substring(1);
                                      if (client_checker.equals("SEARCH: ")) {
                                          if(ht.containsKey(arr[2])){
                                              LinkedList<String> list = (LinkedList<String>) ht.get(arr[2]);
                                              System.out.println("[SERVER] File that you are looking for is found...");
                                              String sendIt = "FOUND: "+ list.toString();
                                              out.write(sendIt.getBytes(StandardCharsets.UTF_8));
                                          }else{
                                              System.out.println("[SERVER] File that you are looking for doesn't exist...");
                                              out.write("NOT FOUND".getBytes(StandardCharsets.UTF_8));
                                          }

                                      }
                                  }

                              }

                  }

  }
}
