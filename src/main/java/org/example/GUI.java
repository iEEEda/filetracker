package org.example;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sain
 */
public class GUI extends JFrame implements ActionListener{
  private JButton search;
  private JButton dload;
  private JButton upload;
  private JButton exit;
  private JList jl;
  private JLabel label;
  private JTextField tf,tf2;
  DefaultListModel listModel;
  private JFileChooser fc;
  private boolean showed = false;
  final int FTport = 9999;
  final String FTIP = "127.0.0.1";
  private String IP;
  private int port;
  InputStream input;
  OutputStream output;
  Socket socket;
  BufferedReader reader;
  private AtomicBoolean disconect = new AtomicBoolean(false);
  private String[] files = new String[5];
  int size = 0;
  int elements = 0;

  SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YY");
  //String str[] = {"Info1", "Info2", "Info3", "Info4", "Info5"};

  public GUI() {
    super("Example GUI");
    setLayout(null);
    setSize(500,600);

    label = new JLabel("File name:");
    label.setBounds(40,50, 80,20);
    add(label);

    tf = new JTextField();
    tf.setBounds(120,50, 225,20);
    add(tf);

    search = new JButton("Search");
    search.setBounds(360,50,80,20);
    search.addActionListener(this);
    add(search);

    upload = new JButton("Upload");
    upload.setBounds(360, 80, 80, 20);
    upload.addActionListener(this);
    add(upload);

    exit = new JButton("Disconnect");
    exit.setBounds(350, 480, 120, 20);
    exit.addActionListener(this);
    add(exit);

    listModel = new DefaultListModel();
    jl = new JList(listModel);

    JScrollPane listScroller = new JScrollPane(jl);
    listScroller.setBounds(40, 80,310,300);

    add(listScroller);

    dload = new JButton("Download");
    dload.setBounds(200,400,130,20);
    dload.addActionListener(this);
    add(dload);

    tf2 = new JTextField();
    tf2.setBounds(200,430,130,20);
    add(tf2);

    setVisible(true);
    IP = findPublicIp();
    port = 456;
    if (getConnection()) {
      startAccepting();
    }
  }
  public void actionPerformed(ActionEvent e){
    if(e.getSource() == search){
      if (!disconect.get()) {
        String fileName = tf.getText();
        String[] found = requestFiles(fileName);
        if (found != null) {
          for (int i = elements, j = 0; i < (elements + found.length) && j < found.length;
               i++, j++) {
            listModel.insertElementAt("Received: " + found[j], i);
          }
          elements += found.length;
        } else {
          tf2.setText("Not found");
        }
        showed = true;
      }
    }
    else if(e.getSource() == dload) {
      if(showed && !jl.isSelectionEmpty())
        downloadFiles(jl.getSelectedValue().toString());
    }
    else if(e.getSource() == upload) {
      if (size < 5) {
        fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        int r = fc.showOpenDialog(null);
        if (r == JFileChooser.APPROVE_OPTION) {
          File f = fc.getSelectedFile();
          String name = "";
          String type = "";
          int i = f.getName().lastIndexOf('.');
          if (i > 0) {
            name = f.getName().substring(0, i);
            type = f.getName().substring(i + 1);
          }
          StringBuilder builder = new StringBuilder("<");
          builder.append(name).append(", ");
          builder.append(type).append(", ");
          builder.append(f.length()).append(", ");
          builder.append(sdf.format(f.lastModified())).append(", ");
          builder.append(IP).append(", ").append(port).append(">");
          files[size] = builder.toString();
          size++;
          sendFiles(size - 1);
          listModel.insertElementAt("Sent: " + name, elements);
          elements++;
          System.out.println(files[size - 1]);
        }
      } else {
        tf2.setText("Maximum 5 files");
      }
    } else if (e.getSource() == exit) {
      if (!disconect.get()) {
        leave();
        exit.setText("Connect");
      } else {
        disconect.set(false);
        getConnection();
        startAccepting();
        exit.setText("Disconnect");
        tf2.setText("Connected");
      }
    }
  }

