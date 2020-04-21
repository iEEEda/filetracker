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
  private String[] files = new String[5];
  int size = 0;

  SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YY");
  //String str[] = {"Info1", "Info2", "Info3", "Info4", "Info5"};

  public GUI() {
    super("Example GUI");
    setLayout(null);
    setSize(500,600);

    label = new JLabel("File name:");
    label.setBounds(50,50, 80,20);
    add(label);

    tf = new JTextField();
    tf.setBounds(130,50, 215,20);
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
    listScroller.setBounds(50, 80,300,300);

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
    if(getConnection()) {
      startAccepting();
    }
  }
  public void actionPerformed(ActionEvent e){
    if(e.getSource() == search){
      String fileName = tf.getText();
      String[] found = requestFiles(fileName);
      if (found != null) {
        for (int i = 0; i < found.length; i++) {
          listModel.insertElementAt(found[i], i);
        }
      } else {
        listModel.insertElementAt("Not found", 0);
      }
//      Random r = new Random();
//      for (int i = 0; i < 25; i++) {
//        listModel.insertElementAt(fileName + " " + str[r.nextInt(str.length)], i);
//      }
      showed = true;
    }
    else if(e.getSource() == dload) {
      if(showed && !jl.isSelectionEmpty())
        downloadFiles(jl.getSelectedValue().toString());
    }
    else if(e.getSource() == upload && size < 5) {
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
        System.out.println(files[size - 1]);
      }
    } else if (e.getSource() == exit) {
      leave();
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
      output.write("HELLO".getBytes(StandardCharsets.UTF_8));
      InputStreamReader reader = new InputStreamReader(input);
      int ch;
      StringBuilder data = new StringBuilder();
      while ((ch = reader.read()) != -1) {
        data.append((char) ch);
      }
      if (data.toString().startsWith("HI")) {
        for (int i = 0; i < size; i++) {
          output.write(files[i].getBytes(StandardCharsets.UTF_8));
        }
      }
      reader.close();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public void startAccepting() {
    try {
      ServerSocket server = new ServerSocket(port);
      while (true) {
        System.out.println("[SERVER] waiting for clients...");
        Socket client = server.accept();
        System.out.println("[SERVER] Connected to client!");
        InputStream in = client.getInputStream();
        OutputStream out = client.getOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = reader.readLine();
        if (line.startsWith("DOWNLOAD: ")) {
          System.out.println("Client wants to download a file");
          String[] fileInfo = line.substring(10).split(", ");
          byte[] filebytes;
          File file = new File(fileInfo[0]);
          if (file.exists()) {
            System.out.println("File exists");
            filebytes = Files.readAllBytes(file.toPath());
            out.write(filebytes, 0, filebytes.length);
            out.flush();
            System.out.println("Done.");
          }
        }
        out.close();
        in.close();
        client.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String[] requestFiles(String name) {
    String search = "SEARCH: " + name;
    //List<String> found = new ArrayList<>();
    String[] found = null;
    try {
      output.write(search.getBytes(StandardCharsets.UTF_8));
      InputStreamReader reader = new InputStreamReader(input);
      int ch;
      StringBuilder data = new StringBuilder();
      while ((ch = reader.read()) != -1) {
        data.append((char) ch);
      }
      if (data.toString().startsWith("F")) {
        found = data.toString().split(">");
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return found;
  }

  public void downloadFiles(String file) {
    String[] info = file.split(", ");
    try {
      Socket anotherPeer = new Socket(info[3], Integer.getInteger(info[4]));
      InputStream pInput = anotherPeer.getInputStream();
      OutputStream pOutput = anotherPeer.getOutputStream();
      pOutput.write("DOWNLOAD: ".getBytes(StandardCharsets.UTF_8));
      String fileInfo = info[0] + ", " + info[1] + ", " + info[2];
      pOutput.write(fileInfo.getBytes(StandardCharsets.UTF_8));
      InputStreamReader pReader = new InputStreamReader(pInput);
      int ch;
      StringBuilder data = new StringBuilder();
      while ((ch = pReader.read()) != -1) {
        data.append((char) ch);
      }
      if (data.toString().startsWith("FILE: ")) {
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
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    //connects to another peer with needed files
    //downloads files by sending DOWNLOAD, then FILE: fname, type, size
  }

  public void leave() {
    String bye = "BYE";
    try {
      output.write(bye.getBytes(StandardCharsets.UTF_8));
      input.close();
      output.close();
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}