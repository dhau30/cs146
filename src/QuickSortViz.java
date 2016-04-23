// Notes:
// It was tricky to get the algorithm to display one step at a time. I ended up running the
// alg from the beginnign to the desired (goal) step each time. 
// In this setup, the entire thing is redrawn every time, so you can't just add the last thing.
// I think the QuickSorter inner class was a good idea.
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.Point;
import java.awt.Font;

import javax.imageio.ImageIO;

import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.util.*;

/**
 * Creates a window with two text fields, two buttons, and a large
 * display area. The app then tracks the mouse over the display area
 * and follows the mouse with a circle.
**/
public class QuickSortViz extends JFrame {

	/** holds the drawing canvas **/
	private QuickSortVizPanel canvas;
	
	/** height of the main drawing window **/
	int height;
	/** width of the main drawing window **/
	int width;

    // All these fields control the state of the simulation
    private int state = 0;
    private QuickSorter quickSorter;
    private boolean runMode = false;
    private boolean doStep = false;
    private int updateStep = 0;
    private int skipSteps = 50;

	/**
		 * Creates the main window
		 * @param height the height of the window in pixels
		 * @param width the width of the window in pixels
		 **/		 
	public QuickSortViz( int height, int width ) {
		super("QuickSortViz Display");
		this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);

		this.canvas = new QuickSortVizPanel( height, width );
		this.add( this.canvas, BorderLayout.CENTER );
		this.pack();
		this.setVisible( true );

		//this.fieldX = new JLabel("X");
		//this.fieldY = new JLabel("Y");
		JLabel skipLabel = new JLabel( "Speed" );
        JSlider skipSlider = new JSlider( 1, 100, this.skipSteps );
        Hashtable<Integer,JLabel> labels = new Hashtable<Integer,JLabel>();
        labels.put( 2, new JLabel("Fast") );
        labels.put( 99, new JLabel("Slow") );
        skipSlider.setLabelTable( labels );
        skipSlider.setPaintLabels(true);
		JButton stepButton = new JButton("Step");
		JButton runButton = new JButton("Run");
		JButton resetButton = new JButton("Reset Random");
		JButton quit = new JButton("Quit");
		JPanel panel = new JPanel( new FlowLayout(FlowLayout.RIGHT));
		//panel.add( this.fieldX );
		//panel.add( this.fieldY );
		panel.add( skipLabel );
		panel.add( skipSlider );
		panel.add( stepButton );
		panel.add( runButton );
		panel.add( resetButton );
		panel.add( quit );
		
		this.add( panel, BorderLayout.SOUTH);
		this.pack();

		Control control = new Control();
		SliderChangeListener sliderChangeListener = new SliderChangeListener();
		this.addKeyListener(control);
		this.setFocusable(true);
		this.requestFocus();

        skipSlider.addChangeListener( sliderChangeListener );
		stepButton.addActionListener( control );
		runButton.addActionListener( control );
		resetButton.addActionListener( control );
		quit.addActionListener( control );
		
