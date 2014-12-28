/*
 * this is for WebMiddleMan to listen to events
 * future improvement: MainWindow implements this interface instead of passing itselt to sipUA
 */
package sipua;


public interface SipUAEventListener {
    public void onCallAccepted();
    public void onCallBye();
    public void onCallCancel();
    public void onCallConfirmed();
    public void onCallInvite(String remoteAddr);
    public void onCallRefused();
    public void onCallRinging();
}