  public String findPublicIp() {
    String globalip = null;
    try {
      URL url_name = new URL("http://bot.whatismyipaddress.com");
      BufferedReader sc =
          new BufferedReader(new InputStreamReader(url_name.openStream()));
      globalip = sc.readLine().trim();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return globalip;
  }

  public boolean getConnection() {
    try {
      socket = new Socket(FTIP, FTport);
      input = socket.getInputStream();
      output = socket.getOutputStream();
      output.write("HELLO\n".getBytes(StandardCharsets.UTF_8));
      output.flush();
      reader = new BufferedReader(new InputStreamReader(input));
      String data = reader.readLine();
      if (data.contains("HI")) {
        System.out.println(data);
        System.out.println("HI received");
        tf2.setText("Connected");
      }
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public void sendFiles(int i) {
    try {
      output.write((files[i] + "\n").getBytes(StandardCharsets.UTF_8));
      output.flush();
      tf2.setText("File is sent");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void startAccepting() {
//    System.out.println("Creating a thread");
//    new Thread(() -> {
//      System.out.println("Thread created");
      try {
        ServerSocket server = new ServerSocket(port);
        System.out.println("Start listening");
        while (!disconect.get()) {
          System.out.println("waiting for clients...");
          Socket client = server.accept();
          System.out.println("Connected to client!");
          InputStream in = client.getInputStream();
          OutputStream out = client.getOutputStream();
          BufferedReader reader2 = new BufferedReader(new InputStreamReader(in));
          String line = reader2.readLine();
          if (line.startsWith("DOWNLOAD: ")) {
            System.out.println("Client wants to download a file");
            out.write("FILE: \n".getBytes(StandardCharsets.UTF_8));
            String[] fileInfo = line.substring(10).split(", ");
            byte[] filebytes;
            File file = new File(fileInfo[0] + "." + fileInfo[1]);
            if (file.exists()) {
              System.out.println("File exists");
              filebytes = Files.readAllBytes(file.toPath());
              out.write(filebytes, 0, filebytes.length);
              out.flush();
              System.out.println("Done.");
            }
          }
          client.close();
          reader2.close();
          out.close();
          in.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    //});
  }

  public String[] requestFiles(String name) {
    String search = "SEARCH: " + name + "\n";
    //List<String> found = new ArrayList<>();
    String[] found = null;
    try {
      output.write(search.getBytes(StandardCharsets.UTF_8));
      String data = reader.readLine();
      if (data != null) {
        if (data.startsWith("F")) {
          found = data.substring(7).replaceAll("<", name + ", ").split(">");
          for (int i = 1; i < found.length; i++) {
            found[i] = found[i].substring(2);
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    tf.setText("");
    return found;
  }

  public void downloadFiles(String file) {
    String[] info = file.substring(10).split(", ");
    try {
      String fileInfo = info[0] + ", " + info[1] + ", " + info[2] + "\n";
//    System.out.println(fileInfo);
      for (int i = 0; i < info.length; i++) {
        System.out.println();
      }
      Socket anotherPeer = new Socket(info[4], Integer.parseInt(info[5]));
      InputStream pInput = anotherPeer.getInputStream();
      OutputStream pOutput = anotherPeer.getOutputStream();
      pOutput.write(("DOWNLOAD: " + fileInfo).getBytes(StandardCharsets.UTF_8));
      String data = reader.readLine();
      if (data.startsWith("FILE: ")) {
        DataInputStream dis = new DataInputStream(input);
        FileOutputStream fos = new FileOutputStream(info[0] + "." + info[1]);
        int fsize = Integer.parseInt(info[2]);
        int read = 0;
        byte[] buf = new byte[4096];
        while ((read = dis.read(buf, 0, Math.min(buf.length, fsize))) > 0) {
          fsize -= read;
          fos.write(buf, 0, read);
        }
        fos.close();
        dis.close();
        anotherPeer.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void leave() {
    String bye = "BYE\n";
    try {
      output.write(bye.getBytes(StandardCharsets.UTF_8));
      reader.close();
      input.close();
      output.close();
      socket.close();
      tf.setText("");
      tf2.setText("Disconnected");
      listModel.removeAllElements();
      elements = 0;
      disconect.set(true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}