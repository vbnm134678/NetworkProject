package Client;

import javax.swing.*;


// 친구 정보창
public class friendInfoPanel extends JFrame {
	
	private String friendID;
	
	public friendInfoPanel(String friendName, String friendID, ImageIcon icon, int x, int y) {
		this.friendID = friendID;
		
		this.setBounds(x, y, 100, 100);
		this.setTitle(friendID + " 정보");
	}
	
	
	
	
	
	
	

}
