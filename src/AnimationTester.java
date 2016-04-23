
     

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JComponent;
import javax.swing.JFrame;


public class AnimationTester {
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    ArrayComponent panel = new ArrayComponent();
    frame.add(panel, BorderLayout.CENTER);

    frame.setSize(800, 300);
    frame.setVisible(true);

    Double[] values = new Double[100];
    for (int i = 0; i < values.length; i++)
      values[i] = Math.random() * panel.getHeight();

    final Sorter sorter = new Sorter(values, panel);

    Thread sorterThread = new Thread(sorter);
    sorterThread.start();
  }
}

class ArrayComponent extends JComponent {

  private Double marked1;

  private Double marked2;

  private Double[] values;
  public synchronized void paintComponent(Graphics g) {
    if (values == null)
      return;
    Graphics2D g2 = (Graphics2D) g;
    int width = getWidth() / values.length;
    for (int i = 0; i < values.length; i++) {
      Double v = values[i];
      Rectangle2D bar = new Rectangle2D.Double(width * i, 0, width, v);
      if (v == marked1 || v == marked2)
        g2.fill(bar);
      else
        g2.draw(bar);
    }
  }

  public synchronized void setValues(Double[] values, Double marked1, Double marked2) {
    this.values = (Double[]) values.clone();
    this.marked1 = marked1;
    this.marked2 = marked2;
    repaint();
  }

}

class Sorter implements Runnable {

  private Double[] values;

  private ArrayComponent panel;

  public Sorter(Double[] values, ArrayComponent panel) {
    this.values = values;
    this.panel = panel;
  }

  public void run() {
    Comparator<Double> comp = new Comparator<Double>() {
      public int compare(Double d1, Double d2) {
        try {
            Thread.sleep(100);
        } catch (Exception exception) {
          System.out.println(exception);
        }
        panel.setValues(values, d1, d2);
        return d1.compareTo(d2);
      }
    };
    Collections.sort(Arrays.asList(values), comp);
    panel.setValues(values, null, null);
  }
}
