package org.example;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.FileChooserUI;

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
  private JList jl;
  private JLabel label;
  private JTextField tf,tf2;
  DefaultListModel listModel;
  private JFileChooser fc;
  private boolean showed = false;
  final int FTport = 111;
  final String FTIP = "192.168.1.1";
  private String IP;
  private String port;
  private String[] files = new String[5];
  int size = 0;

  SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YY");
  String str[] = {"Info1", "Info2", "Info3", "Info4", "Info5"};

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
  }
  public void actionPerformed(ActionEvent e){
    if(e.getSource() == search){
      String fileName = tf.getText();
      Random r = new Random();
      for (int i = 0; i < 25; i++) {
        listModel.insertElementAt(fileName + " " + str[r.nextInt(str.length)], i);
      }
      showed = true;
    }
    else if(e.getSource() == dload) {
      if(showed && !jl.isSelectionEmpty())
        tf2.setText(jl.getSelectedValue().toString() + " donwloaded");
    }
    else if(e.getSource() == upload && size < 5) {
      fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
      int r = fc.showOpenDialog(null);
      if (r == JFileChooser.APPROVE_OPTION) {
        File f = fc.getSelectedFile();
//        String[] type = f.getName().split("\\.(?=[^\\.]+$)");
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
    }
  }
  public String findPublicIp() {
    String systemipaddress = "";
    try {
      URL url_name = new URL("http://bot.whatismyipaddress.com");
      BufferedReader sc =
          new BufferedReader(new InputStreamReader(url_name.openStream()));
      systemipaddress = sc.readLine().trim();
    }
    catch (Exception e) {
      systemipaddress = "Cannot Execute Properly";
    }
    return systemipaddress;
  }
  public void getConnection() {
    //send HELLO
    //then files
  }

  public void requestFiles() {
    //requests files from FT, SEARCH: fname
    //receives list of records
  }

  public void downloadFiles() {
    //connects to another peer with needed files
    //downloads files by sending DOWNLOAD, then FILE: fname, type, size
  }

  public void leave() {
    //send BYE to FT
    //leave the system
  }
}