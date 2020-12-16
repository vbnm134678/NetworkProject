package Client;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.google.gson.*;

public class ChatterUI {

	// 서버와 연결된 소켓
	private Socket server = null;
	// 서버와 연결된 출력 스트림
	private BufferedWriter out = null;

	// 여러가지 자료구조 모아놓는 클래스
	public Collections data = new Collections();

	// 내 이름
	private String myName;
	// 내 아이디
	private String myID;
	// 내 상태 메세지
	private String state;
	// 파일 다운 디렉토리
	private String downPath = "C:/Users/" + System.getProperty("user.name") + "/Documents/채터 다운로드";
	// 프로그램 메인 창
	private JFrame frame = new JFrame("Chatter");

	// 카드 페이지들을 넘기게 해주는 버튼을 넣는 패널
	private JPanel buttonPanel = new JPanel();

	// 친구, 채팅방, 공공 데이터 패널들을 포함하는 패널
	private CardLayout cl = new CardLayout();
	private JPanel cards = new JPanel(cl);

	// 내 정보를 표시하는 패널
	private JPanel myPanel = new JPanel();
	// 내 상태 메세지 패널
	private JLabel stateMessage = new JLabel();

	// 내 프로필 이미지
	private HashSet<RoundPanel> myProfiles = new HashSet<RoundPanel>();
	private ImageIcon myProfileIcon;

	// 친구들 리스트를 표시하는 패널
	private JPanel friendPanel = new JPanel();
	// 친구 추가 기능을 위한 패널
	private JPanel addFriendPanel = new JPanel();
	// 친구 수 표시해주는 라벨
	private JLabel numFriendLabel = new JLabel();

	// 내가 속한 채팅방 리스트 표시 (채팅방 참여자 선택용)
	private JPanel chattingRoomPanel = new JPanel();

	// 다른 채팅방엔 초기화 전엔 못들어가
	private boolean isClickable = true;

	// 채팅방 생성 때 참가자 선택하는 패널
	private JPanel selectFriendListPanel = new JPanel();

	// 채팅방 메인 프레임
	private JFrame chatRoomFrame;
	// 채팅방 테마 색깔
	private Color skyBlue = new Color(180, 200, 215);
	// 버튼 색깔
	private Color pressed = new Color(242, 242, 242);
	private Color onButton = new Color(247, 247, 247);

	// 공공데이터를 사용하는 패널
	private JPanel publicDataPanel = new JPanel();
	// 설정창 패널
	private JPanel optionPanel = new JPanel();
	// 토탈 채팅 수 (업데이트용)
	private JLabel totalChatNum = new JLabel();

	// 친구 서치 창
	private UserSearchWindow searchFriendFrame = null;

	// 스크롤을 자동으로 숨기기를 위한 타이머
	private Timer Timer;
	private Gson JSON = new Gson();

	/* UI 테스트용 메인 메소드 */
	public static void main(String[] args) throws IOException {
		ChatterUI c = new ChatterUI(null);
		ImageIcon icon = new ImageIcon("resources/MainIcon.png");
		c.addMyPanel("My Name", "myID", icon, "고양이 좋아", "online"); // 나
		c.addNewChatRoomPanel("dragon0414=1", "채팅방1"); // 채팅방
		c.addNewChatRoomPanel("dragon0414=2", "채팅방2");
		c.addNewChatRoomPanel("dragon0414=3", "채팅방3");
	}

