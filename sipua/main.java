
import javax.swing.SwingUtilities;
import sipua.MainWindow;


public class main {

    public static void main(String[] args) {
        // TODO code application logic here
        //MainUI sipUI = new MainUI();
        //sipUI.start();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainWindow mainWindow = new MainWindow();
                mainWindow.setVisible(true);
            }
        });
    }
    

}
