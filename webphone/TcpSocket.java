/*
 * tcp serversocket & socket
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TcpSocket {
    private ServerSocket serverSocket=null;
    private Socket socket=null;
    private String accept_ip;

    private boolean threadRunning = false;
    private ServerSocketThread ssThread;
    private SocketSendThread sendThread;
    private SocketReceiveThread receiveThread;
    
    private BufferedWriter writer;
    private BufferedReader reader;
    private TcpSocketEventListener eventListener;
    private LinkedList<String> message;
    

    
    public TcpSocket(){
        message = new LinkedList<String>();
    }
    
    public void startServer(int tcpPort){
        //threadRunning=true;
        if(serverSocket==null){
            try {
                serverSocket = new ServerSocket(tcpPort);

            } catch (IOException ex) {
                Logger.getLogger(TcpSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        ssThread = new ServerSocketThread();
        ssThread.start();
    }
    
    public void connect(String server,int port){
            socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(server, port),10000);
            } catch (IOException ex) {
                Logger.getLogger(TcpSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(socket.isConnected()){
                eventListener.onConnect();
                startSocketThread();
            }
    }
    
    private void startSocketThread(){
        
        try {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TcpSocket.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TcpSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        sendThread = new SocketSendThread();
        receiveThread = new SocketReceiveThread();
        threadRunning=true;
        sendThread.start();
        receiveThread.start();
    }
    
    public void closeSocket(){
        threadRunning=false;
        try{
            if(socket != null ){
                socket.close();
            }
            if(writer != null){
                writer.close();
            }
            if(reader != null){
                reader.close();
            }
        }catch (IOException ex) {
                Logger.getLogger(TcpSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void closeServer(){
        threadRunning=false;
        try {
            serverSocket.close();
            if(socket != null ){
                socket.close();
                
            }
            if(writer != null){
                writer.close();
            }
            if(reader != null){
                reader.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(TcpSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void send(String message){
        synchronized(this.message){
            this.message.add(message+"\r\n");;
        }
    }
    
    public void registerEventListener(TcpSocketEventListener el){
        eventListener = el;
    }
    
    private class ServerSocketThread extends Thread{
        public void run(){
            try {
                socket = serverSocket.accept();
                System.out.println("TcpSocket:Accept");
                eventListener.onAccept();
                startSocketThread();
                
            } catch (IOException ex) {
                Logger.getLogger(TcpSocket.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
    }
    
    private class SocketSendThread extends Thread{
        public void run(){
            try {
                writer.flush();
                while(threadRunning){
                    synchronized(message){
                        while(!message.isEmpty()){
                            String sendMsg = message.poll();
                            writer.write(sendMsg);
                            System.out.println("TcpSocket:Send "+sendMsg);
                            
                        }
                        writer.flush();
                    }
                    Thread.sleep(5);
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(TcpSocket.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(TcpSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private class SocketReceiveThread extends Thread{
        public void run(){        
            try {
                while(threadRunning){
                    String line = reader.readLine();
                    
                    if(line != null){
                        System.out.println("TcpSocket:Get "+line);
                        eventListener.onReceive(line);
                    }
                    Thread.sleep(5);
                }
                
            } catch (InterruptedException ex) {
                Logger.getLogger(TcpSocket.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(TcpSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
