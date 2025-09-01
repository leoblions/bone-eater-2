package main;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
	
	public static void main(String[] args) {
		var game = new Game();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Bone Eater II");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            frame.setContentPane(game);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    // This method is called whenever the JFrame is resized
                    JFrame sourceFrame = (JFrame) e.getSource();
                    game.width = sourceFrame.getWidth();
                    game.height = sourceFrame.getHeight();
                    System.out.println("Frame resized to: " + game.width + "x" + game.height);
                    game.resizeWindow();
                    // Add your custom logic here to handle the resize,
                    // e.g., adjusting component sizes or layouts
                }
            });
            frame.setVisible(true);
        });
    }
	
	
	
	
	

}
