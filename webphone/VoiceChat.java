

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import local.net.RtpPacket;
import local.net.RtpSocket;
import org.zoolu.net.IpAddress;
import org.zoolu.net.UdpSocket;




public class VoiceChat {
    byte[] sendBuffer;
    boolean mic;
    byte[] receiveBuffer;
    String connectAddress;
    int connectPort;
    int bufferSize;
    //SipURL recvSipURL;
    //NameAddress recvNameAddress;
    
    UdpSocket udpSocket;
    RtpSocket rtpSocket;
    
    boolean threadRunning = false;
    
    MicThread micThread;
    SpeakerThread speakerThread;
    //SendThread sendThread;
    //ReceiveThread receiveThread;
    
    AudioFormat audioFormat;
    DataLine.Info recordLineInfo;
    TargetDataLine recordLine;
    
    DataLine.Info playLineInfo;
    SourceDataLine playLine;
    
    
    public void init(String address, int port){
        connectAddress = address;
        connectPort = port;
        try {        
            udpSocket = new UdpSocket(connectPort);
            rtpSocket = new RtpSocket(udpSocket,IpAddress.getByName(connectAddress),connectPort);
            
            
            audioFormat = new AudioFormat(8000,8,2,true,true);
            recordLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            recordLine = (TargetDataLine)AudioSystem.getLine(recordLineInfo);
            //System.out.printf("%b",AudioSystem.isLineSupported(Port.Info.MICROPHONE));
            //recordLine =  AudioSystem.getLine(Port.Info.MICROPHONE).;
            
            
            //Line line;
            //line = AudioSystem.getLine(Port.Info.MICROPHONE);
            //recordLine = (TargetDataLine) AudioSystem.getLine(line.getLineInfo());
            /*if(recordLine == null){
                    System.out.println("Did not worked");
                }else{
                    System.out.println("It worked");
                }*/
            
            bufferSize = (int)audioFormat.getSampleRate() * audioFormat.getFrameSize();
            System.out.printf("%d %f %d", bufferSize,audioFormat.getSampleRate(),audioFormat.getFrameSize());
            //bufferSize = 4000;
            sendBuffer = new byte[bufferSize];
            receiveBuffer = new byte[bufferSize];
            
            
            playLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            playLine =  (SourceDataLine)AudioSystem.getLine(playLineInfo);
            
            
            
        } catch (SocketException ex) {
            Logger.getLogger(VoiceChat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(VoiceChat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LineUnavailableException ex) {
            Logger.getLogger(VoiceChat.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }
    
    public void start(){
        threadRunning = true;
        micThread = new MicThread();
        speakerThread = new SpeakerThread();
        //sendThread = new SendThread();
        //receiveThread = new ReceiveThread();
        
        
        
        
        micThread.start();
        speakerThread.start();
        //sendThread.start();
        //receiveThread.start();
    }
    
    public void close(){
        threadRunning=false;
        //recordLine.drain();
        recordLine.close();
        //playLine.drain();
        playLine.close();
        udpSocket.close();
        rtpSocket.close();
    }
    
    private class MicThread extends Thread{
        @Override
        public void run(){
            try {
                recordLine.open(audioFormat);
            } catch (LineUnavailableException ex) {
                Logger.getLogger(VoiceChat.class.getName()).log(Level.SEVERE, null, ex);
            }
            recordLine.start();
            int count;
            while(threadRunning){
                try {
                    count = recordLine.read(sendBuffer, 0, sendBuffer.length);
                    if (count > 0) {
                      System.out.printf("RECORDED: %s\n", sendBuffer);
                      RtpPacket rtpPacket= new RtpPacket(sendBuffer,sendBuffer.length);
                      rtpSocket.send(rtpPacket);
                      //recordLine.drain();
                    }
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                    //Logger.getLogger(VoiceChat.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    //Logger.getLogger(VoiceChat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //recordLine.drain();
            //recordLine.close();
            System.out.println("Voice send close");
        }
        
    }
    
    private class SpeakerThread extends Thread{
        @Override
        public void run(){
            //byte[] byteData = new byte[bufferSize];
            try {
                playLine.open(audioFormat);
            } catch (LineUnavailableException ex) {
                Logger.getLogger(VoiceChat.class.getName()).log(Level.SEVERE, null, ex);
            }
            playLine.start();
            
            while(threadRunning){
                try {
                    RtpPacket rtpPacket=new RtpPacket(receiveBuffer,bufferSize);
                    rtpSocket.receive(rtpPacket);
                    System.out.printf("RECV: %s\n",rtpPacket.getPacket());
                    playLine.write(receiveBuffer, 0, bufferSize);
                    //

                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                    //Logger.getLogger(VoiceChat.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    //Logger.getLogger(VoiceChat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //playLine.drain();
            //playLine.close();
            System.out.println("Voice recv close");
        }
        
    }
    /*
    private class SendThread extends Thread{
        @Override
        public void run(){
            while(threadRunning){
                try {
                    String data = "123456789ABC";
                    byte[] byteData = data.getBytes();
                    //System.out.printf("%s", byteData);
                    try {
                        RtpPacket rtpPacket= new RtpPacket(byteData,byteData.length);
                        rtpSocket.send(rtpPacket);
                    } catch (IOException ex) {
                        Logger.getLogger(SipUA.class.getName()).log(Level.SEVERE, null, ex);
                        System.err.println("rtpSocket.send(new RtpPacket(data,4)) failed");
                    }
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(VoiceChat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
        
    private class ReceiveThread extends Thread{
        @Override
        public void run(){
            while(threadRunning){
                try {
                    byte[] byteData = new byte[12];
                    RtpPacket rtpPacket=new RtpPacket(byteData,12);
                    rtpSocket.receive(rtpPacket);
                    //System.out.printf("RECV: %s\n",rtpPacket.getPacket());
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(VoiceChat.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(VoiceChat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }*/
    
}
