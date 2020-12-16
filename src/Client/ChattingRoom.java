package Client;

import javax.swing.*;

// 채팅창 메타데이터 관리
public class ChattingRoom {

	private String chatCode;
	private String name;
	private JPanel panel = null;
	private JFrame frame = null;
	private boolean init = false;
	private JPanel messageArea;
	private JPanel wrap;
	private JScrollPane scrollPanel;
	private int chattingNumber = 0;
	private JLabel updateLabel;
	private String previousTime = null;
	
	public ChattingRoom(String chatCode) {
		this.chatCode = chatCode;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	public void setPanel(JPanel panel) {
		this.panel = panel;
	}
	public JPanel getPanel() {
		return panel;
	}
	
	public void setFrame(JFrame chattingRoomFrame) {
		frame = chattingRoomFrame;
	}
	public JFrame getFrame() {
		return frame;
	}
	
	public void setInitialize(boolean tf) {
		init = tf;
	}
	public boolean isInitialized() {
		return init;
	}
	
	public void setWrap(JPanel wrap) {
		this.wrap = wrap;
	}
	public JPanel getWrap() {
		return wrap;
	}
	
	public void setMessageArea(JPanel panel) {
		messageArea = panel;
	}
	public JPanel getMessageArea() {
		return messageArea;
	}
	
	public void setScrollPanel(JScrollPane scrollPanel) {
		this.scrollPanel = scrollPanel;
	}
	public JScrollPane getScrollPanel() {
		return scrollPanel;
	}
	
	public void setChatNum(int num) {
		chattingNumber = num;
	}
	public void addChatNumOne() {
		chattingNumber++;
	}
	public int getChatNum() {
		return chattingNumber;
	}
	
	public void setUpdateLabel(JLabel label) {
		updateLabel = label;
	}
	public void update(String content) {
		updateLabel.setText(content);
	}
	public JLabel getUpdateLabel() {
		return updateLabel;
	}
	
	public void setTime(String time) {
		previousTime = time;
	}
	public String getTime() {
		return previousTime;
	}
	
	
	
	
}