		this.width = width;
		this.height = height;
        this.quickSorter = new QuickSorter();
	}
	
	// --------------------------------------------------------------------------------
	// QuickSorter inner class
	    
	private class QuickSorter {
        private int[] orig_array;
        private int goalStep;
        private int curStep;
        private boolean doneSorting;
        
        public QuickSorter() {
            this.reset();
        }
        
        public void reset() {
            this.orig_array = new int[8];
            this.orig_array[0] = 1;
            this.orig_array[1] = 6;
            this.orig_array[2] = 10;
            this.orig_array[3] = 3;
            this.orig_array[4] = 12;
            this.orig_array[5] = 9;
            this.orig_array[6] = 4;
            this.orig_array[7] = 7;
            this.curStep = -1;
            this.goalStep = 0;
            this.doneSorting = false;
        }

        public void resetRandom() {
            this.orig_array = new int[8];
            Random r = new Random();
            for (int i = 0; i < this.orig_array.length; i++)
                this.orig_array[i] = r.nextInt( 100 );
            this.curStep = -1;
            this.goalStep = 0;
            this.doneSorting = false;
        }

        public String intArrayToString( int[] a ) {
            String ret = "[";
            if (a.length >= 1)
                ret += Integer.toString(a[0]);
            for (int i = 1; i < a.length; i++)
                ret += "," + Integer.toString(a[i]);
            ret += "]";
            return ret;
        }
        
        private void swapIntegers( int[] a, int i, int j ) {
            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
    
        // draw individual boxes with each element in a box
        // the cells a[start] .. a[stop] are the "active" cells and should
        // have a gray background
        private void drawArray(int[] a, int start, int stop, int pivot, int i_arrow, int j_arrow, Graphics g) {
            int x = 80; // left of first box
            int y = 100; // top of all boxes
            int w = 40; // of each box
            int h = 40; // of each box
            g.setFont( new Font( "Arial", Font.PLAIN, 20 ) );
            for (int i = 0; i < a.length; i++) {
                if (start <= i && i <= stop)
                    g.setColor( Color.LIGHT_GRAY );
                else
                    g.setColor( Color.WHITE );
                g.fillRect( x + i*w, y-h, w, h );
                if (start <= i && i <= stop)
                    g.setColor( Color.BLUE );
                else
                    g.setColor( Color.black );
                g.drawRect( x + i*w, y-h, w, h );
                if (a[i] == pivot)
                    g.setColor( Color.red );
                else
                    g.setColor( Color.black );
                g.drawString( Integer.toString(a[i]), x + i*w + 10, y - 10 );
            }
            g.setColor( Color.black );
            if (i_arrow >= 0 && j_arrow >= 0 && i_arrow == j_arrow) {
                g.drawString( "ij", x + i_arrow*w + w/2, y-h-20 );
            }
            else {
                if (i_arrow >= 0) {
                    g.drawString( "i", x + i_arrow*w + w/2, y-h-20 );
                }                
                if (j_arrow >= 0) {
                    g.drawString( "j", x + j_arrow*w + w/2, y-h-20 );
                } 
            }
        }
                
        private void drawBox(int[] a, int start, int end, int level, Graphics g, boolean atEnd) {
            int x = 80+start*40;
            int y = 160 + level * 40;
            int w = (end-start+1)*40;
            int h = 40;
            g.setColor( Color.LIGHT_GRAY );
            g.fillRect(x, y-h, w-3, h-5 ); // x y w h
            if (!atEnd)
                g.setColor( Color.BLUE );
            g.drawRect(x, y-h, w-3, h-5 ); // x y w h
        }
        
        // sorts (in place) a[start]..a[end]
        // returns true if aborted.
        private boolean quickSort(int[] a, int start, int stop, int level, Graphics g) {
            this.curStep++;
            if (this.curStep == this.goalStep) {
                drawArray( a, start, stop, -1, -1, -1, g );
                return true; // we need to stop early, because we reached the step we were going to.
            }

            drawBox( a, start, stop, level, g, false );            
            int pivot = a[(start+stop)/2];
            int i = start;
            int j = stop;
            do {
                this.curStep++;
                if (this.curStep == this.goalStep) {
                    drawArray( a, start, stop, pivot, i, j, g );
                    return true;
                }                    
                while (a[i] < pivot) {
                    i++;
                    this.curStep++;
                    if (this.curStep == this.goalStep) {
                        drawArray( a, start, stop, pivot, i, j, g );
                        return true;
                  }                    
                }
                while (pivot < a[j]) {
                    j--;
                    this.curStep++;
                    if (this.curStep == this.goalStep) {
                        drawArray( a, start, stop, pivot, i, j, g );
                        return true;
                    }                    
                }
                if (i <= j) {   //swap a[i] and a[j]
                    swapIntegers(a, i, j);
                    this.curStep++;
                    if (this.curStep == this.goalStep) {
                        drawArray( a, start, stop, pivot, i, j, g );
                        return true;
                    }                    
                    i++;
                    j--;
                    this.curStep++;
                    if (this.curStep == this.goalStep) {
                        drawArray( a, start, stop, pivot, i, j, g );
                        return true;
                    }                    
                }
            } while (i <= j);
            boolean aborted = false;
            if (start < j)
              aborted = quickSort(a, start, j, level+1, g);
            if (!aborted && i < stop)
              aborted = quickSort(a, i, stop, level+1, g);
            if (!aborted)
                drawBox( a, start, stop, level, g, true );
            return aborted;
        }
        
        public void quickSortStep(Graphics g) {
            this.curStep = -1;
            int[] a = new int[this.orig_array.length];
            System.arraycopy(this.orig_array, 0, a, 0, this.orig_array.length);
            boolean aborted = quickSort(a, 0, a.length-1, 0, g);
            if (!aborted)
                this.doneSorting = true;
        }
    } // end class QuickSorter
    // --------------------------------------------------------------------------------

	public void update() {
			if (this.quickSorter.doneSorting)
			    this.runMode = false;
			if (this.doStep) {
                this.quickSorter.goalStep++;
                this.doStep = false;
    			this.repaint();
    			return;
			}
	        this.updateStep = (this.updateStep+1) % this.skipSteps;
	        if (this.updateStep != 0) 
	            return;
			if (this.runMode && !this.quickSorter.doneSorting)
			    this.quickSorter.goalStep++;
			this.repaint();
	}

	private class QuickSortVizPanel extends JPanel {
		
		/**
		 * Creates the drawing canvas
		 * @param height the height of the panel in pixels
		 * @param width the width of the panel in pixels
		 **/
		public QuickSortVizPanel(int height, int width) {
			super();
			this.setPreferredSize( new Dimension( width, height ) );
			this.setBackground(Color.white);
		}

		/**
		 * Method overridden from JComponent that is responsible for
		 * drawing components on the screen.  The supplied Graphics
		 * object is used to draw.
		 * 
		 * @param g		the Graphics object used for drawing
		 */
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

            if (quickSorter == null)
                return;

			// do whatever drawing is necessary
            quickSorter.quickSortStep(g);
            			
		}
	} // end class QuickSortVizPanel

    private class Control extends KeyAdapter implements ActionListener {

        public void keyTyped(KeyEvent e) {
            if( ("" + e.getKeyChar()).equalsIgnoreCase("q") ) {
                state = 1;
            }
        }

        public void actionPerformed(ActionEvent event) {
            if( event.getActionCommand().equalsIgnoreCase("Run") ) {
                runMode = true;
            }
            if( event.getActionCommand().equalsIgnoreCase("Reset Random") ) {
                quickSorter.resetRandom();
            }
            if( event.getActionCommand().equalsIgnoreCase("Step") ) {
                doStep = true;
            }
            else if( event.getActionCommand().equalsIgnoreCase("Quit") ) {
                state = 1;
            }
        }
    } // end class Control

    private class SliderChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSlider slider = (JSlider)e.getSource();
            skipSteps = slider.getValue();
        }
    }

	public static void main(String[] argv) throws InterruptedException {
		QuickSortViz w = new QuickSortViz( 300, 470 );
		while(w.state == 0) {
			w.update();
			Thread.sleep(33);
		}
		w.dispose();
	}
	
} // end class QuickSortViz
