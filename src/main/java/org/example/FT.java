package org.example;
import com.sun.security.ntlm.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FT {
  private static final String IP = "127.0.0.1";
  private static final int PORT = 9999;

  private static ExecutorService threads = Executors.newFixedThreadPool(4);
  private static Hashtable<String, List<String>> ht = new Hashtable<>();
  private static ArrayList<MultiClientHandler> clients = new ArrayList<>();


  public static void main(String[] args) throws IOException {
    ServerSocket listener = new ServerSocket(PORT);
    System.out.println("[SERVER] created...");
    try {
      while (true) {
        System.out.println("[SERVER] waiting for clients...");
        Socket client = listener.accept();
        System.out.println("[SERVER] Connected to client!");
        MultiClientHandler visitor = new MultiClientHandler(client, ht);
        clients.add(visitor);
        threads.execute(visitor);
      }
    } catch (Exception ex) {
      System.err.println("[SERVER] IOException in server loop!");
      ex.printStackTrace();
    }
    }
  }
