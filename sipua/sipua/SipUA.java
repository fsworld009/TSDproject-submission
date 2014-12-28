/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sipua;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import local.net.RtpPacket;
import local.net.RtpSocket;
import org.zoolu.net.IpAddress;
import org.zoolu.net.UdpSocket;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.call.Call;
import org.zoolu.sip.call.CallListenerAdapter;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipProviderListener;
import org.zoolu.sip.provider.Transport;

/**
 *
 * @author WorldFS
 */
public class SipUA extends CallListenerAdapter{
    SipProvider sipProvider;
    String myIpAddress="";
    SipURL mySipURL;
    NameAddress myNameAddress;
    Call callHandler;
    int myPort;
    

    
    UdpSocket udpSocket;
    RtpSocket rtpSocket;
    
    String recvAddress;
    int recvPort;
    SipURL recvSipURL;
    NameAddress recvNameAddress;
    
    VoiceChat voiceChat;
    VoiceForwarder voiceForwarder;
    
    int rtpPort;
    String remoteAddress;
    
    //MainUI uiRef;
    MainWindow uiRef;
    //CallListenerAdapter eventListener;
    SipUAEventListener eventListener=null;
    
    private int remoteRtpPort;
    
            
    public void addEventListener(SipUAEventListener ls){
        eventListener = ls;
    }
    
    public void removeEventListener(){
        //end remote control
        eventListener = null;
    }
    
 
    
    public SipUA(MainWindow sipui/*CallListenerAdapter eventListener*/){
        //this.eventListener = eventListener;
        uiRef = sipui;
    }
    
    //debug
    private void callStatus(){
        System.out.printf("Outgoing=%s Incoming=%s Active=%s Closed=%s Idle=%s\n",callHandler.isOutgoing(), callHandler.isIncoming(), callHandler.isActive(),callHandler.isClosed(),callHandler.isIdle());
    }
    
    @Override
    public void onCallAccepted(Call call, java.lang.String sdp, Message resp){
        //super.onCallAccepted(call, sdp, resp);
        //System.err.println("Accepted: "+resp);
        uiRef.appendLog("[RECV]<<< (SIP) "+resp.toString()+"\n");
        uiRef.appendMsg(String.format("Call accepted by "+resp.getRemoteAddress()+"\n"));
        
        //stop ring
        RingPlayer.ins().stopPlay();


        //Caller starts its voice chat here
        //if(callHandler.isActive()){
        //    closeVoiceChat();
        //}else{
        if(callHandler.isActive()){
            call.ackWithAnswer("");
            initVoiceChat();
            uiRef.appendMsg(String.format("Chat with "+resp.getRemoteAddress()+"...\n"));
        }else if(callHandler.isClosed()){
            //do nothing
            System.out.println("OK when closing");
        }
            
        //}
        if(eventListener != null){
            eventListener.onCallAccepted();
        }
    }
    
    @Override
    public void onCallRefused(Call call, java.lang.String reason, Message resp){
        
        uiRef.appendLog("[RECV]<<< (SIP) "+resp.toString()+"\n");
        uiRef.appendMsg(String.format("Call refused by "+resp.getRemoteAddress()+"\n"));
        RingPlayer.ins().stopPlay();
        callHandler.ackWithAnswer("");
        callHandler.listen();
        uiRef.enableButton(true, false, false, false);
        if(eventListener != null){
            eventListener.onCallRefused();
        }
    }
    
