
package sipua;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import sun.audio.ContinuousAudioDataStream;

/**
 *
 * Singleton
 */
public class RingPlayer {
    private static RingPlayer ins=null;
    private String filename = "ring.au";
    private ContinuousAudioDataStream audioStream;
    public static RingPlayer ins(){
        if(ins==null){
            ins = new RingPlayer();
        }
        return ins;
    }
    
    public RingPlayer(){
        InputStream instream;
        try {
            instream = new FileInputStream(filename);
            AudioStream audioStream = new AudioStream(instream);
            this.audioStream = new ContinuousAudioDataStream(audioStream.getData());

        } catch (FileNotFoundException ex) {
            Logger.getLogger(RingPlayer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RingPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void startPlay(){
        AudioPlayer.player.start(audioStream);
    }
    
    public void stopPlay(){
        AudioPlayer.player.stop(audioStream);
    }
    
    private class LoginCheckTask extends TimerTask{
        public void run(){
        /*    if(!loginSuccessful){
                onSysMsg("Incorrect login information");
                IRCBot.this.close();
                onLoginFailed();
            }*/
        }
    }
}