	/* 생성자 */
	public ChatterUI(Socket socket) throws IOException {
		// 서버와 연결된 소켓
		server = socket;
		// 서버와 연결된 입력 스트림
		if (server != null)
			out = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));

		// 메인 패널은 박스 레이아웃
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		frame.getContentPane().setBackground(Color.WHITE);
		// 아이콘 설정
		ImageIcon icon = new ImageIcon("resources/MainIcon.png");
		Image image = icon.getImage().getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH);
		frame.setIconImage(image);
		// 최소 크기 고정
		frame.setMinimumSize(new Dimension(420, 650));
		frame.setBounds(800, 150, 420, 550);

		// 각 카드 패널들을 초기화 시킴
		initCardPanels();
		// 카드 넘기는 버튼들을 초기화시킴
		initButtonPanel();

		// 프로그램 끄면 메모리상에서 제거
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/* Friend Panel에 내 정보 추가 */
	public void addMyPanel(String myID, String myName, ImageIcon icon, String state, String last) {
		// 내 이름
		this.myName = myName;
		// 내 라벨
		this.myID = myID;
		this.state = state;
		data.selected.put(myID, myName);

		myPanel.setLayout(new BorderLayout());

		// 디자인을 위한 빈공간
		JPanel space = new JPanel();
		space.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
		space.setBackground(Color.WHITE);

		// 이름을 표시해줄 라벨
		JLabel name = new JLabel(myName);
		name.setFont(new Font("맑은 고딕", Font.PLAIN, 25));

		// 상태 메세지 라벨
		if (state.equals(""))
			stateMessage.setText("\n");
		else
			stateMessage.setText(state);
		stateMessage.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		stateMessage.setForeground(new Color(206, 109, 60));

		JPanel nameAndState = new JPanel(new BorderLayout());
		nameAndState.add(name, BorderLayout.CENTER);
		nameAndState.add(stateMessage, BorderLayout.SOUTH);
		nameAndState.setPreferredSize(new Dimension(230, 75));
		nameAndState.setBorder(new EmptyBorder(25, 0, 0, 0));
		nameAndState.setBackground(Color.ORANGE);

		JPanel nasW = new JPanel();
		nasW.add(nameAndState);
		nasW.setBackground(Color.ORANGE);

		JPanel nasW2 = new JPanel(new BorderLayout());
		nasW2.add(nasW, BorderLayout.WEST);
		nasW2.setBackground(Color.ORANGE);

		// 프로필 패널
		RoundPanel myProfile = new RoundPanel(icon, myName, Color.ORANGE, new Dimension(80, 80), 40);
		if (icon == null)
			myProfile.setPreferredSize(new Dimension(80, 80));
		myProfile.setBackground(Color.ORANGE);
		JPanel wrapper = myProfile.wrapRoundPanel(new Dimension(90, 90));
		myProfiles.add(myProfile);
		myProfileIcon = icon;

		// 내 패널에 프로필이랑 이름 추가
		myPanel.add(wrapper, BorderLayout.WEST);
		myPanel.add(nasW2, BorderLayout.CENTER);

		// 최대, 최소 크기 설정
		myPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
		myPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 7, Color.WHITE));
		myPanel.setBackground(Color.ORANGE);

		// 나 혼자 이야기하는 채팅방 만들기, 이미 존재한다면 채팅방 열기
		myPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					TreeMap<String, String> participant = new TreeMap<String, String>();
					participant.put(myID, myName);
					createOrOpenChatRoom(participant);
				}
			}
		});
		// 팝업 메뉴
		myPanel.setComponentPopupMenu(friendPopUp(myPanel, null, myName, myID));

		friendPanel.add(space);
		myPanel.setVisible(false);
		friendPanel.add(myPanel);
		myPanel.setVisible(true);
		friendPanel.setVisible(false);
		friendPanel.add(addFriendPanel);
		friendPanel.setVisible(true);
		// 친구가 없는 경우에도 수동 사이즈 조절 해줘야 됨
		numFriendLabel.setText("친구 " + data.friends.size());
		friendPanel.setPreferredSize(new Dimension(cards.getSize().width, data.friends.size() * 120 + 160));

		// 설정창에도 내 정보 추가
		addMyOption(myID, myName, icon, state, last);
	}

	/* option panel에 내 정보 추가 */
	public void addMyOption(String myID, String name, ImageIcon icon, String stateM, String last) {
		this.myName = name;
		this.myID = myID;

		JPanel myOptionPanel = new JPanel(new BorderLayout());
		myOptionPanel.setMinimumSize(new Dimension(Integer.MAX_VALUE, 100));
		myOptionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

		// 디자인을 위한 빈공간
		JPanel space = new JPanel();
		space.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
		space.setBackground(Color.WHITE);

		// 업데이트 아이콘
		ImageIcon updateIcon = new ImageIcon("resources/update.png");
		Image sized = updateIcon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
		updateIcon = new ImageIcon(sized);

		// 이름
		JLabel nameField = new JLabel();
		nameField.setText(name);
		nameField.setFont(new Font("맑은 고딕", Font.PLAIN, 25));
		nameField.setBackground(Color.ORANGE);
		nameField.setForeground(Color.BLACK);
		nameField.setBorder(null);
		nameField.setFocusable(false);

		// 상태 메세지
		sized = updateIcon.getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH);
		updateIcon = new ImageIcon(sized);

		JTextField stateMessageField = new JTextField();
		stateMessageField.setDocument(new TextLimitDocument(40));
		if (stateM.equals(""))
			stateMessageField.setText("\n");
		else
			stateMessageField.setText(stateM);
		stateMessageField.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		stateMessageField.setBackground(Color.ORANGE);
		stateMessageField.setForeground(new Color(206, 109, 60));
		stateMessageField.setBorder(null);
		stateMessageField.setEditable(false);
		stateMessageField.setFocusable(false);
		stateMessageField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (stateMessageField.isEditable()) {
					String temp = stateMessageField.getText().trim();
					if (!state.equals(temp)) {
						state = temp;
						// 서버에 변경된 state 전송
						try {
							out.write("REQ-UpdateState " + state + "\n");
							out.flush();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						stateMessage.setText(state);
					}
					stateMessageField.setText(temp);
					stateMessageField.setCaretPosition(0);
					stateMessageField.setEditable(false);
					stateMessageField.setFocusable(false);
				}
			}
		});

		JButton updateStateButton = new JButton();
		updateStateButton.setPreferredSize(new Dimension(18, 12));
		updateStateButton.setIcon(updateIcon);
		updateStateButton.setOpaque(false);
		updateStateButton.setBorder(new EmptyBorder(2, 0, 0, 0));
		updateStateButton.setFocusPainted(false);
		updateStateButton.setContentAreaFilled(false);
		updateStateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (stateMessageField.isEditable()) {
					String temp = stateMessageField.getText().trim();
					if (!state.equals(temp)) {
						state = temp;
						// 서버에 변경된 state 전송
						try {
							out.write("REQ-UpdateState " + state + "\n");
							out.flush();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						stateMessage.setText(state);
					}
					stateMessageField.setText(temp);
					stateMessageField.setCaretPosition(0);
					stateMessageField.setEditable(false);
					stateMessageField.setFocusable(false);
				} else {
					stateMessageField.setEditable(true);
					stateMessageField.setFocusable(true);
					stateMessageField.requestFocus();
					stateMessageField.setText(state);
				}
			}
		});

		JPanel usbWrapper = new JPanel(new BorderLayout());
		usbWrapper.setBackground(Color.ORANGE);
		usbWrapper.add(updateStateButton, BorderLayout.WEST);

		JPanel stateAndUpdate = new JPanel(new BorderLayout());
		stateAndUpdate.setBackground(Color.ORANGE);
		stateAndUpdate.add(stateMessageField, BorderLayout.CENTER);
		stateAndUpdate.add(usbWrapper, BorderLayout.WEST);

		JPanel nameAndState = new JPanel(new BorderLayout());
		nameAndState.add(nameField, BorderLayout.CENTER);
		nameAndState.add(stateAndUpdate, BorderLayout.SOUTH);
		nameAndState.setPreferredSize(new Dimension(230, 75));
		nameAndState.setBorder(new EmptyBorder(25, 0, 0, 0));
		nameAndState.setBackground(Color.ORANGE);

		JPanel nasW = new JPanel();
		nasW.add(nameAndState);
		nasW.setBackground(Color.ORANGE);

		JPanel nasW2 = new JPanel(new BorderLayout());
		nasW2.add(nasW, BorderLayout.WEST);
		nasW2.setBackground(Color.ORANGE);

		// 프로필 패널
		RoundPanel profile = new RoundPanel(icon, myName, Color.ORANGE, new Dimension(80, 80), 40);
		myProfiles.add(profile);
		if (icon == null)
			profile.setPreferredSize(new Dimension(80, 80));
		profile.setBackground(Color.WHITE);
		JPanel wrapper = profile.wrapRoundPanel(new Dimension(90, 90));
		profile.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					LookAndFeel defaultUI = null;
					try {
						defaultUI = UIManager.getLookAndFeel();
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (Exception useDefault) {
					}

					JFileChooser fc = new ThumbnailFileChooser("C:\\");
					fc.removeChoosableFileFilter(fc.getFileFilter());
					fc.setFileFilter(
							new FileNameExtensionFilter("이미지 파일 (*.jpeg, *.jpg, *.png)", "jpeg", "jpg", "png"));
					int check = fc.showOpenDialog(null);
					fc.setVisible(true);

					try {
						UIManager.setLookAndFeel(defaultUI);
					} catch (UnsupportedLookAndFeelException e1) {
					}

					if (check == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						String fileName = file.getName();
						System.out.println(fileName);

						if (fileName.contains(".jpeg") || fileName.contains(".jpg") || fileName.contains(".png")) {

							ImageIcon icon2 = new ImageIcon(file.getPath());
							myProfileIcon = icon2;
							for (RoundPanel p : myProfiles) {
								p.setImage(icon2, name, new Dimension(80, 80), 40);
							}
							// 서버로 변경된 사진 전송
							try {
								sendImageIcon("REQ-UpdateProfileYes\n", file.getPath());
							} catch (IOException e1) {
							}

							System.out.println("프로필 이미지 설정 완료");
						}
					} else {
						for (RoundPanel p : myProfiles) {
							p.setImage(null, name, new Dimension(80, 80), 40);
						}
						try {
							sendImageIcon("REQ-UpdateProfile null\n", null);
						} catch (IOException e1) {
						}
					}

					// UI 원래대로
					try {
						UIManager.setLookAndFeel(defaultUI);
					} catch (Exception useDefault) {
					}
				}
			}
		});

		// 내 패널에 프로필이랑 이름 추가
		myOptionPanel.add(wrapper, BorderLayout.WEST);
		myOptionPanel.add(nasW2, BorderLayout.CENTER);

		// 최대, 최소 크기 설정
		myOptionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
		myOptionPanel.setMinimumSize(new Dimension(Integer.MAX_VALUE, 100));
		myOptionPanel.setBackground(Color.ORANGE);

		optionPanel.add(space);
		optionPanel.add(myOptionPanel);
		optionPanel.add(totalChatNum);

		totalChatNum.setText("총 채팅의 수: " + data.getTotalChatNum());
		totalChatNum.setVisible(false);
		optionPanel.setPreferredSize(new Dimension(cards.getSize().width, 120));
	}

	// 클라이언트에게 파일 전송
	private void sendImageIcon(String protocol, String path) throws IOException {
		if (path == null) {
			out.write(protocol);
			out.flush();
			return;
		}

		OutputStream outputStream = server.getOutputStream();
		BufferedImage image = ImageIO.read(new File(path));

		// 파일 사이즈가 너무 크면 좀 줄이기
		BufferedImage sizedImage;
		if (image.getWidth() > 400 && image.getHeight() > 400) {
			sizedImage = ImageConverter.scale(image, 400, 400);
		} else if (image.getWidth() > 400 && image.getHeight() <= 400) {
			sizedImage = ImageConverter.scale(image, 100, image.getHeight());
		} else if (image.getWidth() <= 400 && image.getHeight() > 400) {
			sizedImage = ImageConverter.scale(image, image.getWidth(), 400);
		} else
			sizedImage = image;

		// 오류 안뜨면 사진 존재하는 것
		out.write(protocol);
		out.flush();

		ByteArrayOutputStream byteArrayOutputStream;
		int s;
		byte[] size;
		do {
			// 바이트 단위로 전송
			byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write(sizedImage, "jpg", byteArrayOutputStream);
			s = byteArrayOutputStream.size();
			size = ByteBuffer.allocate(4).putInt(s).array();
		} while (s < 0);

		outputStream.write(size);
		outputStream.write(byteArrayOutputStream.toByteArray());
		outputStream.flush();

		System.out.println("프로필 사진 업데이트 전송");
	}

	/* Friend Panel에 새로운 친구 추가 */
	public void addNewFriend(String friendID, String friendName, ImageIcon icon, String state, String last) {
		// 친구 ID 리스트에 추가
		if (!data.friends.containsKey(friendID)) {
			data.friends.put(friendID, new Friend(friendID));
			data.friends.get(friendID).setState(state);
			data.friends.get(friendID).setLast(last);
			data.friends.get(friendID).setIcon(icon);
		}

		// 프로필 패널
		RoundPanel profile = new RoundPanel(icon, friendName, Color.ORANGE, new Dimension(80, 80), 40);
		if (icon == null)
			profile.setPreferredSize(new Dimension(80, 80));
		profile.setBackground(Color.WHITE);
		data.friends.get(friendID).addProfilePanel(profile);
		JPanel wrapper = profile.wrapRoundPanel(new Dimension(90, 90));

		JLabel fn = new JLabel(friendName);
		fn.setFont(new Font("맑은 고딕", Font.PLAIN, 25));

		JLabel stateMessage = new JLabel(state);
		if (state.equals(""))
			stateMessage.setText("\n");
		stateMessage.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		stateMessage.setForeground(new Color(206, 109, 60));
		data.friends.get(friendID).setStateLabel(stateMessage);

		JPanel nameAndState = new JPanel(new BorderLayout());
		nameAndState.add(fn, BorderLayout.CENTER);
		nameAndState.add(stateMessage, BorderLayout.SOUTH);
		nameAndState.setPreferredSize(new Dimension(230, 75));
		nameAndState.setBorder(new EmptyBorder(25, 0, 0, 0));
		nameAndState.setBackground(Color.ORANGE);

		JPanel nasW = new JPanel();
		nasW.add(nameAndState);
		nasW.setBackground(Color.ORANGE);

		JPanel nasW2 = new JPanel(new BorderLayout());
		nasW2.add(nasW, BorderLayout.WEST);
		nasW2.setBackground(Color.ORANGE);

		// 친구 정보 패널
		JPanel newFriend = new JPanel(new BorderLayout());
		newFriend.add(wrapper, BorderLayout.WEST);
		newFriend.add(nasW2, BorderLayout.CENTER);
		data.friends.get(friendID).setPanel(newFriend);

		// 최대, 최소 사이즈 지정
		newFriend.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
		newFriend.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 7, Color.WHITE));
		newFriend.setBackground(Color.ORANGE);

		// 더블클릭시 채팅방 생성, 이미 존재한다면 채팅방 열기
		newFriend.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					TreeMap<String, String> participant = new TreeMap<String, String>();
					participant.put(myID, myName);
					participant.put(friendID, friendName);
					createOrOpenChatRoom(participant);
				}
			}
		});

		// 디자인을 위한 빈공간
		JPanel space = new JPanel();
		space.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
		space.setBackground(Color.WHITE);
		data.friends.get(friendID).setSpace(space);

		// 팝업 메뉴
		newFriend.setComponentPopupMenu(friendPopUp(newFriend, space, friendName, friendID));

		// 친구 패널에 추가
		newFriend.setVisible(false);
		friendPanel.add(newFriend);
		newFriend.setVisible(true);
		friendPanel.add(space);

		// 친구 패널 크기를 친구 수 + 1 만큼 자동조절
		friendPanel.setPreferredSize(new Dimension(cards.getSize().width, data.friends.size() * 120 + 160));

		// 친구 선택창도 추가
		addSelectFriendPanel(friendID, friendName, icon);
	}

	/* 채팅 참여자 패널 */
	public void addSelectFriendPanel(String friendID, String friendName, ImageIcon icon) {
		// 프로필 패널
		RoundPanel profile = new RoundPanel(icon, friendName, Color.ORANGE, new Dimension(80, 80), 40);
		profile.setPreferredSize(new Dimension(80, 80));
		profile.setBackground(Color.WHITE);
		data.friends.get(friendID).addProfilePanel(profile);
		JPanel[] fpWrapper = profile.wrapRoundPanelArr(new Dimension(90, 90));
		data.friends.get(friendID).setFpWrapper(fpWrapper);
		fpWrapper[1].setBorder(new EmptyBorder(5, 0, 0, 0));

		// 친구 이름 표시
		JLabel fn = new JLabel(friendName);
		fn.setFont(new Font("맑은 고딕", Font.PLAIN, 25));
		fn.setBorder(new EmptyBorder(0, 10, 0, 0));

		// 친구 정보 패널
		JPanel selectFriend = new JPanel(new BorderLayout());
		selectFriend.add(fpWrapper[1], BorderLayout.WEST);
		selectFriend.add(fn, BorderLayout.CENTER);
		data.friends.get(friendID).setSelectPanel(selectFriend);
		selectFriend.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// 마우스 왼쪽 클릭시에만
				if (SwingUtilities.isLeftMouseButton(e)) {
					if (!data.selected.containsKey(friendID)) {
						data.selected.put(friendID, friendName);
						selectFriend.setBackground(Color.LIGHT_GRAY);
						fpWrapper[0].setBackground(Color.LIGHT_GRAY);
						fpWrapper[1].setBackground(Color.LIGHT_GRAY);
					} else {
						data.selected.remove(friendID);
						selectFriend.setBackground(Color.ORANGE);
						fpWrapper[0].setBackground(Color.ORANGE);
						fpWrapper[1].setBackground(Color.ORANGE);
					}
				}
			}
		});

		// 최대, 최소 사이즈 지정
		selectFriend.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
		selectFriend.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 7, Color.WHITE));
		selectFriend.setBackground(Color.ORANGE);

		// 디자인을 위한 빈공간
		JPanel space = new JPanel();
		space.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
		space.setBackground(Color.WHITE);
		data.friends.get(friendID).setSelectSpace(space);

		selectFriendListPanel.setBackground(Color.WHITE);
		selectFriendListPanel.add(selectFriend);
		selectFriendListPanel.add(space);

		// 업데이트
		Random r = new Random();
		totalChatNum.setText("총 채팅의 수: " + data.getTotalChatNum() + r.nextInt(100));
		numFriendLabel.setText("친구 " + data.friends.size()); // 친구 수 증가 반영
		selectFriendListPanel
				.setPreferredSize(new Dimension(selectFriendListPanel.getSize().width, data.friends.size() * 120));
	}

	// 상태 메세지 업데이트
	public void updateState(String friendID, String state) {
		data.friends.get(friendID).setState(state);
		data.friends.get(friendID).getStateLabel().setText(state);
	}

	// 프로필 사진 업데이트
	public void updateProfile(String friendID, ImageIcon icon) {
		data.friends.get(friendID).setIcon(icon);
		HashSet<RoundPanel> panels = data.friends.get(friendID).getProfilePanels();
		for (RoundPanel rp : panels) {
			rp.setVisible(false);
			rp.setImage(icon, data.friends.get(friendID).getName(), new Dimension(80, 80), 40);
			rp.setVisible(true);
		}
	}

	public void update(String name) {
		Random r = new Random();
		totalChatNum.setText("총 채팅의 수 : " + data.getTotalChatNum() + r.nextInt(1000) + name);
	}

	/* 친구 정보 패널 삭제 (패널만 삭제) */
	public void removeFriendPanel(String friendID) {
		data.friends.get(friendID).getPanel().setVisible(false);
		friendPanel.remove(data.friends.get(friendID).getPanel());
		data.friends.get(friendID).getSpace().setVisible(false);
		friendPanel.remove(data.friends.get(friendID).getSpace());
		data.friends.get(friendID).getSelectPanel().setVisible(false);
		selectFriendListPanel.remove(data.friends.get(friendID).getSelectPanel());
		data.friends.get(friendID).getSelectSpace().setVisible(false);
		selectFriendListPanel.remove(data.friends.get(friendID).getSelectSpace());
	}

	/* 채팅방 생성 따로 뺌 */
	private void createOrOpenChatRoom(TreeMap<String, String> participant) {
		// ID:name

		if (!isClickable)
			return;

		// 임시로 챗코드 유추하기
		StringBuffer chatCodeBuffer = new StringBuffer();
		for (String id : participant.keySet()) {
			chatCodeBuffer.append(id);
			if (!id.equals(participant.lastKey()))
				chatCodeBuffer.append("=");
		}
		String chatCode = chatCodeBuffer.toString();

		// 채팅방 이름 작명
		StringBuffer nameBuffer = new StringBuffer();
		for (Entry<String, String> info : Collections.entriesSortedByValues(participant)) {
			if (!info.getKey().equals(participant.firstKey()))
				nameBuffer.append(", ");
			nameBuffer.append(info.getValue());
		}
		String chatName = nameBuffer.toString();

		// 이 채팅방이 초기화 안 됐을 때
		if (data.chats.get(chatCode) == null || !data.chats.get(chatCode).isInitialized()) {
			try {
				createChatRoom(chatName, participant.keySet()); // 서버에 채팅방 생성 요청 날리기 (이미 있으면 무시됨)
				// 채팅창 보이기
				if (data.chats.containsKey(chatCode)) {
					createChatRoomFrame(chatCode, chatName);
					data.chats.get(chatCode).setInitialize(true);
					initChatRoomHistory(chatCode);
				} else {
					createChatRoomFrame(chatCode, chatName);
					data.chats.get(chatCode).setInitialize(true);
					initChatRoomHistory(chatCode);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else // 아니면 그냥 채팅창 보이게하기
			openChatRoom(chatCode);

	}

	/* 친구목록 팝업 메뉴 */
	private JPopupMenu friendPopUp(JPanel panel, JPanel space, String friendName, String friendID) {
		JMenuItem createChatRoom = new JMenuItem("채팅방 열기");
		createChatRoom.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		createChatRoom.setIconTextGap(5);
		createChatRoom.setBackground(Color.WHITE);
		createChatRoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TreeMap<String, String> participant = new TreeMap<String, String>();
				participant.put(myID, myName);
				participant.put(friendID, friendName);
				createOrOpenChatRoom(participant);
			}
		});

		JMenuItem removeFriend = new JMenuItem("친구 삭제");
		removeFriend.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		removeFriend.setIconTextGap(5);
		removeFriend.setBackground(Color.WHITE);
		removeFriend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					out.write("REQ-RemoveFriend " + friendID + "\n");
					out.flush();
					panel.setVisible(false);
					space.setVisible(false);
					friendPanel.remove(panel);
					friendPanel.remove(space);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				data.friends.get(friendID).getSelectPanel().setVisible(false);
				data.friends.get(friendID).getSelectSpace().setVisible(false);
				selectFriendListPanel.remove(data.friends.get(friendID).getSelectPanel());
				selectFriendListPanel.remove(data.friends.get(friendID).getSelectSpace());
				data.friends.remove(friendID);
				numFriendLabel.setText("친구 " + data.friends.size());
				friendPanel.setPreferredSize(new Dimension(cards.getSize().width, data.friends.size() * 120 + 160));
			}
		});

		JPopupMenu menu = new JPopupMenu();
		menu.setBackground(Color.WHITE);
		menu.add(createChatRoom);
		if (!friendID.equals(myID))
			menu.add(removeFriend);

		return menu;
	}

	/* 새 채팅 패널 추가 */
	public void addNewChatRoomPanel(String chatCode, String chatName) {

		if (!data.chats.containsKey(chatCode)) {
			data.chats.put(chatCode, new ChattingRoom(chatCode));
			data.chats.get(chatCode).setName(chatName); // 채팅방 이름 저장
		}

		// 새 채팅 패널
		JPanel chatRoomPanel = new JPanel();

		// 채팅방 이름을 표시해줄 라벨
		JLabel chatRoomName = new JLabel(chatName);
		chatRoomPanel.add(chatRoomName);
		// 라벨의 글자 크기 증가
		chatRoomName.setFont(new Font("맑은 고딕", Font.PLAIN, 25));
		// 크기 설정
		chatRoomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
		chatRoomPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 7, Color.WHITE));
		chatRoomPanel.setBackground(Color.ORANGE);

		// 마우스 더블 클릭시 채팅창 열기 / 초기화 안했으면 채팅 내역 초기화하기
		chatRoomPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					openChatRoom(chatCode);
				}
			}
		});

		// 디자인을 위한 빈공간
		JPanel space = new JPanel();
		space.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
		space.setBackground(Color.WHITE);

		// 팝업 메뉴
		chatRoomPanel.setComponentPopupMenu(chatRoomPopup(chatRoomPanel, space, chatCode, chatName));

		// chat 패널에 추가
		chattingRoomPanel.add(chatRoomPanel);
		chattingRoomPanel.add(space);

		totalChatNum.setText("" + data.chats.get(chatCode).getChatNum() + new Random().nextInt(1000));
		totalChatNum.setVisible(false);

		// 채팅방 수 만큼 chat 패널 길이 조절
		chattingRoomPanel.setPreferredSize(new Dimension(cards.getSize().width, data.chats.size() * 170));
	}

	/* 채팅창 여는 부분 */
	private void openChatRoom(String chatCode) {

		if (!isClickable)
			return;

		String chatName = data.chats.get(chatCode).getName(); // 채팅방 이름

		// 이 채팅방이 이미 초기화 됐을 때
		if (data.chats.get(chatCode).isInitialized())
			showChatRoom(chatCode, chatName); // 채팅방 열기

		// 아니면 서버에 초기화 요청
		else {
			showChatRoom(chatCode, chatName);
			try {
				initChatRoomHistory(chatCode);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			// 초기화 했다는 사실 저장
			data.chats.get(chatCode).setInitialize(true);
		}
	}

	/* 채팅방 팝업 메뉴 */
	private JPopupMenu chatRoomPopup(JPanel panel, JPanel space, String chatCode, String chatName) {
		JMenuItem openChatRoom = new JMenuItem("채팅방 열기");
		openChatRoom.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		openChatRoom.setIconTextGap(5);
		openChatRoom.setBackground(Color.WHITE);
		openChatRoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openChatRoom(chatCode);
			}
		});

		JMenuItem leaveChatRoom = new JMenuItem("채팅방 나가기");
		leaveChatRoom.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		leaveChatRoom.setIconTextGap(5);
		leaveChatRoom.setBackground(Color.WHITE);
		leaveChatRoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					data.chats.get(chatCode).setFrame(null);
					data.chats.get(chatCode).setInitialize(false);
					data.chats.remove(chatCode);
					JsonObject chatRoom = new JsonObject();
					chatRoom.addProperty("chatCode", chatCode);
					chatRoom.addProperty("name", chatName);
					String json = JSON.toJson(chatRoom);

					out.write("REQ-LeaveChatRoom " + json + "\n");
					out.flush();
					panel.setVisible(false);
					space.setVisible(false);
					chattingRoomPanel.remove(panel);
					chattingRoomPanel.remove(space);
					chattingRoomPanel.setPreferredSize(new Dimension(cards.getSize().width, data.chats.size() * 170));

				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		});

		JPopupMenu menu = new JPopupMenu();
		menu.setBackground(Color.WHITE);
		menu.add(openChatRoom);
		menu.add(leaveChatRoom);

		return menu;
	}

	public void setSearchedPanel(String name, String id, String state, ImageIcon profile, boolean isExistUser) {
		searchFriendFrame.setSearchedPanel(id, name, state, profile, isExistUser);
	}

	/* Friend, Chat, Public, Option 패널들을 초기화 */
	private void initCardPanels() throws IOException {

		// 친구 페이지
		friendPanel.setLayout(new BoxLayout(friendPanel, BoxLayout.Y_AXIS));
		friendPanel.setBackground(Color.WHITE);
		// 친구 페이지를 스크롤 가능하게
		JScrollPane scrollFriendPanel = createCustomScrollPanel(friendPanel, Color.WHITE);

		// 친구 수 표시 라벨, 친구 추가 버튼이 들어있는 패널
		addFriendPanel.setLayout(new BorderLayout());
		addFriendPanel.setBackground(Color.WHITE);
		addFriendPanel.setBorder(new EmptyBorder(0, 0, 0, 2));
		addFriendPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		// 친구 수 표시 라벨
		numFriendLabel.setBorder(new EmptyBorder(0, 10, 0, 0)); // 라벨 왼쪽에 빈공간 추가
		numFriendLabel.setText("친구 " + data.friends.size());
		numFriendLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		// 친구 추가 페이지로 이동시키는 버튼
		addFriendPanel.add(numFriendLabel, BorderLayout.WEST);
		JButton addFriendButton = RoundPanel.RoundButton(new Dimension(60, 60), pressed, onButton, Color.WHITE);
		// 버튼 디자인 관련
		addFriendButton.setPreferredSize(new Dimension(65, 65));
		ImageIcon icon = new ImageIcon("resources/AddFriendButton.png");
		Image sized = icon.getImage().getScaledInstance(45, 36, Image.SCALE_SMOOTH);
		addFriendButton.setIcon(new ImageIcon(sized));
		addFriendButton.setBorder(new EmptyBorder(0, 0, 20, 0));
		addFriendButton.setBackground(Color.WHITE);
		addFriendButton.setFocusPainted(false);
		addFriendButton.setContentAreaFilled(false);

		// 버튼 누르면 친구 추가 창 띄우기
		addFriendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (searchFriendFrame == null || !searchFriendFrame.isShowing()) { // 친구 서치 창이 이미 만들어져 있다면 다시 만들지 않기
					searchFriendFrame = new UserSearchWindow(myID, out, data);
					searchFriendFrame.setBounds(frame.getBounds().x + 403, frame.getBounds().y, 420, 320);
				}
			}
		});

		JPanel afbPanel = new JPanel();
		afbPanel.add(addFriendButton);
		afbPanel.setBackground(Color.WHITE);
		addFriendPanel.add(afbPanel, BorderLayout.EAST);

		// 채팅 페이지
		JPanel chatPanel = new JPanel();
		chatPanel.setLayout(new BorderLayout());
		chatPanel.setBackground(Color.WHITE); // 채팅방 생성 버튼 + 채팅방 리스트 전체 포함

		// 친구 선택창으로 넘어가게
		JPanel toSelectFriendBar = new JPanel(new BorderLayout());
		toSelectFriendBar.setPreferredSize(new Dimension(400, 50));
		toSelectFriendBar.setBorder(new EmptyBorder(0, 0, 10, 7));
		toSelectFriendBar.setBackground(Color.WHITE);
		chatPanel.add(toSelectFriendBar, BorderLayout.NORTH);

		// 친구 선택창 이동
		JButton toSelectFriendButton = RoundPanel.RoundButton(new Dimension(60, 60), pressed, onButton, Color.WHITE);
		toSelectFriendBar.add(toSelectFriendButton, BorderLayout.EAST);
		toSelectFriendButton.setPreferredSize(new Dimension(45, 45));
		// 디자인
		ImageIcon iconImg = new ImageIcon("resources/AddChatIcon.png");
		Image sizedImg = iconImg.getImage().getScaledInstance(37, 40, Image.SCALE_SMOOTH);
		toSelectFriendButton.setIcon(new ImageIcon(sizedImg));
		toSelectFriendButton.setBorder(new EmptyBorder(0, 2, 0, 0));
		toSelectFriendButton.setFocusPainted(false);
		toSelectFriendButton.setContentAreaFilled(false);

		toSelectFriendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cl.show(cards, "Select");
			}
		});

		// 내가 속한 채팅 리스트 표시
		chattingRoomPanel.setLayout(new BoxLayout(chattingRoomPanel, BoxLayout.Y_AXIS));
		chattingRoomPanel.setBackground(Color.WHITE);

		// 채팅 페이지를 스크롤 가능하게
		JScrollPane scrollChatPanel = createCustomScrollPanel(chattingRoomPanel, Color.WHITE);
		chatPanel.add(scrollChatPanel, BorderLayout.CENTER);

		// 채팅방 참가자 선택 패널
		JPanel selectFriendPanel = new JPanel();
		selectFriendPanel.setLayout(new BorderLayout());

		// 뒤로가기, 채팅 생성
		JPanel selectFriendConfirmBar = new JPanel(new BorderLayout());
		selectFriendConfirmBar.setPreferredSize(new Dimension(400, 50));
		selectFriendConfirmBar.setBorder(new EmptyBorder(0, 5, 10, 7));
		selectFriendConfirmBar.setBackground(Color.WHITE);
		selectFriendPanel.add(selectFriendConfirmBar, BorderLayout.NORTH);

		// 뒤로가기 버튼
		icon = new ImageIcon("resources/BackIcon.png");
		Image image = icon.getImage().getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH);
		JButton back = RoundPanel.RoundButton(new Dimension(60, 60), pressed, onButton, Color.WHITE);
		back.setIcon(new ImageIcon(image));
		back.setPreferredSize(new Dimension(40, 40));
		back.setBorderPainted(false);
		back.setFocusPainted(false);
		back.setContentAreaFilled(false);
		selectFriendConfirmBar.add(back, BorderLayout.WEST);
		back.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (String key : data.friends.keySet()) {
					JPanel[] fpWrapper = data.friends.get(key).getFpWrapper();
					fpWrapper[0].setBackground(Color.ORANGE);
					fpWrapper[1].setBackground(Color.ORANGE);

					data.friends.get(key).getSelectPanel().setBackground(Color.ORANGE);
					data.selected.remove(key);
				}
				cl.show(cards, "Chat");
			}
		});

		// 채팅창 생성 확인 TODO
		icon = new ImageIcon("resources/ConfirmIcon.png");
		sizedImg = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
		JButton confirm = RoundPanel.RoundButton(new Dimension(60, 60), pressed, onButton, Color.WHITE);
		confirm.setIcon(new ImageIcon(sizedImg));
		confirm.setPreferredSize(new Dimension(40, 40));
		confirm.setBorder(new EmptyBorder(0, 0, 0, 5));
		confirm.setFocusPainted(false);
		confirm.setContentAreaFilled(false);
		selectFriendConfirmBar.add(confirm, BorderLayout.EAST);
		confirm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (data.selected.size() > 0) { // 채팅방 생성 후 초기화
					createOrOpenChatRoom(data.selected);
					for (String key : data.friends.keySet()) {
						JPanel[] fpWrapper = data.friends.get(key).getFpWrapper();
						fpWrapper[0].setBackground(Color.ORANGE);
						fpWrapper[1].setBackground(Color.ORANGE);

						data.friends.get(key).getSelectPanel().setBackground(Color.ORANGE);
						data.selected.remove(key);
					}
					cl.show(cards, "Chat");
				}
			}
		});

		// 친구 선택 리스트
		selectFriendListPanel.setLayout(new BoxLayout(selectFriendListPanel, BoxLayout.Y_AXIS));
		// selectFriendListPanel.setBackground(Color.WHITE);
		JScrollPane scrollSelectFriendListPanel = createCustomScrollPanel(selectFriendListPanel, Color.WHITE);
		selectFriendPanel.add(scrollSelectFriendListPanel, BorderLayout.CENTER);

		// 공공 데이터 페이지
		publicDataPanel.setLayout(new BoxLayout(publicDataPanel, BoxLayout.Y_AXIS));
		publicDataPanel.setBackground(Color.WHITE);
		JScrollPane scrollPublicDataPanel = createCustomScrollPanel(publicDataPanel, Color.WHITE);

		// 설정 페이지
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
		optionPanel.setBackground(Color.WHITE);
		JScrollPane scrollOptionPanel = createCustomScrollPanel(optionPanel, Color.WHITE);

		// 친구, 채팅방, 공공데이터, 설정 패널들은 모두 카드 레이아웃으로 연결되있음
		cards.setPreferredSize(new Dimension(330, 750));
		cards.add(scrollFriendPanel, "Friend");
		cards.add(chatPanel, "Chat");
		cards.add(selectFriendPanel, "Select");
		cards.add(scrollPublicDataPanel, "PublicData");
		cards.add(scrollOptionPanel, "Option");

		// 메인 패널에 카드들 추가
		frame.getContentPane().add(cards, BorderLayout.WEST);
	}

	// 공공데이터 패널 만들기 TODO
	public void addNewPublicDataPanel(JsonObject info) {
		String day = info.get("day").getAsString(); // 날짜
		String time = info.get("time").getAsString(); // 시간
		String precipitation = info.get("precipitation").getAsString(); // 강수확률
		String humidity = info.get("humidity").getAsString(); // 습도
		String sky = info.get("sky").getAsString(); // 하늘
		String temperature = info.get("temperature").getAsString(); // 기온

		JPanel space = new JPanel();
		space.setBackground(Color.WHITE);
		space.setPreferredSize(new Dimension(400, 10));
		publicDataPanel.add(space);
		
		JLabel temLabel = new JLabel(temperature);
		temLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 35));
		temLabel.setForeground(Color.WHITE);
		
		
		String ampm = null;
		int hour = Integer.parseInt(time.substring(0, 2));
		if (hour == 12) {
			ampm = "오후";
		}
		else if (hour > 12) {
			ampm = "오후";
			hour = hour - 12;
		}
		else if (hour == 0) {
			ampm = "오전";
			hour = 12;
		}
		else {
			ampm = "오전";
		}
		
		JLabel timeLabel = new JLabel(ampm + " " + hour + "시");
		timeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
		timeLabel.setForeground(Color.WHITE);
		
		JLabel humiLabel = new JLabel("습도 " + humidity);
		humiLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
		humiLabel.setForeground(Color.WHITE);
		
		JLabel preLabel = new JLabel("강수확률 " + precipitation);
		preLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
		preLabel.setForeground(Color.WHITE);
		
		JPanel hpPanel = new JPanel(new BorderLayout());
		hpPanel.add(humiLabel, BorderLayout.NORTH);
		hpPanel.add(preLabel, BorderLayout.SOUTH);
		hpPanel.setBackground(new Color(0, 0, 0, 0));
		
		ImageIcon skyImage = null;
		Image sized = null;
		ImageIcon symbol = null;
		
		switch (sky) {
		case "맑음":
			skyImage = new ImageIcon("resources/sky/ClearSky.png");
			symbol = new ImageIcon("resources/sky/SunIcon.png");
			break;
		case "비":
			skyImage = new ImageIcon("resources/sky/RainySky.png");
			symbol = new ImageIcon("resources/sky/RainIcon.png");
			break;
		case "구름 많음":
			skyImage = new ImageIcon("resources/sky/ManyCloudSky.png");
			symbol = new ImageIcon("resources/sky/ManyCloudIcon.png");
			break;
		case "흐림":
			skyImage = new ImageIcon("resources/sky/CloudySky.png");
			symbol = new ImageIcon("resources/sky/CloudyIcon.png");
			break;
		default:
			skyImage = new ImageIcon("resources/sky/ClearSky.png");
			symbol = new ImageIcon("resources/sky/SunIcon.png");
			break;
		}
		sized = symbol.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
		symbol = new ImageIcon(sized);
		
		JPanel body = new JPanel(new GridBagLayout());
		body.setBackground(Color.WHITE);
		GridBagConstraints con = new GridBagConstraints();
		con.gridx = 0;
		con.gridy = 0;
		con.ipadx = 20;
		con.insets = new Insets(10, 0, 0, 0);
		con.anchor = GridBagConstraints.CENTER;
		body.add(new JLabel(symbol), con);
		con.gridx = 1;
		con.ipadx = 0;
		con.anchor = GridBagConstraints.LINE_END;
		body.add(temLabel, con);
		con.gridx = 0;
		con.gridy = 1;
		con.insets = new Insets(0, 5, 0, 0);
		con.anchor = GridBagConstraints.CENTER;
		body.add(timeLabel, con);
		con.gridx = 0;
		con.gridy = 2;
		con.insets = new Insets(0, 20, 0, 0);
		body.add(hpPanel, con);
		con.gridx = 0;
		con.gridy = 0;
		con.gridwidth = 3;
		con.gridheight = 3;
		con.ipadx = 0;
		con.insets = new Insets(0, 5, 0, 0);
		con.anchor = GridBagConstraints.WEST;
		body.add(new JLabel(ImageConverter.rounding(skyImage, new Dimension(330, 200))), con);
		
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setVisible(false);
		wrapper.setBackground(Color.WHITE);
		wrapper.add(body, BorderLayout.WEST);
		
		publicDataPanel.add(wrapper);
		wrapper.setVisible(true);

	}

	/* Friend, Chat, Public, Option 버튼들을 초기화 */
	private void initButtonPanel() {
		// 친구 페이지으로 이동해 주는 버튼
		ImageIcon icon = new ImageIcon("resources/FriendIcon.png");
		// 이미지 불러와서 사이즈 변경 후 버튼으로 만들기
		Image image = icon.getImage().getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
		JButton friendButton = RoundPanel.RoundButton(new Dimension(60, 60), pressed, onButton, Color.WHITE);
		friendButton.setIcon(new ImageIcon(image));
		friendButton.setPreferredSize(new Dimension(55, 55));
		// 디자인을 위해 경계선 제거
		friendButton.setBorder(new EmptyBorder(0, 2, 0, 0));
		friendButton.setFocusPainted(false);
		friendButton.setContentAreaFilled(false);
		// 누르면 친구 페이지로 이동
		friendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cl.show(cards, "Friend");
			}
		});

		// 채팅 페이지로 이동해 주는 버튼
		icon = new ImageIcon("resources/ChatIcon.png");
		// 이미지 불러와서 사이즈 변경 후 버튼으로 만들기
		image = icon.getImage().getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
		JButton chatButton = RoundPanel.RoundButton(new Dimension(60, 60), pressed, onButton, Color.WHITE);
		chatButton.setIcon(new ImageIcon(image));
		chatButton.setPreferredSize(new Dimension(55, 55));
		// 디자인을 위해 경계선 제거
		chatButton.setBorder(new EmptyBorder(0, 2, 0, 0));
		chatButton.setFocusPainted(false);
		chatButton.setContentAreaFilled(false);
		// 누르면 채팅 페이지로 이동
		chatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cl.show(cards, "Chat");
			}
		});

		// 공공 데이터 페이지로 이동해 주는 버튼
		// 설정 페이지로 이동해 주는 버튼
		icon = new ImageIcon("resources/PublicDataIcon.png");
		// 이미지 불러와서 사이즈 변경 후 버튼으로 만들기
		image = icon.getImage().getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
		JButton publicDataButton = RoundPanel.RoundButton(new Dimension(60, 60), pressed, onButton, Color.WHITE);
		publicDataButton.setIcon(new ImageIcon(image));
		publicDataButton.setPreferredSize(new Dimension(55, 55));
		// 디자인을 위해 경계선 제거
		publicDataButton.setBorder(new EmptyBorder(0, 2, 0, 0));
		publicDataButton.setFocusPainted(false);
		publicDataButton.setContentAreaFilled(false);
		// 누르면 공공 데이터 페이지로 이동
		publicDataButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cl.show(cards, "PublicData");
			}
		});

		// 설정 페이지로 이동해 주는 버튼
		icon = new ImageIcon("resources/OptionIcon.png");
		// 이미지 불러와서 사이즈 변경 후 버튼으로 만들기
		image = icon.getImage().getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
		JButton optionButton = RoundPanel.RoundButton(new Dimension(60, 60), pressed, onButton, Color.WHITE);
		optionButton.setIcon(new ImageIcon(image));
		optionButton.setPreferredSize(new Dimension(55, 55));
		// 디자인을 위해 경계선 제거
		optionButton.setBorder(new EmptyBorder(0, 2, 0, 0));
		optionButton.setFocusPainted(false);
		optionButton.setContentAreaFilled(false);
		// 누르면 설정 페이지로 이동
		optionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cl.show(cards, "Option");
			}
		});
		;

		// 버튼들 있는 패널
		buttonPanel.setPreferredSize(new Dimension(60, 750));
		buttonPanel.setBorder(new EmptyBorder(0, 0, 0, 3));
		buttonPanel.setBackground(Color.WHITE);
		buttonPanel.add(friendButton);
		buttonPanel.add(chatButton);
		buttonPanel.add(publicDataButton);
		buttonPanel.add(optionButton);

		// 메인 패널에 버튼들 추가
		frame.getContentPane().add(buttonPanel, BorderLayout.EAST);
	}

	/* 채팅방 만들기 */
	private void createChatRoomFrame(String chatCode, String chatName) {
		// 채팅창이 리스트에 없으면 넣기
		if (!data.chats.containsKey(chatCode)) {
			data.chats.put(chatCode, new ChattingRoom(chatCode));
			data.chats.get(chatCode).setName(chatName);
		}

		// 채팅방 프레임 생성
		chatRoomFrame = new JFrame();
		data.chats.get(chatCode).setFrame(chatRoomFrame);
		
		ImageIcon icon = new ImageIcon("resources/MainIcon.png");
		Image image = icon.getImage().getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH);
		chatRoomFrame.setIconImage(image);

		// 이름 설정
		chatRoomFrame.setTitle("chatter - " + chatName);
		// 크기, 위치 조절
		chatRoomFrame.setBounds(frame.getBounds().x + 403, frame.getBounds().y, 420, 650);
		chatRoomFrame.setMinimumSize(new Dimension(420, 320));

		// 메인 패널 변수 이름 간단히 줄이기
		JPanel chatRoomPanel = (JPanel) chatRoomFrame.getContentPane();
		chatRoomPanel.setBackground(skyBlue);

		// 말풍선 업데이트
		JLabel counter = new JLabel();
		counter.setForeground(Color.WHITE);
		data.chats.get(chatCode).setUpdateLabel(counter);
		chatRoomPanel.add(counter);

		// 메세지 출력창
		JPanel messageArea = new JPanel();
		messageArea.setLayout(new BoxLayout(messageArea, BoxLayout.Y_AXIS));
		messageArea.setBackground(skyBlue);
		data.chats.get(chatCode).setMessageArea(messageArea);
		chatRoomPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				JPanel myPanel = (JPanel) e.getComponent();
				Dimension size = myPanel.getSize();
				messageArea.setPreferredSize(new Dimension(size.width, messageArea.getHeight()));
			}
		});

		JPanel messageAreaWrapper = new JPanel();
		messageAreaWrapper.setBackground(skyBlue);
		messageAreaWrapper.add(messageArea);
		data.chats.get(chatCode).setWrap(messageAreaWrapper);

		// 스크롤 추가
		JScrollPane scrollMessageArea = createCustomScrollPanel(messageAreaWrapper, skyBlue);
		data.chats.get(chatCode).setScrollPanel(scrollMessageArea);
		// 채팅 말풍선이 새로 추가되면 스크롤 맨 밑으로 이동 TODO
		messageArea.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				JScrollBar scrollBar = scrollMessageArea.getVerticalScrollBar();
				scrollBar.setValue(scrollBar.getMaximum());
			}
		});

		// 메세지 입력창
		JTextField inputMessageField = new JTextField();
		inputMessageField.setFont(new Font("맑은 고딕", Font.PLAIN, 20)); // 크기 설정
		inputMessageField.setPreferredSize(new Dimension(320, 40));
		inputMessageField.setBorder(new EmptyBorder(5, 10, 5, 10));
		inputMessageField.addActionListener(new ActionListener() { // 채팅 입력
			@Override
			public void actionPerformed(ActionEvent e) {
				String chat = inputMessageField.getText();
				if (!chat.equals("")) {
					inputMessageField.setText("");

					// 서버에 보내기 위해 json 형태로 변환
					JsonObject chatJson = new JsonObject();
					chatJson.addProperty("chatCode", chatCode);
					chatJson.addProperty("content", chat);
					chatJson.addProperty("time",
							new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(Calendar.getInstance().getTime()));

					try { // 서버로 전송
						out.write("REQ-Chat " + chatJson + "\n");
						out.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(Color.WHITE);

		// 메세지 입력버튼
		JButton inputMessageButton = RoundPanel.RoundButton(new Dimension(25, 25), new Color(255, 180, 30),
				new Color(255, 165, 0, 200), Color.ORANGE);
		inputMessageButton.setPreferredSize(new Dimension(50, 35));
		// 디자인
		icon = new ImageIcon("resources/SendMessageIcon.png");
		Image sizedImg = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
		inputMessageButton.setIcon(new ImageIcon(sizedImg));
		inputMessageButton.setBorder(new EmptyBorder(2, 5, 0, 0));
		inputMessageButton.setFocusPainted(false);
		inputMessageButton.setContentAreaFilled(false);
		inputMessageButton.addActionListener(new ActionListener() { // 채팅 입력
			@Override
			public void actionPerformed(ActionEvent e) {
				String chat = inputMessageField.getText();
				if (!chat.equals("")) {
					inputMessageField.setText("");

					// 서버에 보내기 위해 json 형태로 변환
					JsonObject chatJson = new JsonObject();
					chatJson.addProperty("chatCode", chatCode);
					chatJson.addProperty("content", chat);
					chatJson.addProperty("time",
							new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(Calendar.getInstance().getTime()));

					try { // 서버로 전송
						out.write("REQ-Chat " + chatJson + "\n");
						out.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		JPanel imbWrapper = new JPanel();
		imbWrapper.setBackground(Color.WHITE);
		imbWrapper.add(inputMessageButton);

		// 한번 감싸앉기
		wrapper.add(inputMessageField, BorderLayout.CENTER);
		wrapper.add(imbWrapper, BorderLayout.EAST);

		// 메세지 입력 요소 모아놓은 패널
		JPanel inputMessagePanel = new JPanel(new BorderLayout());
		inputMessagePanel.setPreferredSize(new Dimension(420, 100));
		inputMessagePanel.setMinimumSize(new Dimension(420, 100));
		// 입력 기능을 담당하는 것들을 패널에 추가
		inputMessagePanel.add(wrapper, BorderLayout.NORTH);

		// 파일 전송 버튼
		JButton fileTransferButton = new JButton();
		ImageIcon fileIcon = new ImageIcon("resources/FileIcon.png");
		Image sized = fileIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		fileTransferButton.setIcon(new ImageIcon(sized));
		fileTransferButton.setPreferredSize(new Dimension(40, 30));
		fileTransferButton.setBorder(new EmptyBorder(0, 10, 0, 0));
		fileTransferButton.setOpaque(false);
		fileTransferButton.setBorderPainted(false);
		fileTransferButton.setFocusPainted(false);
		fileTransferButton.setContentAreaFilled(false);
		fileTransferButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LookAndFeel defaultUI = null;
				try {
					defaultUI = UIManager.getLookAndFeel();
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception useDefault) {
				}

				JFileChooser fc = new ThumbnailFileChooser("C:\\");
				fc.removeChoosableFileFilter(fc.getFileFilter());
				fc.setFileFilter(new FileNameExtensionFilter("전송 가능한 모든 파일", "jpeg", "jpg", "png", "gif", "bmp", "psd",
						"ai", "sketch", "tif", "tiff", "tga", "webp", "dng", "heic", "doc", "docx", "hwp", "ppt",
						"pptx", "xls", "xlsx", "pdf", "txt", "rtf", "xml", "wks", "wps", "xps", "md", "odf", "odt",
						"pages", "odp", "key", "show", "ods", "csv", "tsv", "zip", "rar", "7z", "gz", "bz2", "lzh",
						"alz"));
				fc.addChoosableFileFilter(new FileNameExtensionFilter("이미지 파일", "jpeg", "jpg", "png"));
				fc.addChoosableFileFilter(new FileNameExtensionFilter("미리보기 지원 안하는 이미지 파일", "gif", "bmp", "psd", "ai",
						"sketch", "tif", "tiff", "tga", "webp", "dng", "heic"));
				fc.addChoosableFileFilter(new FileNameExtensionFilter("문서 파일", "doc", "docx", "hwp", "ppt", "pptx",
						"xls", "xlsx", "pdf", "txt", "rtf", "xml", "wks", "wps", "xps", "md", "odf", "odt", "pages",
						"odp", "key", "show", "ods", "csv", "tsv"));
				fc.addChoosableFileFilter(
						new FileNameExtensionFilter("압축 파일", "zip", "rar", "7z", "gz", "bz2", "lzh", "alz"));
				int check = fc.showOpenDialog(null);
				fc.setVisible(true);

				try {
					UIManager.setLookAndFeel(defaultUI);
				} catch (UnsupportedLookAndFeelException e1) {
				}

				// 서버로 파일 전송
				if (check == JFileChooser.APPROVE_OPTION) {
					try {
						File file = fc.getSelectedFile();
						String fileName = file.getName();

						JsonObject fileInfo = new JsonObject();
						fileInfo.addProperty("content", fileName);
						fileInfo.addProperty("chatCode", chatCode);
						fileInfo.addProperty("time",
								new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(Calendar.getInstance().getTime()));
						out.write("REQ-FileChat " + fileInfo + "\n");
						out.flush();

						byte[] byteArray = new byte[(int) file.length()];
						BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

						BufferedOutputStream fileOutput = new BufferedOutputStream(server.getOutputStream());

						bis.read(byteArray, 0, byteArray.length);
						fileOutput.write(byteArray, 0, byteArray.length);
						fileOutput.flush();
						bis.close();

						System.out.println("파일 전송: " + fileName);
						UIManager.setLookAndFeel(defaultUI); // UI 원래대로
					} catch (Exception e1) {
					}
				}
			}
		});

		// 보조기능 요소들 모아놓은 패널
		JPanel subElementPanel = new JPanel(new BorderLayout());
		subElementPanel.setPreferredSize(new Dimension(420, 60));
		subElementPanel.setMinimumSize(new Dimension(420, 60));
		subElementPanel.setBackground(Color.WHITE);
		subElementPanel.add(fileTransferButton, BorderLayout.WEST);
		inputMessagePanel.add(subElementPanel, BorderLayout.SOUTH);

		// 메인 패널에 다 추가
		chatRoomPanel.add(scrollMessageArea, BorderLayout.CENTER);
		chatRoomPanel.add(inputMessagePanel, BorderLayout.SOUTH);

		// 채팅 출력창 크기 조절
		chatRoomFrame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				messageAreaWrapper.setPreferredSize(
						new Dimension(e.getComponent().getWidth() - 12, messageArea.getHeight() + 10));
			}
		});

		chatRoomFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		chatRoomFrame.setVisible(true);
		data.chats.get(chatCode).setFrame(chatRoomFrame); // 채팅 리스트에 챗코드 + 채팅방 프레임 추가
		messageArea.setPreferredSize(new Dimension(400, data.chats.get(chatCode).getChatNum() * 100));
	}

	/* 채팅창 새로 만들기 */
	public void createChatRoom(String chatRoomName, Set<String> participantID) throws IOException {
		// 채팅 생성 정보 JSON으로 변환
		JsonObject chatInfo = new JsonObject();
		chatInfo.addProperty("name", chatRoomName); // 채팅방 이름

		JsonArray p = JSON.fromJson(JSON.toJson(participantID), JsonArray.class);
		chatInfo.add("participant", p); // 채팅 참가자 리스트 추가
		String json = JSON.toJson(chatInfo);

		// 채팅방 만든다고 서버에 전달
		out.write("REQ-NewChatRoom " + json + "\n"); // 채팅방 이름 + 채팅방 참가자 목록 전송
		out.flush();

	}

	/* chat code에 해당되는 채팅 보이도록 on */
	public void showChatRoom(String chatCode, String chatName) {
		JFrame chatRoom = data.chats.get(chatCode).getFrame();

		// 채팅방이 이미 만들어져있으면
		if (chatRoom != null)
			chatRoom.setVisible(true); // 보이게 on
		else // 채팅창이 만들어진적 없으면 새로 만들기
			createChatRoomFrame(chatCode, chatName);
	}

	// 채팅 말풍선의 텍스트 가중치 조절
	private double[] weightString(String content) {
		double[] counting = { 0, 0 };
		Character[] shortChar = { '`', '.', ',', '!', '\"', '\'', ';', ':', '/', '|', '*', '(', ')', '{', '}', '[',
				']' };
		java.util.List<Character> shortCharList = Arrays.asList(shortChar);

		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (Character.isLowerCase(c))
				counting[0] += 1.1;
			else if (Character.isUpperCase(c))
				counting[0] += 1.3;
			else if (Character.isDigit(c))
				counting[0] += 1.11;
			else if (Character.isWhitespace(c))
				counting[0] += 0.8;
			else if (shortCharList.contains(c))
				counting[0] += 0.7;
			else if (c > 32 && c < 127)
				counting[0] += 1.4;
			else
				counting[0] += 2;

			if (counting[0] < 31)
				counting[1] = i;
		}
		return counting;
	}

	// 파일 다운 경로
	public void setDownPath(String path) {
		this.downPath = path;
	}

	public void setDefaultDownPath() {
		this.downPath = "C:/Users/" + System.getProperty("user.name") + "/Documents/채터 다운로드";
	}

	public String getDownPath() {
		return downPath;
	}

	public boolean isDirChanged() {
		return !downPath.equals("C:/Users/" + System.getProperty("user.name") + "/Documents/채터 다운로드");
	}

	// 채팅 말풍선
	public void addNewChat(String chatCode, String senderID, String senderName, String content, String chatTime,
			String type, ImageIcon image) {

		// 채팅창이 활성화되있지 않으면 메세지 무시
		if (data.chats.get(chatCode).getFrame() == null)
			return;

		// 프로필 사진
		RoundPanel profile;
		if (myID.equals(senderID))
			profile = new RoundPanel(myProfileIcon, senderName, Color.ORANGE, new Dimension(50, 50), 20);
		else if (!data.friends.containsKey(senderID))
			profile = new RoundPanel(null, "?", Color.CYAN, new Dimension(50, 50), 20);
		else
			profile = new RoundPanel(data.friends.get(senderID).getIcon(), senderName, Color.CYAN,
					new Dimension(50, 50), 20);
		profile.setPreferredSize(new Dimension(50, 50));
		profile.setBackground(skyBlue);
		JPanel wrapper = new JPanel();
		wrapper.setBackground(skyBlue);
		wrapper.add(profile);

		// 이름
		JLabel name = new JLabel(senderName);
		name.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		name.setBorder(new EmptyBorder(0, 3, 0, 0));

		// 채팅 시간
		JLabel time = new JLabel();
		String previousTime = data.chats.get(chatCode).getTime();
		data.chats.get(chatCode).setTime(chatTime); // 지난 시간 업데이트
		int year = Integer.parseInt(chatTime.substring(0, 4));
		int month = Integer.parseInt(chatTime.substring(5, 7));
		int date = Integer.parseInt(chatTime.substring(8, 10));
		int hour = Integer.parseInt(chatTime.substring(11, 13));
		int minute = Integer.parseInt(chatTime.substring(14, 16));
		if (hour == 0)
			time.setText("오전 " + 12 + "시" + ((minute == 0) ? "" : (" " + minute + "분")));
		else if (hour == 12)
			time.setText("오후 " + 12 + "시" + ((minute == 0) ? "" : (" " + minute + "분")));
		else if (hour > 12)
			time.setText("오후 " + (hour - 12) + "시" + ((minute == 0) ? "" : (" " + minute + "분")));
		else
			time.setText("오전 " + hour + "시" + ((minute == 0) ? "" : (" " + minute + "분")));
		time.setFont(new Font("맑은 고딕", Font.PLAIN, 11));

		int prevYear = 0;
		int prevMonth = 0;
		int prevDate = 0;
		if (previousTime != null) {
			prevYear = Integer.parseInt(previousTime.substring(0, 4));
			prevMonth = Integer.parseInt(previousTime.substring(5, 7));
			prevDate = Integer.parseInt(previousTime.substring(8, 10));

		}
		boolean dateChanged = (prevYear != year || prevMonth != month || prevDate != date);
		int timePanelSize = 0;

		if (dateChanged) {
			JPanel timePanel = new RoundPanel(new Dimension(15, 15), skyBlue);
			JLabel timeNote = new JLabel(year + "년 " + month + "월 " + date + "일");
			timeNote.setBackground(skyBlue);
			timeNote.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
			timePanel.add(timeNote);

			JPanel timeWrapperPanel = new JPanel();
			timeWrapperPanel.add(timePanel);
			timeWrapperPanel.setBackground(skyBlue);
			timeWrapperPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
			timePanelSize = 45;

			JPanel messageArea = data.chats.get(chatCode).getMessageArea();
			messageArea.add(timeWrapperPanel);
		}

		JTextPane chatContent = new JTextPane();
		chatContent.setEditable(false);
		chatContent.setBorder(null);
		chatContent.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		StyledDocument doc = chatContent.getStyledDocument();
		Style style = chatContent.addStyle("Color Style", null);
		StyleConstants.setBackground(style, Color.WHITE);
		StyleConstants.setForeground(style, Color.BLACK);
		int count = 1;
		String backupContent = content;
		try {
			while (weightString(content)[0] > 30) {
				doc.insertString(doc.getLength(), content.substring(0, (int) weightString(content)[1]) + "\n", style);
				content = content.substring((int) weightString(content)[1]);
				count++;
			}
			doc.insertString(doc.getLength(), content, style);
		} catch (BadLocationException e) {
		}

		int balloonSize = 0;
		if (count == 1)
			balloonSize = 22;
		else
			balloonSize = 22 * (count);

		// 말풍선
		JPanel chatBalloon = new RoundPanel(new Dimension(15, 15), Color.WHITE);
		chatBalloon.setBackground(Color.WHITE);
		chatBalloon.add(chatContent);

		// 파일인 경우 다운로드 라벨 추가
		if (type.equals("file")) {

			JLabel downFile = new JLabel();
			File path = new File(downPath + "/" + backupContent);
			if (path.exists())
				downFile.setText("열기");
			else
				downFile.setText("저장");
			downFile.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
			downFile.addMouseListener(new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					Font font = downFile.getFont();
					Map attributes = font.getAttributes();
					attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
					downFile.setFont(font.deriveFont(attributes));
				}

				public void mouseExited(MouseEvent e) {
					downFile.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
				}

				public void mousePressed(MouseEvent e) {
					try {
						File path = new File(downPath + "/" + backupContent);
						if (path.exists())
							Desktop.getDesktop().open(path);
						else {
							JsonObject downInfo = new JsonObject();
							downInfo.addProperty("chatCode", chatCode);
							downInfo.addProperty("fileName", backupContent);

							out.write("REQ-DownloadFile " + downInfo + "\n");
							out.flush();
							downFile.setText("열기");
						}
					} catch (IOException e1) {
					}
				}
			});

			JLabel downOtherName = new JLabel("다른 이름으로 저장");
			downOtherName.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
			downOtherName.addMouseListener(new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					Font font = downFile.getFont();
					Map attributes = font.getAttributes();
					attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
					downOtherName.setFont(font.deriveFont(attributes));
				}

				public void mouseExited(MouseEvent e) {
					downOtherName.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
				}

				public void mousePressed(MouseEvent e) {
					LookAndFeel defaultUI = null;
					try {
						defaultUI = UIManager.getLookAndFeel();
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (Exception useDefault) {
					}

					File path = new File(downPath);
					if (!path.exists())
						path.mkdir();
					JFileChooser fc = new ThumbnailFileChooser(path.getPath());
					fc.setSelectedFile(new File(path.getPath() + "/" + backupContent));
					int check = fc.showSaveDialog(null);
					fc.setVisible(true);
					downOtherName.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

					try {
						UIManager.setLookAndFeel(defaultUI);
					} catch (UnsupportedLookAndFeelException e1) {
					}

					if (check == JFileChooser.APPROVE_OPTION) {
						setDownPath(fc.getSelectedFile().getPath());
						JsonObject downInfo = new JsonObject();
						downInfo.addProperty("chatCode", chatCode);
						downInfo.addProperty("fileName", backupContent);

						try {
							out.write("REQ-DownloadFile " + downInfo + "\n");
							out.flush();
						} catch (IOException e1) {
						}
					}
				}
			});

			JLabel dot = new JLabel("·");
			dot.setFont(new Font("맑은 고딕", Font.BOLD, 12));

			JPanel downPanel = new JPanel();
			downPanel.setBackground(Color.WHITE);
			downPanel.add(downFile);
			downPanel.add(dot);
			downPanel.add(downOtherName);
			downPanel.setMinimumSize(new Dimension(60, 35));

			JPanel totalPanel = new JPanel(new BorderLayout());
			totalPanel.setBackground(Color.WHITE);
			totalPanel.add(chatContent, BorderLayout.NORTH);
			totalPanel.add(downPanel, BorderLayout.SOUTH);

			chatBalloon.remove(chatContent);
			chatBalloon.add(totalPanel);

			balloonSize += 35;
		}

		// 이미지인 경우 띄우기
		else if (type.equals("image")) {

			double width = image.getIconWidth();
			double height = image.getIconHeight();
			while (width > 200) {
				width *= 0.95;
				height *= 0.95;
			}
			width = Math.round(width);
			height = Math.round(height);
			Image sized = image.getImage().getScaledInstance((int) width, (int) height, Image.SCALE_SMOOTH);
			ImageIcon icon = new ImageIcon(sized);

			JPanel imagePanel = new JPanel(new BorderLayout());
			imagePanel.setBorder(null);
			imagePanel.setPreferredSize(new Dimension((int) width, (int) height));
			imagePanel.setBackground(Color.WHITE);
			JLabel imgLabel = new JLabel(icon);
			imagePanel.add(imgLabel, BorderLayout.NORTH);

			chatBalloon.remove(chatContent);
			chatBalloon.add(imagePanel);

			// 팝업창
			JMenuItem imageDown = new JMenuItem("이미지 저장");
			imageDown.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
			imageDown.setIconTextGap(5);
			imageDown.setBackground(Color.WHITE);
			imageDown.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JsonObject downInfo = new JsonObject();
					downInfo.addProperty("chatCode", chatCode);
					downInfo.addProperty("fileName", backupContent);

					try {
						out.write("REQ-DownloadFile " + downInfo + "\n");
						out.flush();
					} catch (IOException e1) {
					}
				}
			});

			JMenuItem imageOtherName = new JMenuItem("다른 이름으로 저장");
			imageOtherName.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
			imageOtherName.setIconTextGap(5);
			imageOtherName.setBackground(Color.WHITE);
			imageOtherName.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					LookAndFeel defaultUI = null;
					try {
						defaultUI = UIManager.getLookAndFeel();
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (Exception useDefault) {
					}

					File path = new File(downPath);
					if (!path.exists())
						path.mkdir();
					JFileChooser fc = new ThumbnailFileChooser(path.getPath());
					fc.setSelectedFile(new File(path.getPath() + "/" + backupContent));
					int check = fc.showSaveDialog(null);
					fc.setVisible(true);

					try {
						UIManager.setLookAndFeel(defaultUI);
					} catch (UnsupportedLookAndFeelException e1) {
					}

					if (check == JFileChooser.APPROVE_OPTION) {
						setDownPath(fc.getSelectedFile().getPath());
						JsonObject downInfo = new JsonObject();
						downInfo.addProperty("chatCode", chatCode);
						downInfo.addProperty("fileName", backupContent);

						try {
							out.write("REQ-DownloadFile " + downInfo + "\n");
							out.flush();
						} catch (IOException e1) {
						}
					}
				}

			});

			JPopupMenu menu = new JPopupMenu();
			menu.setBackground(Color.WHITE);
			menu.add(imageDown);
			menu.add(imageOtherName);

			chatBalloon.setComponentPopupMenu(menu);

			balloonSize = (int) height;
		}

		// 프로필, 이름, 말풍선 하나로 묶기
		JPanel allInPanel = new JPanel(new GridBagLayout());
		allInPanel.setBackground(skyBlue);
		GridBagConstraints con = new GridBagConstraints();

		// 내 채팅 말풍선은 프로필 사진이 오른쪽에 있음
		if (myID.equals(senderID)) {
			name.setText("나");
			con.gridx = 2;
			con.gridy = 0;
			con.gridheight = 2;
			con.anchor = GridBagConstraints.NORTH;
			allInPanel.add(profile, con);
			con.gridx = 0;
			con.gridy = count;
			con.anchor = GridBagConstraints.SOUTH;
			con.insets = new Insets(0, 0, 3, 5);
			con.gridheight = 1;
			allInPanel.add(time, con);
			con.gridx = 1;
			con.gridy = 0;
			con.anchor = GridBagConstraints.FIRST_LINE_END;
			con.insets = new Insets(0, 0, 3, 10);
			con.gridheight = 1;
			allInPanel.add(name, con);
			con.gridx = 1;
			con.gridy = 1;
			con.insets = new Insets(0, 0, 0, 7);
			con.ipadx = 1;
			con.gridheight = count;
			allInPanel.add(chatBalloon, con);
		} else {
			con.gridx = 0;
			con.gridy = 0;
			con.gridheight = 2;
			con.anchor = GridBagConstraints.NORTH;
			allInPanel.add(profile, con);
			con.gridx = 2;
			con.gridy = count;
			con.anchor = GridBagConstraints.SOUTH;
			con.insets = new Insets(0, 5, 5, 0);
			con.gridheight = 1;
			allInPanel.add(time, con);
			con.gridx = 1;
			con.gridy = 0;
			con.anchor = GridBagConstraints.FIRST_LINE_START;
			con.insets = new Insets(0, 7, 3, 0);
			con.gridheight = 1;
			allInPanel.add(name, con);
			con.gridx = 1;
			con.gridy = 1;
			con.ipadx = 1;
			con.insets = new Insets(0, 7, 0, 0);
			con.gridheight = count;
			allInPanel.add(chatBalloon, con);
		}

		JPanel allInWrapper = new JPanel(new BorderLayout());
		allInWrapper.setBackground(skyBlue);
		if (myID.equals(senderID)) { // 내 채팅 말풍선은 오른쪽 방향에 있음
			allInWrapper.setBorder(new EmptyBorder(0, 0, 0, 11));
			allInWrapper.add(allInPanel, BorderLayout.EAST);
		} else {
			allInWrapper.setBorder(new EmptyBorder(0, 3, 0, 0));
			allInWrapper.add(allInPanel, BorderLayout.WEST);
		}
		allInWrapper.setPreferredSize(new Dimension(allInPanel.getSize().width, 50 + balloonSize));

		// 메세지 출력창에 말풍선 추가
		JPanel messageArea = data.chats.get(chatCode).getMessageArea();
		messageArea.add(allInWrapper);

		// messageAreaWrapper 사이즈 조절
		int w = messageArea.getHeight();
		data.chats.get(chatCode).getWrap()
				.setPreferredSize(new Dimension(messageArea.getWidth(), w + 50 + balloonSize + timePanelSize));
		messageArea.setPreferredSize(new Dimension(400, w + 45 + balloonSize + timePanelSize));

		data.chats.get(chatCode).addChatNumOne();
		data.chats.get(chatCode).update("" + data.chats.get(chatCode).getChatNum());
	}

	// 채탱창 띄우기 on-off
	public void setClickable(boolean tf) {
		isClickable = tf;
	}

	/* 채팅방 채팅 내역 초기화 */
	public void initChatRoomHistory(String chatCode) throws IOException {
		out.write("REQ-ChatHistory " + chatCode + "\n");
		out.flush();
		setClickable(false);
	}

	/* UI가 이쁘게 수정된 스크롤 패널 만들어주는 메소드 */
	private JScrollPane createCustomScrollPanel(JComponent originalPanel, Color background) {
		JScrollPane scrollPanel = new JScrollPane(originalPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		// 가로축에만 스크롤이 있어야함
		// 스크롤 패널 경계선 제거
		scrollPanel.setBorder(null);
		// 스크롤 속도 업
		scrollPanel.getVerticalScrollBar().setUnitIncrement(16);
		// 스크롤바 얇아지게 수정
		scrollPanel.getVerticalScrollBar().setPreferredSize(new Dimension(7, 0));
		// 스크롤바 배경 색깔
		scrollPanel.getVerticalScrollBar().setBackground(background);

		// 기본 스크롤바 UI 수정
		setBarUI(scrollPanel, background);

		/* 스크롤바가 안 움직일 때 보이지 않게 해줌 */
		class WheelMovementTimerActionListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				setBarUI(scrollPanel, background); // 스크롤바가 마우스 휠에 의해 안 움직이면 배경색으로 변경
			}
		}
		originalPanel.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				scrollPanel.dispatchEvent(e);
				setBarUI(scrollPanel, Color.ORANGE); // 스크롤바가 움직이면 보이도록 변경
				if (Timer != null && Timer.isRunning()) {
					Timer.stop();
				}
				Timer = new Timer(500, new WheelMovementTimerActionListener());
				Timer.setRepeats(false);
				Timer.start();
			}

		});

		// UI가 수정된 스크롤바 리턴
		return scrollPanel;
	}

	/* 스크롤 패널을 이쁘게 꾸며주는 메소드 */
	private void setBarUI(JScrollPane origin, Color cl) {
		origin.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
			/* 스크롤바 위 아래 버튼 제거 */
			@Override
			protected void configureScrollBarColors() {
				this.thumbColor = Color.ORANGE;
			}

			@Override
			protected JButton createDecreaseButton(int orientation) {
				return createZeroButton();
			}

			@Override
			protected JButton createIncreaseButton(int orientation) {
				return createZeroButton();
			}

			private JButton createZeroButton() {
				JButton jbutton = new JButton();
				jbutton.setPreferredSize(new Dimension(0, 0));
				jbutton.setMinimumSize(new Dimension(0, 0));
				jbutton.setMaximumSize(new Dimension(0, 0));
				return jbutton;
			}

			/* 스크롤바 모양이랑 색깔 이쁘게 수정 */
			@Override
			protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				Color color = null;
				JScrollBar sb = (JScrollBar) c;

				if (!sb.isEnabled() || r.width > r.height)
					return;
				else if (isDragging)
					color = new Color(255, 165, 0, 200);
				else if (isThumbRollover())
					color = new Color(255, 165, 0, 200);
				else
					color = cl;

				g2.setPaint(color);
				g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
				g2.setPaint(new Color(255, 255, 255, 0));
				g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
				g2.dispose();
			}
		});
	}

}