    @Override
    public void onCallInvite(Call call,NameAddress callee, NameAddress caller, java.lang.String sdp, Message invite){
        //super.onCallInvite(call, callee, caller, sdp, invite);
        //System.err.println("Invite: "+invite);
        uiRef.appendLog("[RECV]<<< (SIP) "+invite.toString()+"\n");
        uiRef.appendMsg("You got a call from "+invite.getRemoteAddress()+"\n");
        sdpRef=sdp;
        inviteRef=invite;
        if(callHandler.isIncoming()){
            call.ring();
            if(eventListener != null){
                eventListener.onCallInvite(invite.getRemoteAddress());
            }else{
                uiRef.enableButton(false, false, true, true);
                //start ring
                RingPlayer.ins().startPlay();
            }

        }
    }
    /*
    public void acceptCall(Call call,NameAddress callee, NameAddress caller, java.lang.String sdp, Message invite){
        //callStatus();
        if(callHandler.isIncoming()){
            RingPlayer.ins().stopPlay();
            uiRef.appendMsg(String.format("Accept the call from "+invite.getRemoteAddress()+"\n"));
            callHandler.accept(sdp);
        }
    }
    
    public void refuseCall(Call call,NameAddress callee, NameAddress caller, java.lang.String sdp, Message invite){
        //callStatus();
        if(callHandler.isIncoming()){
            RingPlayer.ins().stopPlay();
            uiRef.appendMsg(String.format("refuse the call from "+invite.getRemoteAddress()+"\n"));
            callHandler.refuse();
            callHandler.listen();
        }
    }*/
    
    
    private String sdpRef;
    private Message inviteRef;
    public void acceptCall(){
        if(callHandler.isIncoming()){
            RingPlayer.ins().stopPlay();
            uiRef.enableButton(false, true, false, false);
            uiRef.appendMsg(String.format("Accept the call from "+inviteRef.getRemoteAddress()+"\n"));
            callHandler.accept(sdpRef);
        }
    }
    
    public void refuseCall(){
        if(callHandler.isIncoming()){
            RingPlayer.ins().stopPlay();
            uiRef.enableButton(true, false, false, false);
            uiRef.appendMsg(String.format("Refuse the call from "+inviteRef.getRemoteAddress()+"\n"));
            callHandler.refuse();
            callHandler.listen();
        }
    }
    
    @Override
    public void onCallRinging(Call call,Message resp){
        //super.onCallRinging(call, resp);
        //System.err.println("onCallRinging: "+resp);
        if(callHandler.isOutgoing()){
            uiRef.appendLog("[RECV]<<< (SIP) "+resp.toString()+"\n");

        }
        if(eventListener != null){
            eventListener.onCallRinging();
        }else{
            //start ring
            RingPlayer.ins().startPlay();
        }
    }
    
    @Override
    public void onCallConfirmed(Call call, java.lang.String sdp, Message ack){
        //super.onCallConfirmed(call, sdp, ack);
        //System.err.println("onCallConfirmed: "+ack);
        uiRef.appendLog("[RECV]<<< (SIP) "+ack.toString()+"\n");
        if(callHandler.isActive()){
            
            //Callee starts its voice chat here
            initVoiceChat();
            uiRef.appendMsg(String.format("Chat with "+ack.getRemoteAddress()+"...\n"));
            if(eventListener != null){
                eventListener.onCallConfirmed();
            }
        }
        

    }
    
    @Override
    public void onCallCancel(Call call, Message cancel){
        uiRef.appendLog("[RECV]<<< (SIP) "+cancel.toString()+"\n");
        RingPlayer.ins().stopPlay();
        if(callHandler.isClosed()){
            uiRef.appendMsg(String.format("Call canceled by"+cancel.getRemoteAddress()+"...\n"));
            callHandler.ackWithAnswer("");
            //callHandler.hangup();
            callHandler.listen();
            uiRef.enableButton(true, false, false, false);
            if(eventListener != null){
                eventListener.onCallCancel();
            }
        }
    }
    
    @Override
    public void onCallBye(Call call,Message bye){
        uiRef.appendLog("[RECV]<<< (SIP) "+bye.toString()+"\n");
        uiRef.appendMsg(String.format("Call ended by "+bye.getRemoteAddress()+"\n"));
        //callHandler.accept("");
        callHandler.ackWithAnswer("");
        if(callHandler.isClosed()){
            this.closeVoiceChat();

            if(eventListener != null){
                eventListener.onCallBye();
            }
        }
    }
    
    private void initVoiceChat(){
        uiRef.enableButton(false, true, false, false);
        if(eventListener == null){
            if(voiceChat==null){
                voiceChat = new VoiceChat();
            }
            voiceChat.init(recvAddress, rtpPort);
            voiceChat.start();
        }else{
            //remote control, use voice forwarder
            if(voiceForwarder==null){
                voiceForwarder = new VoiceForwarder();
            }
            voiceForwarder.init(remoteAddress, remoteRtpPort, recvAddress, rtpPort);
            voiceForwarder.start();
        }

    }
    
