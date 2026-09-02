import javax.swing.*;
import java.awt.*;

public class RoundedProgressBar extends JProgressBar {

    public RoundedProgressBar(int min, int max) {
        super(min, max);

        // Setări pentru bara personalizată
        setOpaque(false);
        setBorderPainted(false);
        setStringPainted(true);

        setForeground(new Color(80, 160, 255));
        setBackground(new Color(40, 50, 80));
    }

    @Override
    protected void paintComponent(Graphics g) {

        // Activăm grafica 2D
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int width = getWidth();
        int height = getHeight();

        int arc = height;

        // Desenăm fundalul barei
        g2.setColor(getBackground());

        g2.fillRoundRect(
                0,
                0,
                width,
                height,
                arc,
                arc
        );

        // Calculăm lățimea progresului
        int progressWidth =
                (int) (width * getPercentComplete());

        // Desenăm progresul
        if (progressWidth > 0) {

            g2.setColor(getForeground());

            g2.fillRoundRect(
                    0,
                    0,
                    progressWidth,
                    height,
                    arc,
                    arc
            );
        }

        // Afișăm procentul
        if (isStringPainted()) {

            String text = getString();

            FontMetrics fm = g2.getFontMetrics();

            int textWidth =
                    fm.stringWidth(text);

            int textHeight =
                    fm.getAscent();

            int x =
                    (width - textWidth) / 2;

            int y =
                    (height + textHeight) / 2 - 2;

            g2.setColor(new Color(70, 120, 200));

            g2.drawString(
                    text,
                    x,
                    y
            );
        }

        g2.dispose();
    }
}