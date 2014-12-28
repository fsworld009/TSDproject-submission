/*
 * Main GUI
 */


import java.applet.AudioClip;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;


public class WebUI extends JApplet {
    
    private JTextPane logPane;
    private JTextPane msgPane;
    private JTextField inputField;
    private JButton okButton;
    private JButton cancelButton;
    private GUIActionListener listener;
    private int state;  //0=caller, 1=callee
    private WebMiddleware webMiddleMan;
    private String remoteIp;
    private String httpPort;
    private AudioClip ringTone;
    private JButton acceptButton;
    private JButton refuseButton;
    
    public WebUI(){
        //super("Webphone");
        //this.mainWinRef = mainWinRef;
        //init();
        //this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //this.setSize(360,480);
        //this.setResizable(false);
        
        //this.add
       // this.addWindowListener(new closeEventWindowListener());
        
        initComponents();
        try {
            Thread.sleep(20);
        } catch (InterruptedException ex) {
            Logger.getLogger(WebUI.class.getName()).log(Level.SEVERE, null, ex);
        }
{

        }

    }
    
    public void enableButton(final boolean ok, final boolean cancel, final boolean accept, final boolean refuse){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                okButton.setEnabled(ok);
                cancelButton.setEnabled(cancel);
                acceptButton.setEnabled(accept);
                refuseButton.setEnabled(refuse);
            }
        });
    }
    
    public void playRing(){
        ringTone.loop();
    }
    
    public void stopRing(){
        ringTone.stop();
    }
    
    public void init(){
        remoteIp = getParameter("remoteIp");
        httpPort = getParameter("httpPort");
        //RingPlayer.ins().setRemoteIp(remoteIp+":"+httpPort);
        webMiddleMan = new WebMiddleware(this);
        webMiddleMan.start(remoteIp);
        
        ringTone = getAudioClip(getCodeBase(), "ring.au");
    }
    
    /*public void called(final String callerAddr){
        //JDialog dialog = new JDialog(this,"You got a call from"+addr);
        //dialog.set
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int a = JOptionPane.showConfirmDialog(null,"You got a call from "+callerAddr,"New call",JOptionPane.YES_NO_OPTION);
                
                if(a == JOptionPane.YES_OPTION){
                    appendMsg("Accept the call from "+callerAddr+"\n");
                    webMiddleMan.acceptCall();
                }else if(a== JOptionPane.NO_OPTION){
                    appendMsg("Refuse the call from "+callerAddr+"\n");
                    webMiddleMan.refuseCall();
                }else{
                    //JOptionPane.CLOSED_OPTION
                    return;
                }
            }
        });

        
    }*/
    
    private void initComponents(){
        inputField = new JTextField(15);
        msgPane = new JTextPane();
        msgPane.setEditable(false);
        JScrollPane msgScrollPane = new JScrollPane(msgPane);
        //msgScrollPane.setPreferredSize( new Dimension( 360, 200 ) );
        
        logPane = new JTextPane();
        logPane.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logPane);
        okButton = new JButton("Call");
        cancelButton = new JButton("Close");
        cancelButton.setEnabled(false);
        acceptButton = new JButton("Accept");
        acceptButton.setEnabled(false);
        refuseButton = new JButton("Refuse");
        refuseButton.setEnabled(false);
        listener = new GUIActionListener();
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        
        
        
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.ipady = 150;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(msgScrollPane,gbc);
        
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        inputPanel.add(inputField);
        inputPanel.add(okButton);
        inputPanel.add(cancelButton);
        inputPanel.add(acceptButton);
        inputPanel.add(refuseButton);
        
        
        okButton.addActionListener(listener);
        cancelButton.addActionListener(listener);
        acceptButton.addActionListener(listener);
        refuseButton.addActionListener(listener);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weighty = 0.5;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(inputPanel,gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0.5;
        gbc.ipady = 200;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(logScrollPane,gbc);
        
        Font font = new Font("Arial",Font.PLAIN,20);
        logPane.setFont(font);
        msgPane.setFont(font);
        
    }
    
    public void appendLog(final String newLog){     
        //SwingUtilities.invokeLater(new Runnable() {
           // @Override
            //public void run() {
                try {
                    logPane.getDocument().insertString(logPane.getDocument().getLength(), newLog, null);
                } catch (BadLocationException ex) {
                    System.err.println("BadLocationException");
                }
          //  }
        //});
    }
    
    public void appendMsg(final String newLog){     
        //SwingUtilities.invokeLater(new Runnable() {
            //@Override
            //public void run() {
                try {
                    msgPane.getDocument().insertString(msgPane.getDocument().getLength(), newLog, null);
                } catch (BadLocationException ex) {
                    System.err.println("BadLocationException");
                }
           // }
       // });
    }
    
    //private class closeEventWindowListener extends WindowAdapter{
    //    @Override
    //    public void windowClosing(WindowEvent e) {
    //        webMiddleMan.logout();
    //    }
    //}
    
    @Override
    public void stop(){
        webMiddleMan.logout();
    }
    
    private class GUIActionListener implements ActionListener{
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == WebUI.this.okButton){
                    String[] sipAddrs = inputField.getText().split("\\s+");
                    if(sipAddrs.length==2){
                        //sipUA.call(sipAddrs[0],Integer.parseInt(sipAddrs[1]));
                        webMiddleMan.call(sipAddrs[0],Integer.parseInt(sipAddrs[1]));
                    }else{
                        appendMsg("Input format error");
                    }
                }else if(e.getSource() == WebUI.this.cancelButton){
                    //sipUA.closeCall();
                    webMiddleMan.closeCall();
                }else if(e.getSource() == WebUI.this.acceptButton){
                    webMiddleMan.acceptCall();
                }else if(e.getSource() == WebUI.this.refuseButton){
                    webMiddleMan.refuseCall();
                }
            }
    }
    

}
