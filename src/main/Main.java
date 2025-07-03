package main;

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
            frame.setVisible(true);
        });
    }
	
	
	
	
	

}
