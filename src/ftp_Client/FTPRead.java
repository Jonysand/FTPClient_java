package ftp_Client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FTPRead {
    
    //定义基础变量
    Socket socket = null;
    BufferedReader reader = null;
    BufferedWriter writer = null;
    
    public synchronized void connect(String host) throws IOException {
        connect(host, 21);
    }
    
    public synchronized void connect(String host, int port) throws IOException {
        connect(host, port, "anonymous", "anonymous");
    }
    
    public synchronized String connect(String host, int port, String user, String pass) {
        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String line = reader.readLine();
            System.out.println("step1 ----- " + line);
            
            //input user
            sendCommand("USER " + user);
            line = reader.readLine();
            System.out.println("step2 -----" + line);
            
            //input pwd
            sendCommand("PASS " + pass);
            line = reader.readLine();
            System.out.println("step3 -----" + line);
            
            return line;
            
        } catch (UnknownHostException ex) {
            System.out.println("Couldn't find the Ftp Server");
        } catch (IOException ex) {
            System.out.println("IOException");
        }
		return null;
    }
    
    public synchronized void disconnect() throws IOException {
        
        try {
            sendCommand("QUIT");
            System.out.println("last step ----- " + reader.readLine());
        } finally {
            socket = null;
        }
    }
    
    public synchronized ArrayList<String> listFiles(String serverPath) throws IOException {
        
        writer.write("cwd " + serverPath + "\r\n"); //若要指定某一位置就修改 caches
        writer.flush();
        System.out.println(reader.readLine());
        
        sendCommand("PASV");
        
        String response = reader.readLine();
        String ip = null;
        int port1 = -1;
        int opening = response.indexOf('(');
        int closing = response.indexOf(')', opening + 1);
        
        if (closing > 0) {
            String dataLink = response.substring(opening + 1, closing);
            StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
            try {
                ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
                + tokenizer.nextToken() + "." + tokenizer.nextToken();
                port1 = Integer.parseInt(tokenizer.nextToken()) * 256
                + Integer.parseInt(tokenizer.nextToken());
            } catch (Exception e) {
                throw new IOException("SimpleFTP received bad data link information: "
                                      + response);
            }
        }
        
        System.out.println(ip + "  " + port1);
        
        writer.write("LIST ." + "\r\n");
        writer.flush();
        
        Socket dataSocket = new Socket(ip, port1);
        System.out.println(reader.readLine());
        
        InputStreamReader dat = new InputStreamReader(dataSocket.getInputStream());
        BufferedReader dis = new BufferedReader(dat);
        String s = "";
        ArrayList<String> filelist = new ArrayList<String>();
        while ((s = dis.readLine()) != null) {
//            String l = new String(s.getBytes("ISO-8859-1"), "utf-8");
            String[] files = s.split(" ");
            String filename = files[files.length-1];
            System.out.println(filename);
            filelist.add(filename);
        }
        
        dis.close();
        dataSocket.close();
        
        System.out.println(reader.readLine());
		return filelist;
    }
    
    public synchronized boolean upload(String lfilepath, String serverPath) throws IOException {
        File file = new File(lfilepath);
        
        if (file.isDirectory()) {
            throw new IOException("SimpleFTP cannot upload a directory.");
        }
        
        String filename = file.getName();
        BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
        
        writer.write("cwd  " + serverPath + "\r\n"); //若要指定某一位置就修改 caches
        writer.flush();
        System.out.println(reader.readLine());
        
        sendCommand("PASV");
        String response = reader.readLine();
        if (!response.startsWith("227 ")) {
            throw new IOException("SimpleFTP could not request passive mode: "
                                  + response);
        }
        
        String ip = null;
        int port = -1;
        int opening = response.indexOf('(');
        int closing = response.indexOf(')', opening + 1);
        if (closing > 0) {
            String dataLink = response.substring(opening + 1, closing);
            StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
            try {
                ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
                + tokenizer.nextToken() + "." + tokenizer.nextToken();
                port = Integer.parseInt(tokenizer.nextToken()) * 256
                + Integer.parseInt(tokenizer.nextToken());
            } catch (Exception e) {
                throw new IOException("SimpleFTP received bad data link information: "
                                      + response);
            }
        }
        
        System.out.println(ip + "  " + port);
        
        sendCommand("STOR " + filename);
        
        Socket dataSocket = new Socket(ip, port);
        
        response = reader.readLine();
        
        BufferedOutputStream output = new BufferedOutputStream(dataSocket.getOutputStream());
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
        output.close();
        input.close();
        
        response = reader.readLine();
        return response.startsWith("226 ");
    }
    
    
    public void sendCommand(String com) throws IOException {
        
        if (socket == null) {
            throw new IOException("SimpleFTP is not connected.");
        }
        
        try {
            writer.write(com + "\r\n");
            writer.flush();
            
        } catch (IOException e) {
            socket = null;
            throw e;
        }
    }
    
//    public static void main(String args[]) throws IOException {
//        String host = "192.168.2.7";
//        int port = 21;
//        String uname = "pi";
//        String pwd = "studio";
//        
//        FTPRead fr = new FTPRead();
//        fr.connect(host, port, uname, pwd);
//        
//        fr.listFiles("Desktop");
//        
//        fr.disconnect();
//    }
}



