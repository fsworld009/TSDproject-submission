About
=====================================
**SIP User Agent Remote Controller (September 2013 - December 2013)**  
*A Java Applet that can remotely control a SIP User Agent over the network to make a call or receive a call*
 - Created a message-oriented protocol between a Java Applet and a Java program
 - Modified Session Initiation Protocol (SIP) to meet project requirements
 - Investigated an open source Java package (MjSip)

Note
=====================================
This was a team project in my telecom software design course  
Not everyone in our team uses git, so I only checked in the codes we submitted 

How to compile and run
=====================================
We only tested the program under JRE 7u45

1. Run compile.bat to generate sipua.jar and Webphone.jar
(or bash compile.bat under Linux)

2. make sure port 9527,10000,10001,10002,10003 are open on all machines

3. two laptops running Sip UA opens sipua.jar 
  ```
  java -jar sipua.jar
  ```
4. the other laptop serves as the client need to change Java settings:
    1. in Java Control Panel, change Safety to Medium
    2. edit (JRE Install Path)/lib/security/java.policy
(note: it's possible to have multiple JREs in one machine so make sure you modify the correct one)
in grant{} section add
    permission javax.sound.sampled.AudioPermission "record";
    permission javax.sound.sampled.AudioPermission "play";
    
5. client opens a web browser, make sure Java is enabled on that browser and open
```
http://(SipUA's ip):9527/login.html
```
enter password 1234 to start remote control

MjSip
=======
We used MjSip, a full SIP protocol implementation in Java, to build our project.  
Find more about MjSip at http://www.mjsip.org/