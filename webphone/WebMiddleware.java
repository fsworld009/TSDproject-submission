



public class WebMiddleware implements TcpSocketEventListener {
    private TcpSocket socket;
    private int tcpPort=10002;
    private int rtpPort=10003;
    private String remoteIp;
    WebUI uiRef;
    VoiceChat voiceChat;
    
    int status=0;   //0=idle 1=active
    
    public WebMiddleware(WebUI ref){
        uiRef = ref;
    }
    
    public void start(String rmip){
        remoteIp = rmip;
        socket = new TcpSocket();
        socket.registerEventListener(this);
        socket.connect(remoteIp, tcpPort); //need improve
    }

    @Override
    public void onAccept() {

    }

    @Override
    public void onConnect() {
        uiRef.appendMsg("Connected to remote server\n");
    }

    @Override
    public void onReceive(String msg) {
        uiRef.appendLog("[RECV]<<< "+msg+"\n");
        if(msg.equals("ACCEPTED")){
            uiRef.stopRing();
            uiRef.appendMsg("Your call is accepted\n");
            //start voice chat when you're caller
            initVoiceChat();
            uiRef.appendMsg("Start voice chat...\n");
        }else if(msg.equals("REFUSED")){
            //RingPlayer.ins().stopPlay();
            uiRef.stopRing();
            uiRef.appendMsg("Your call is refused\n");
            uiRef.enableButton(true, false, false, false);
        }else if(msg.equals("BYE")){
            uiRef.appendMsg("Call closed\n");
            closeVoiceChat();
        }else if(msg.equals("CONFIRMED")){
            //start voice chat when you're callee
            uiRef.appendMsg("Start voice chat...\n");
            initVoiceChat();
        }else if(msg.contains("INVITE FROM")){
            String[] split = msg.split("\\s+");
            uiRef.playRing();
            uiRef.appendMsg("You got a call from "+split[2]+"\n");
            uiRef.enableButton(false, false, true, true);
        }else if(msg.equals("CANCELED")){
            uiRef.stopRing();
            uiRef.appendMsg("The call is canceled\n");
            uiRef.enableButton(true, false, false, false);
        }else if(msg.equals("RING")){
            uiRef.playRing();
        }
    }
    
    public void acceptCall(){
        uiRef.stopRing();
        uiRef.enableButton(false, true, false, false);
        sendRaw("ACCEPT");
        uiRef.appendMsg("Accept the call\n");
    }
    
    public void refuseCall(){
        uiRef.stopRing();
        uiRef.enableButton(true, false, false, false);
        sendRaw("REFUSE");
        uiRef.appendMsg("Refuse the call\n");
    }
    
    private void initVoiceChat(){
        uiRef.enableButton(false, true, false, false);
        System.out.println("me");
        if(voiceChat==null){
            voiceChat = new VoiceChat();
        }
        voiceChat.init(remoteIp, rtpPort); //need improve
        voiceChat.start();
        status=1;
    }
    
    public void call(String ip,int port){
        sendRaw(String.format("CALL %s %d",ip,port));
        uiRef.appendMsg("Calling "+String.format("%s:%d",ip,port)+"\n");
        uiRef.enableButton(false, true, false, false);
    }
    
    private void closeVoiceChat(){
        uiRef.enableButton(true, false, false, false);
        voiceChat.close();
        System.out.println("Call close");
        uiRef.appendMsg("Call closed\n");
        status=0;
    }
    
    public void logout(){
        
        sendRaw("LOGOUT");
    }
    
    public void sendRaw(String msg){
        uiRef.appendLog("[SEND]>>> "+msg+"\n");
        socket.send(msg);
    }
    
    public void closeCall(){
        
        if(status==1){
            sendRaw("CLOSE");
            closeVoiceChat();
        }else{
            sendRaw("CANCEL");
            uiRef.stopRing();
            uiRef.appendMsg("Your call is canceled\n");
            uiRef.enableButton(true, false, false, false);
        }
    }
    
}
