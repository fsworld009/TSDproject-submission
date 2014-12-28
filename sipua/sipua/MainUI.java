/*
 * 
 * user interface
 */
package sipua;

import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MainUI{
    private SipUA sipUA=null;
    private Scanner userInput = new Scanner(System.in);
    
    //LinkedBlockingQueue<String> inputQueue;
    
    //boolean threadRunning = false;
    
    //private InputThread inputThread;
    
    /*private class InputThread extends Thread{
        @Override
        public void run(){
            String buffer;
            while(threadRunning){
                buffer = userInput.next();
                synchronized(inputQueue){
                    inputQueue.add(buffer);
                    //System.out.println(buffer);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }*/
    
    
    private char fetchChar(){
        synchronized(userInput){
            return userInput.nextLine().charAt(0);
        }
    }

    private String fetchString(){
        synchronized(userInput){
            return userInput.nextLine();
        }
    }
    
    public void start(){
        
        if(sipUA==null){
            //sipUA = new SipUA(this);
            sipUA.start();
        }
        
        //init input thread
        /*inputQueue = new LinkedBlockingQueue<String>();
        threadRunning = true;
        inputThread = new InputThread();
        inputThread.start();*/
        run();
    }
    
    public void run(){
        //char option;
        System.out.println("SIPUA");
        System.out.println("c: Call");
        System.out.println("q: Quit");
        while(true){
            if(fetchChar()=='c'){
                call();
            }else if(fetchChar()=='q'){
                quit();
            }
        }
        
    }
    
    public void call(){
        System.out.println("Enter SIP Address (IP Port)");
        String sipAddr;
        sipAddr = fetchString();
        String[] sipAddrs = sipAddr.split("\\s+");
        //System.out.printf("%s %s\n",sipAddrs[0],sipAddrs[1]);
        sipUA.call(sipAddrs[0],Integer.parseInt(sipAddrs[1]));
        
        
        return;
    }
    
    public boolean called(String addr){
        System.out.printf("A call from %s\n",addr);
        System.out.printf("y=answer n=deny\n");
        char option;
        while(true){
            option = fetchChar();
            if(option=='y'){
                return true;
            }else if(option=='n'){
                return false;
            }
        }
        
    }
    
    public void quit(){
        System.out.println("Quit");
        return;
    }
}
