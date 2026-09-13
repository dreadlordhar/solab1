import javax.swing.*;
import  java.util.Timer;
import java.util.TimerTask;
import java.awt.Toolkit;

class IntervalTimer extends TimerTask{
    int secunde;
    Timer timer;
    IntervalTimer(int secunde) {
        this.secunde = secunde;
    }
  @Override
 public void run(){import java.util.Timer;
import java.util.TimerTask;
import java.awt.Toolkit;
import javax.swing.JOptionPane;

      class IntervalTimer extends TimerTask {

          int secunde;
          Timer timer;

          IntervalTimer(int secunde) {
              this.secunde = secunde;
          }

          @Override
          public void run() {
              Toolkit.getDefaultToolkit().beep();
              JOptionPane.showMessageDialog(
                      null,
                      "Timpul a expirat!",
                      "Timer cu interval",
                      JOptionPane.INFORMATION_MESSAGE
              );

          }

          public void start() {
              timer = new Timer();
              timer.schedule(this, secunde * 1000);
          }

          public void stop() {
              timer.cancel();
          }
      }
      System.out.println("Timpul a expirat!");
      Toolkit.getDefaultToolkit().beep();
  }
public void start(){
timer = new Timer();
timer.schedule(this,secunde *1000);
}
public void stop(){
        timer.cancel();
    }
}
