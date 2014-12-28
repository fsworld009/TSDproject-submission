How to compile and run
=====================================
We only test the program under JRE 7u45

1. Run compile.bat to generate sipua.jar and Webphone.jar
(or bash compile.bat under Linux)

2. make sure port 9527,10000,10001,10002,10003 are open on all machines

3.two laptops running Sip UA opens sipua.jar 
java -jar sipua.jar

4. the other laptop serves as the client need to change Java settings:
4.1 in Java Control Panel, change Safety to Medium
4.2 edit (JRE Install Path)/lib/security/java.policy
(note: it's possible to have multiple JREs in one machine so make sure you modify the correct one)
in grant{} section add
    permission javax.sound.sampled.AudioPermission "record";
    permission javax.sound.sampled.AudioPermission "play";
    
5 client opens a web browser, make sure Java is enabled on that browser and open
http://(SipUA's ip):9527/login.html
enter password 1234 to start remote control
