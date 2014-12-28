
package sipua;


public class WebMiddleware implements TcpSocketEventListener, SipUAEventListener {
    private TcpSocket socket;
    private int tcpPort=10002;
    //private String remoteIp;
    MainWindow uiRef;
    SipUA sipRef;
    
    public WebMiddleware(MainWindow ref,SipUA ua){
        uiRef= ref;
        sipRef = ua;
    }
    public void start(String ip){
        //remoteIp = ip;
        socket = new TcpSocket();
        socket.registerEventListener(this);
        socket.startServer(ip,tcpPort);
    }
    
    public void closeAcceptSocket(){
        socket.closeSocket();
    }

    @Override
    public void onAccept() {
        uiRef.remoteLogin();
    }

    @Override
    public void onConnect() {
        
    }

    @Override
    public void onReceive(String msg) {
        uiRef.appendLog("[RECV]<<< (Web) "+msg+"\n");
        if(msg.contains("CALL")){
            String[] addr = msg.split("\\s+");
            uiRef.call(addr[1],Integer.parseInt(addr[2]));
        }else if (msg.equals("CANCEL")||msg.equals("CLOSE")){
            uiRef.closeCall();
        }else if(msg.equals("LOGOUT")){
            uiRef.remoteLogout();
        }else if(msg.equals("ACCEPT")){
            sipRef.acceptCall();
        }else if(msg.equals("REFUSE")){
            sipRef.refuseCall();
        }
    }
    
    public void closeServer(){
        socket.closeServer();
    }

    @Override
    public void onCallAccepted() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        sendRaw("ACCEPTED");
    }

    @Override
    public void onCallBye() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        sendRaw("BYE");
    }

    @Override
    public void onCallCancel() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        sendRaw("CANCELED");
    }

    @Override
    public void onCallConfirmed() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        sendRaw("CONFIRMED");
    }

    @Override
    public void onCallInvite(String remoteAddr) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        sendRaw("INVITE FROM "+remoteAddr);
    }

    @Override
    public void onCallRefused() {
        //throw new UnsuportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        sendRaw("REFUSED");
    }

    @Override
    public void onCallRinging() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        sendRaw("RING");
    }
    
    public void sendRaw(String msg){
        uiRef.appendLog("[SEND]>>> (Web) "+msg+"\n");
        socket.send(msg);
    }
}
