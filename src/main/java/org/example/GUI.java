//Aida Eduard, Yernur Sabyrzhanov
package org.example;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

public class GUI extends JFrame implements ActionListener{
  private static ExecutorService threads = Executors.newFixedThreadPool(1);

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
  private int upperBound = 9999;
  private int lowerBound = 1000;
  private String IP;
  private int port;
  InputStream input;
  OutputStream output;
  Socket socket;
  BufferedReader reader;
  private AtomicBoolean disconect = new AtomicBoolean(false);
  File directory = FileSystemView.getFileSystemView().getHomeDirectory();
  private String[] files = new String[5];
  int size = 0;
  int elements = 0;

  SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YY");

  public GUI() {
    super("File Transfer Client v.62 (c) powered by Aida and Yernur");
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
    IP = "127.0.0.1";
    port = lowerBound + (int)(Math.random() * ((upperBound - lowerBound) + 1));
    if (getConnection()) {
      startAccepting();
    }

    this.addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        leave();
        System.exit(0);
      }
    });
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
        fc = new JFileChooser(directory);
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
      try {
        ServerSocket server = new ServerSocket(port);
        System.out.println("[FT CLIENT] start listening");
        while (!disconect.get()) {
          System.out.println("[FT CLIENT] waiting for peers...");
          //server.setSoTimeout(60000);
          Socket client = server.accept();
          System.out.println("[FT CLIENT] connected to peer!");
          FileHandler peer = new FileHandler(client, directory);
          threads.execute(peer);
        }
        server.close();
      } catch (SocketTimeoutException e) {
        leave();
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
  }

  public String[] requestFiles(String name) {
    String search = "SEARCH: " + name + "\n";
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
      Socket anotherPeer = new Socket(info[4], Integer.parseInt(info[5]));
      InputStream pInput = anotherPeer.getInputStream();
      OutputStream pOutput = anotherPeer.getOutputStream();
      pOutput.write(("DOWNLOAD: " + fileInfo).getBytes(StandardCharsets.UTF_8));
      BufferedReader pReader = new BufferedReader(new InputStreamReader(pInput));
      String data = pReader.readLine();
      if (data.startsWith("FILE: ")) {
        DataInputStream dis = new DataInputStream(pInput);
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
        pReader.close();
        pOutput.close();
        pInput.close();
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
      threads.shutdownNow();
      tf2.setText("Disconnected");
      listModel.removeAllElements();
      elements = 0;
      disconect.set(true);
      //System.exit(0);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    GUI ex = new GUI();
    ex.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        ex.leave();
        System.exit(0);
      }
    });
  }
}