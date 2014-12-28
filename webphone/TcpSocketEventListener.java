


public interface TcpSocketEventListener {
    public void onAccept();
    public void onConnect();
    public void onReceive(String msg);
}
