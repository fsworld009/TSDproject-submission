    //A Simple Web Server (WebServer.java)
package webserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
    import com.sun.net.httpserver.HttpServer;
import java.io.BufferedInputStream;
    import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
    import java.io.InputStreamReader;
    import java.io.PrintWriter;
    import java.net.ServerSocket;
    import java.net.Socket;
    import java.io.FileReader;
    import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
    import java.net.InetSocketAddress;
import java.util.Scanner;
    import java.util.logging.Level;
    import java.util.logging.Logger;
import sipua.MainWindow;

    public class WebServer {

      /* WebServer constructor. */

      //ServerSocket serverSocket;  
        private HttpServer httpServer;
        private static int httpPort = 9527;
        //private VoiceWarper voiceWarper;
        private MainWindow uiRef;
        private String password="1234";
        
        
        public WebServer(MainWindow ref){
            uiRef = ref;
        }
        
        public void init(){
              try {
                  /*try {
                      serverSocket = new ServerSocket(httpPort);
                  } catch (IOException ex) {
                      Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
                  }*/
                  httpServer = HttpServer.create(new InetSocketAddress(httpPort), 0);
              } catch (IOException ex) {
                  Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
              }
              System.out.println("Webserver binds to port 9527");
              //createContent();
        }  

        private void createContent(){
            httpServer.createContext("/login.html", new Login());
            httpServer.createContext("/login_check.html", new Login_check());
            httpServer.createContext("/recorder.js", new RecorderJS());
            httpServer.createContext("/recorderWorker.js", new RecorderWorkerJS());
            httpServer.createContext("/WebUI.class", new WebSip());
            httpServer.createContext("/Webphone.jar", new WebSipJar());
            httpServer.createContext("/ipinfo", new IpInfo());
            httpServer.createContext("/ring.au", new RingWav());
            //httpServer.createContext("/WebSip$1.class", new WebSip1());
            //httpServer.createContext("/WebSip$MicThread.class", new WebSipMicThread());

        }
        
        public void start(){
            init();
            createContent();
            httpServer.start();
            
            //temp
            //voiceWarper = new VoiceWarper();
            //voiceWarper.init("192.168.2.2", 10002);
            //voiceWarper.start();
            
        }
        
        private static void WriteHTML(String filepath, HttpExchange t) throws IOException{
            OutputStream os = t.getResponseBody();
            //InputStream is = new InputStream(new FileReader("index.html"));
            File webpage = new File(filepath);
            //FileReader webpageReader = new FileReader(webpage);
            //BufferedReader br = new BufferedReader(webpageReader);
            Scanner sc = new Scanner(webpage);

            

            String line;
            
            String param="";
            
            
            if(filepath.equals("Webphone.html")){
                param="<param name=\"remoteIp\" value=\""+InetAddress.getLocalHost().getHostAddress()+"\"/>";
                param+="<param name=\"httpPort\" value=\""+String.format("%d",httpPort)+"\"/>";
            }
            
            t.sendResponseHeaders(200, webpage.length()+param.length());
            int count=0;
            while(sc.hasNext())
            {
                line = sc.nextLine();
                count++;

                os.write(line.getBytes());
                //System.out.println(line);
                if(count==11){  //for Webphone.html
                    os.write(param.getBytes());
                    //System.out.println(param);
                }
                os.write("\r\n".getBytes());
            }

            //os.write(os.getBytes());
            os.close();
            sc.close();
            //webpageReader.close();
        }
        
        private static void WriteJS(String filepath, HttpExchange t) throws IOException{
            OutputStream os = t.getResponseBody();
            //InputStream is = new InputStream(new FileReader("index.html"));
            File webpage = new File(filepath);
            //FileReader webpageReader = new FileReader(webpage);
            //BufferedReader br = new BufferedReader(webpageReader);
            Scanner sc = new Scanner(webpage);

            t.sendResponseHeaders(200, webpage.length());

            String line;

            while(sc.hasNext())
            {
                line = sc.nextLine();
                os.write(line.getBytes());
                os.write("\n".getBytes());
            }

            //os.write(os.getBytes());
            os.close();
            sc.close();
            //webpageReader.close();
        }
        
        private static void WriteRaw(String filepath, HttpExchange t) throws IOException{
                  // add the required response header for a PDF file
            Headers h = t.getResponseHeaders();
            h.add("Content-Type", "application/java-archive");

            // a PDF (you provide your own!)
            File file = new File (filepath);
            byte [] bytearray  = new byte [(int)file.length()];
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(bytearray, 0, bytearray.length);

            // ok, we are ready to send the response.
            t.sendResponseHeaders(200, file.length());
            OutputStream os = t.getResponseBody();
            os.write(bytearray,0,bytearray.length);
            os.close();
            fis.close();
        }
        
        private static class Login implements HttpHandler {
            public void handle(HttpExchange t) throws IOException {
              WebServer.WriteHTML("login.html", t);

                
            }
        }

        private static class IpInfo implements HttpHandler {
            public void handle(HttpExchange t) throws IOException {
                InetAddress addr = InetAddress.getLocalHost();
                String send = addr.getHostAddress();
                OutputStream os = t.getResponseBody();
                t.sendResponseHeaders(200, send.length());
                os.write(send.getBytes());
                os.close();
            }
        }
        
        private class Login_check implements HttpHandler {
            public void handle(HttpExchange t) throws IOException {
              //String response = "Test HttpServer";
                
                InputStreamReader is = new InputStreamReader(t.getRequestBody(),"utf-8");
                BufferedReader reader = new BufferedReader(is);
                String post = reader.readLine();
                System.out.println(post);
                String password="";
                if(post != null){
                    String[] spl1 = post.split("&");
                    String[] spl2 = spl1[0].split("=");
                    //System.out.println(spl2[1]);
                    password = spl2[1];
                }
                if(password.equals(WebServer.this.password)){
                    System.out.println("login succeed");
                    uiRef.setRemoteIp(t.getRemoteAddress().getAddress().getHostAddress());
                    WebServer.WriteHTML("Webphone.html", t);  //temp
                    //
                    System.out.println("login succeed");
                }else{
                    WebServer.WriteHTML("login_failed.html", t);
                }
                
            }
        }
        
        private static class RecorderJS implements HttpHandler {
            public void handle(HttpExchange t) throws IOException {
              //String response = "Test HttpServer";
                WebServer.WriteJS("recorder.js", t);
            }
        }
        
        private static class RecorderWorkerJS implements HttpHandler {
            public void handle(HttpExchange t) throws IOException {
              //String response = "Test HttpServer";
                WebServer.WriteJS("recorderWorker.js", t);
            }
        }
        
        private static class WebSip implements HttpHandler {
            public void handle(HttpExchange t) throws IOException {
              //String response = "Test HttpServer";
                WebServer.WriteRaw("WebUI.class", t);
            }
        }
        
        private static class RingWav implements HttpHandler {
            public void handle(HttpExchange t) throws IOException {
              //String response = "Test HttpServer";
                WebServer.WriteRaw("ring.au", t);
            }
        }
        
        private static class WebSipJar implements HttpHandler {
            public void handle(HttpExchange t) throws IOException {
              //String response = "Test HttpServer";
                WebServer.WriteRaw("Webphone.jar", t);
            }
        }
        
        private static class WebSip1 implements HttpHandler {
            public void handle(HttpExchange t) throws IOException {
              //String response = "Test HttpServer";
                WebServer.WriteRaw("WebSip$1.class", t);
            }
        }
        
        private static class WebSipMicThread implements HttpHandler {
            public void handle(HttpExchange t) throws IOException {
              //String response = "Test HttpServer";
                WebServer.WriteRaw("WebSip$MicThread.class", t);
            }
        }

      /*
      protected void start() {
        System.out.println("Waiting for connection");
        while (true) {
          try {
            // wait for a connection
            Socket remote = serverSocket.accept();
            // remote is now the connected socket
            System.out.println("Connection, sending data.");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                remote.getInputStream()));
            PrintWriter out = new PrintWriter(remote.getOutputStream());

            // read the data sent. We basically ignore it,
            // stop reading once a blank line is hit. This
            // blank line signals the end of the client HTTP
            // headers.
            String str = ".";
            while (!str.equals(""))
              str = in.readLine();

            // Send the response
            // Send the headers
            out.println("HTTP/1.0 200 OK");
            out.println("Content-Type: text/html");
            out.println("Server: Bot");
            // this blank line signals the end of the headers
            out.println("");

            // Send the HTML page
            BufferedReader n = new BufferedReader(new FileReader("index.html"));
            String line;
            while((line = n.readLine()) != null)
            {
                out.println(line);
            }

            out.flush();
            remote.close();
          } catch (Exception e) {
            System.out.println("Error: " + e);
          }
        }
      }*/

      /* Start the application. Command line parameters are not used. */
      /*public static void main(String args[]) {
        WebServer ws = new WebServer();
        ws.start();
      }*/
        
        
    }