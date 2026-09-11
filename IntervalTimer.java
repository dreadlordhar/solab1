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
 public void run(){
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
