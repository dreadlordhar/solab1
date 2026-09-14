package circlecatcher.gui;

import javax.swing.JPanel;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class CirclePanel extends JPanel {
    public final double x, y, diam;
    public final int color;

    public CirclePanel(double x, double y, double diameter, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
        diam = diameter;

        setBounds((int)x, (int)y, (int)diam, (int)diam);
        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                double centerX = diam / 2;
                double centerY = diam / 2;

                double dx = e.getX() - centerX;
                double dy = e.getY() - centerY;

                if (dx * dx + dy * dy <= (diam / 2) * (diam / 2)) {
                    // TODO: Make stuff work, now it just destroys itself
                    destroy();
                }
            }
        });
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Shape circle = new Ellipse2D.Double(0, 0, diam, diam);

            g2.setPaint(new Color(this.color));
            g2.fill(circle);
            g2.setPaint(new Color(color));
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(circle);
        } finally {
            g2.dispose();
        }
    }

    private void destroy() {
        Container parent = getParent();

        if (parent != null) {
            parent.remove(this);
            parent.repaint();
        }
    }
}
