package Client;

import java.io.Serializable;
import java.util.HashSet;

import javax.swing.*;

// 친구 메타데이터 관리
public class Friend implements Serializable {

	private String name;
	private String id;
	private String state = null;
	private JLabel stateLabel;
	private String last;
	private ImageIcon icon = null;
	private HashSet<RoundPanel> profilePanels = new HashSet<RoundPanel>();
	private JPanel panel;
	private JPanel space;
	private JPanel[] fpWrapper;
	private JPanel selectPanel;
	private JPanel selectSpace;
	
	public Friend(String friendID) {
		id = friendID;
	}

	public void setName(String friendName) {
		name = friendName;
	}
	public String getName() {
		return name;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	public String getState() {
		return state;
	}
	
	public void setStateLabel(JLabel label) {
		stateLabel = label;
	}
	public JLabel getStateLabel() {
		return stateLabel;
	}
	
	public void setLast(String lastConnect) {
		last = lastConnect;
	}
	public String getLast() {
		return last;
	}
	
	public void setIcon(ImageIcon profileIcon) {
		icon = profileIcon;
	}
	public ImageIcon getIcon() {
		return icon;
	}
	
	public void addProfilePanel(RoundPanel p) {
		profilePanels.add(p);
	}
	public void remveProfilePanel(RoundPanel p) {
		profilePanels.remove(p);
	}
	public HashSet<RoundPanel> getProfilePanels() {
		return profilePanels;
	}
	
	public void setPanel(JPanel friendPanel) {
		panel = friendPanel;
	}
	public JPanel getPanel() {
		return panel;
	}
	
	public void setSpace(JPanel friendSpace) {
		space = friendSpace;
	}
	public JPanel getSpace() {
		return space;
	}
	
	public void setFpWrapper(JPanel[] fpWrapper) {
		this.fpWrapper = fpWrapper;
	}
	public JPanel[] getFpWrapper() {
		return fpWrapper;
	}
	
	public void setSelectPanel(JPanel selectPanel) {
		this.selectPanel = selectPanel;
	}
	public JPanel getSelectPanel() {
		return selectPanel;
	}
	
	public void setSelectSpace(JPanel selectSpace) {
		this.selectSpace = selectSpace;
	}
	public JPanel getSelectSpace() {
		return selectSpace;
	}
	
}