    public void remoteRTPAddress(String addr, int rport){
        remoteRtpPort = rport;
        remoteAddress = addr;
    }
    
    private void closeVoiceChat(){

        
        uiRef.enableButton(true, false, false, false);
        if(eventListener == null){
            voiceChat.close();
        }else{
            //remote control, use voice forwarder
            voiceForwarder.close();
        }
        callHandler.listen();
        System.out.println("Call close");
    }
    
    private void readConfig(){
         Scanner sc=null;
        try {
            //sc = new Scanner(new File("input.txt"));
            sc = new Scanner(new File("config.txt"));
        } catch (FileNotFoundException ex) {
            //Logger.getLogger(SipUA.class.getName()).log(Level.SEVERE, null, ex);
        }
        myPort = sc.nextInt();
        
        try {
            myIpAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            //Logger.getLogger(SipUA.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        mySipURL = new SipURL(myIpAddress,myPort);
        myNameAddress = new NameAddress(mySipURL);
        
        //recvAddress = sc.next();
        //recvPort = sc.nextInt();
        rtpPort = sc.nextInt();
    }
    
    private void initSipProvider(){
        sipProvider = new SipProvider(myIpAddress,myPort){
            @Override
            public synchronized void onSendMessage(Message msg){
                uiRef.appendLog("[SEND]>>> (SIP) "+msg.toString()+"\n");
            }
            
            //@Override
            //public synchronized void onReceivedMessage(Transport transport,Message msg){
            //    uiRef.appendLog("[RECV]<<< "+msg.toString()+"\n");
            //}
            
            
        };
        
        uiRef.appendMsg(String.format("My sip address: %s:%d\n", sipProvider.getViaAddress(), sipProvider.getPort()));
    }
    
    public void initCall(){
        callHandler = new Call(sipProvider,myNameAddress,this);
        callHandler.listen();
        //System.out.printf(" init : %s %s %s\n",callHandler.isActive(),callHandler.isClosed(),callHandler.isIdle());
    }
    
    public void start(){       

        readConfig();
        initSipProvider();
        initCall();
        
        /*if(recvAddress.equals("server")){
            System.out.println("I'm a server");
        }else{
            recvSipURL = new SipURL(recvAddress,recvPort);
            recvNameAddress = new NameAddress(recvSipURL);

            System.out.printf("Call %s:%d\n",recvAddress,recvPort);

            myCall = new Call(sipProvider,myNameAddress,this);
            myCall.call(recvNameAddress);
        }*/
        
    }
    
    public void call(String sipAddress, int port){
        recvAddress = sipAddress;
        recvPort = port;
        recvSipURL = new SipURL(recvAddress,recvPort);
        recvNameAddress = new NameAddress(recvSipURL);
        uiRef.enableButton(false, true, false, false);
        uiRef.appendMsg(String.format("Calling %s:%d...\n",recvAddress,recvPort));
        callHandler.call(recvNameAddress);
        //System.out.printf("%s\n",callHandler.isOutgoing());
        
        
        
        //myCall = new Call();
        //myCall.call(recvNameAddress);

    }
    
    
    
    
    
    public void closeCall(){
        //System.out.printf(" before hang up: %s %s %s\n",callHandler.isActive(),callHandler.isClosed(),callHandler.isIdle());
        if(callHandler.isActive()){
            callHandler.hangup();
            uiRef.appendMsg(String.format("Call ended\n"));
            this.closeVoiceChat();
            
        }else if(callHandler.isOutgoing()){
            RingPlayer.ins().stopPlay();
            uiRef.appendMsg(String.format("Call canceled\n"));
            callHandler.hangup();
            callHandler.listen();
            uiRef.enableButton(true, false, false, false);
        }
        //System.out.printf(" after hang up: %s %s %s\n",callHandler.isActive(),callHandler.isClosed(),callHandler.isIdle());
        
        //System.out.printf(" after listen: %s %s %s\n",callHandler.isActive(),callHandler.isClosed(),callHandler.isIdle());
    }


}
