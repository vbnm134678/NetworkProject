package Client;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import Client.Collections;

public class ChatterClient {

	// 포트넘버
	private static String server_id;
	private static int port_num;
	private static Scanner inputStream;
	// 내 개인정보
	private static String myID;
	private static String myName;
	private static String myState;
	private static String myLast;
	private static JsonObject myInfo;
	private static ImageIcon myProfile;

	// 서버와 연결된 스트림
	static Socket server = null;
	private static BufferedReader in = null;
	private static BufferedWriter out = null;

	// 친구들 정보
	private static HashMap<String, JsonObject> friendInfoList = new HashMap<String, JsonObject>();
	// 친구들 (ID:이름) 정보 정렬된 버전
	private static TreeMap<String, String> friendNameList = new TreeMap<String, String>();
	// 친구들 프로필 사진 저장
	private static HashMap<String, ImageIcon> friendProfileList = new HashMap<String, ImageIcon>();

	// 채팅방 정보 리스트
	private static HashMap<String, JsonObject> myChatRoomInfo = new HashMap<String, JsonObject>();

	// JSON 변형하기 위해 필요
	private static Gson JSON = new Gson();

	public static void main(String[] args) throws InterruptedException, IOException {
		//login서버 포트넘버, chat서버 포트넘버를 파일로부터 읽음
		try {
			inputStream = new Scanner(new FileReader("src/Server/ServerInfo.txt"));
		} catch (FileNotFoundException e) {
			System.out.println("File not Found");
			System.exit(0);
		}
		server_id = inputStream.nextLine();
		port_num = inputStream.nextInt();
		System.out.println(server_id);
		System.out.println(port_num);
		ChatterClient client = new ChatterClient();
		client.run_log();
		client.run_chat();
	}

	// Login시 필요한 변수들
	static String file_server;
	static String serverIP;
	static int serverPort;

	static Scanner ins;
	static PrintWriter outs;

	JFrame frame_log;
	JFrame frame_find;
	JFrame frame_sign;

	JTextField bolN;
	JTextField bolI;

	static String Nickname = "";
	static String Id = "";
	static String Password = "";
	static String Address = "";
	static String Birth = "";

	int check = 0;

	// 비밀번호 찾기 gui
	private void forgotGUI() {
		frame_find = new JFrame("Forgot");
		frame_find.getContentPane().setBackground(new Color(255, 200, 0));
		frame_find.setLayout(null);
		frame_find.setBounds(600, 300, 400, 250);
		ImageIcon icon = new ImageIcon("resources/MainIcon.png");
		Image image = icon.getImage().getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH);
		frame_find.setIconImage(image);
		
		JLabel UN = new JLabel("Id :");
		UN.setBounds(50, 60, 250, 25);
		JTextField ID = new JTextField(20);
		ID.setBounds(140, 60, 200, 25);
		JLabel UE = new JLabel("E-mail :");
		UE.setBounds(50, 90, 250, 25);
		JTextField EM = new JTextField(30);
		EM.setBounds(140, 90, 200, 25);
		JButton check = new JButton("Check");
		check.setBounds(160, 130, 70, 25);
		frame_find.add(UN);
		frame_find.add(ID);
		frame_find.add(UE);
		frame_find.add(EM);
		frame_find.add(check);
		frame_find.setVisible(true);

		check.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Id = ID.getText();
				Address = EM.getText();
				outs.println("FIND");
			}
		});

	}

	// 회원가입 gui
	private void registerGUI() {
		frame_sign = new JFrame("Sign Up");
		frame_sign.getContentPane().setBackground(new Color(255, 200, 0));
		frame_sign.setLayout(null);
		frame_sign.setBounds(600, 300, 500, 400);
		ImageIcon icon = new ImageIcon("resources/MainIcon.png");
		Image image = icon.getImage().getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH);
		frame_sign.setIconImage(image);
		
		JLabel NN = new JLabel("Nickname :");
		NN.setBounds(70, 50, 250, 25);
		JTextField NI = new JTextField(20);
		NI.setBounds(160, 50, 200, 25);
		JButton checkN = new JButton("Duplicate");
		checkN.setBounds(380, 50, 90, 25);
		bolN = new JTextField(20);
		bolN.setEditable(false);
		bolN.setBorder(null);
		bolN.setForeground(Color.red);
		bolN.setBounds(160, 80, 200, 25);
		JLabel UN = new JLabel("Id :");
		UN.setBounds(70, 110, 250, 25);
		JTextField ID = new JTextField(20);
		ID.setBounds(160, 110, 200, 25);
		JButton checkI = new JButton("Duplicate");
		checkI.setBounds(380, 110, 90, 25);
		bolI = new JTextField(20);
		bolI.setEditable(false);
		bolI.setBorder(null);
		bolI.setForeground(Color.red);
		bolI.setBounds(160, 140, 200, 25);
		JLabel UP = new JLabel("Password :");
		UP.setBounds(70, 170, 250, 25);
		JPasswordField PW = new JPasswordField(20);
		PW.setEchoChar('*');
		PW.setBounds(160, 170, 200, 25);
		JLabel UE = new JLabel("E-mail :");
		UE.setBounds(70, 200, 250, 25);
		JTextField EM = new JTextField(30);
		EM.setBounds(160, 200, 200, 25);
		JLabel BI = new JLabel("Birth :");
		BI.setBounds(70, 230, 250, 25);
		JTextField BD = new JTextField(20);
		BD.setBounds(160, 230, 200, 25);
		JButton SG = new JButton("Sign Up");
		SG.setBounds(190, 280, 100, 25);

		frame_sign.add(NN);
		frame_sign.add(NI);
		frame_sign.add(checkN);
		frame_sign.add(bolN);
		frame_sign.add(UN);
		frame_sign.add(ID);
		frame_sign.add(checkI);
		frame_sign.add(bolI);
		frame_sign.add(UP);
		frame_sign.add(PW);
		frame_sign.add(UE);
		frame_sign.add(EM);
		frame_sign.add(BI);
		frame_sign.add(BD);
		frame_sign.add(SG);
		frame_sign.setVisible(true);

		checkN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Nickname = NI.getText();
				outs.println("CHECKNICK");
			}
		});

		checkI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Id = ID.getText();
				outs.println("CHECKID");
			}
		});

		SG.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Nickname = NI.getText();
				Id = ID.getText();
				Password = new String(PW.getPassword());
				Address = EM.getText();
				Birth = BD.getText();
				if (bolN.getText().equalsIgnoreCase("Already Exist") || bolN.getText().equals(null)) {
					JOptionPane.showMessageDialog(null, "Check Nickname", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (bolI.getText().equalsIgnoreCase("Already Exist") || bolI.getText().equals(null)) {
					JOptionPane.showMessageDialog(null, "Check Id", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					outs.println("SIGN");
				}
			}
		});

	}

	// 로그인창 gui
	public void LoginGUI() throws UnknownHostException, IOException {
		frame_log = new JFrame("Login");
		frame_log.getContentPane().setBackground(new Color(255, 200, 0));
		frame_log.setLayout(null);
		frame_log.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame_log.setBounds(600, 300, 540, 330);
		ImageIcon icon = new ImageIcon("resources/MainIcon.png");
		Image image = icon.getImage().getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH);
		frame_log.setIconImage(image);

		JLabel text = new JLabel("    Login    ");
		text.setOpaque(true);
		text.setBackground(Color.white);
		text.setBorder(new TitledBorder(new LineBorder(Color.black, 2)));
		text.setBounds(28, 22, 60, 25);

		JPanel LOGIN = new JPanel();
		LOGIN.setLayout(null);
		LOGIN.setBorder(new TitledBorder(new LineBorder(Color.black, 2)));
		LOGIN.setBounds(20, 45, 484, 230);
		LOGIN.setBackground(Color.white);
		JLabel UN = new JLabel("Id :");
		UN.setBounds(80, 60, 250, 25);
		JTextField ID = new JTextField(20);
		ID.setBounds(170, 62, 200, 25);
		JLabel UP = new JLabel("Password :");
		UP.setBounds(80, 95, 250, 25);
		JPasswordField PW = new JPasswordField(20);
		PW.setEchoChar('*');
		PW.setBounds(170, 97, 200, 25);
		JButton login = new JButton("Login");
		login.setForeground(Color.WHITE);
		login.setBackground(Color.blue);
		login.setBounds(340, 140, 70, 25);

		JButton forgot = new JButton("Forgot");
		forgot.setBorder(null);
		forgot.setBackground(Color.white);
		forgot.setBounds(20, 200, 60, 25);
		JButton register = new JButton("Sign Up");
		register.setBorder(null);
		register.setBackground(Color.white);
		register.setBounds(390, 200, 70, 25);

		LOGIN.add(UN);
		LOGIN.add(ID);
		LOGIN.add(UP);
		LOGIN.add(PW);
		LOGIN.add(login);
		LOGIN.add(forgot);
		LOGIN.add(register);
		frame_log.add(text);
		frame_log.add(LOGIN);
		frame_log.setVisible(true);

		login.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Id = ID.getText();
				Password = new String(PW.getPassword());
				if (!Password.trim().equals("")) {
					outs.println("LOGIN");
				}
			}
		});
		forgot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				forgotGUI();
			}
		});
		register.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				registerGUI();
			}
		});
	}

	public void run_log() throws IOException {
		try {
			Socket socket = new Socket(server_id, port_num);
			ins = new Scanner(socket.getInputStream());
			outs = new PrintWriter(socket.getOutputStream(), true);
			
			outs.write("REQ-LogIn\n");
			outs.flush();
			
			LoginGUI();
			while (ins.hasNextLine()) {
				String line = ins.nextLine();
				// 로그인
				if (line.startsWith("LOGININFO")) {
					//암호화해서 서버에 전송
					String encryption = "";
					for(int i = 0;i < Password.length();i++) {
						encryption += (char) (Password.charAt(i) + 3);
					}
					outs.println(Id + "," + encryption);
				}
				// id없음
				else if (line.startsWith("WRONGID")) {
					JOptionPane.showMessageDialog(null, "Wrong Id.", "Error", JOptionPane.PLAIN_MESSAGE);
				}
				// password 없음
				else if (line.startsWith("WRONGPASSWORD")) {
					JOptionPane.showMessageDialog(null, "Wrong Password.", "Error", JOptionPane.PLAIN_MESSAGE);
				}
				// password 찾기
				else if (line.startsWith("FINDINFO")) {
					outs.println(Id + "," + Address);
				}
				// password 보여주기
				else if (line.startsWith("PW")) {
					//암호화 해제후 출력
					String deEncryption = "";
					for(int i = 2;i < line.length();i++) {
						deEncryption += (char) (line.charAt(i) - 3);
					}
					JOptionPane.showMessageDialog(null, "Password : " + deEncryption, null,
							JOptionPane.PLAIN_MESSAGE);
					frame_find.setVisible(false);
					frame_find.dispose();
				}
				// id 중복체크
				else if (line.startsWith("ID")) {
					outs.println(Id);
				}
				// nickname 중복체크
				else if (line.startsWith("NICK")) {
					outs.println(Nickname);
				} else if (line.startsWith("SAMEID")) {
					bolI.setText("Already Exist");
				} else if (line.startsWith("UNIQUEID")) {
					bolI.setText("Available");
				} else if (line.startsWith("SAMENICK")) {
					bolN.setText("Already Exist");
				} else if (line.startsWith("UNIQUENICK")) {
					bolN.setText("Available");
				}
				// 새로 회원가입
				else if (line.startsWith("NEWSIGN")) {
					//암호화해서 서버에 전송
					String encryption = "";
					for(int i = 0;i < Password.length();i++) {
						encryption += (char) (Password.charAt(i) + 3);
					}
					outs.println(Id + "," + Nickname + "," + encryption + "," + Address + "," + Birth);
					frame_sign.setVisible(false);
					frame_sign.dispose();
				} else if (line.startsWith("SUCCESS")) {
					outs.println("THANKs");
					JOptionPane.showMessageDialog(null, "Login Success", null, JOptionPane.PLAIN_MESSAGE);
					break;
				}
			}
		} finally {
			myID = Id;
			frame_log.setVisible(false);
			frame_log.dispose();
		}
	}

	public void run_chat() throws InterruptedException {
		try {

			// 서버와 연결
			server = new Socket(server_id, port_num);
			System.out.println("서버 연결 완료...\n: port#/" + server.getLocalPort());
			in = new BufferedReader(new InputStreamReader((server.getInputStream())));
			out = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));

			out.write("REQ-Chatting\n");
			out.flush();
			
			// 서버로부터 ID 요청
			String line = null;
			while (true) {
				System.out.println();
				line = in.readLine();
				System.out.println("메세지 도착: " + line);

				/* 로그인 대체 */
				// 아이디 달라는 뜻
				if (line.startsWith("REQ-ID")) {
					out.write(myID + "\n");
					out.flush();
					System.out.println("서버에 ID 전송: " + myID);
				}

				// 아이디 잘 받았다는 뜻 - 내 정보 받기
				else if (line.startsWith("RES-AcceptID")) {
					System.out.println("내 정보 받아오기");
					String myJson = line.substring(13);
					myInfo = JSON.fromJson(myJson, JsonObject.class);
					myName = myInfo.get("name").getAsString();
					myState = myInfo.get("state").getAsString();
					System.out.println(myState);
					myLast = myInfo.get("last").getAsString();
				}

				// 프로필 사진 받기
				else if (line.startsWith("RES-YesProfile")) {
					myProfile = receiveImage();
					System.out.println("프로필 사진 확인");
					break;
				} else if (line.startsWith("RES-NoProfile")) {
					System.out.println("프로필 사진 X");
					break;
				}

			}
			System.out.println("\n로그인 완료\n");

			// 클라이언트 UI 구성
			ChatterUI chatter = new ChatterUI(server);
			chatter.addMyPanel(myID, myName, myProfile, myState, myLast);
			new Thread(new Weather(chatter)).start();

			// 친구 목록 불러오기
			initFriendList(chatter);

			// 채팅 정보 불러오기
			initChatRoom(chatter);
			System.out.println("\n 기본정보 초기화 완료\n\n");

			/* 초기화 이후 */
			handleActiveResponse(chatter); // 서버에서 처리한 여러가지 요청 결과 받기

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* 프로그램 시작시 친구 정보 초기화 */
	private static void initFriendList(ChatterUI UI) throws IOException {

		out.write("REQ-FriendList\n"); // 서버에 친구정보 요청
		out.flush();
		System.out.println("\n친구 목록 요청:");

		String line = in.readLine(); // 서버에서 응답 받기

		if (line.startsWith("RES-FriendList")) {
			String friendJson = line.substring(15); // JSON 형태로 친구 리스트 받아오기
			JsonArray friendJsonArray = JSON.fromJson(friendJson, JsonArray.class);

			// 친구들을 패널에 추가, 친구가 없으면 루프 종료
			for (int i = 0; i < friendJsonArray.size(); i++) {
				String friendID = friendJsonArray.get(i).getAsJsonObject().get("id").getAsString();
				JsonObject infoObj = null;
				String friendName = null;
				String state = null;
				String last = null;

				// 친구 상세 정보 요청
				out.write("REQ-FriendInfo " + friendID + "\n");
				out.flush();
				System.out.println("친구 상세 정보 요청");
				line = in.readLine();

				if (line.startsWith("RES-FriendInfo")) {
					String info = line.substring(15);
					infoObj = JSON.fromJson(info, JsonObject.class);
					friendName = infoObj.get("name").getAsString();
					state = infoObj.get("state").getAsString();
					last = infoObj.get("last").getAsString();
				}

				out.write("REQ-FriendProfile " + friendID + "\n");
				out.flush();
				System.out.println("친구 프로필 사진 요청");
				line = in.readLine();

				// 친구 프로필 사진 있으면 받기
				ImageIcon icon = null;
				if (line.startsWith("RES-YesFriendProfile"))
					icon = receiveImage();

				// 친구들 정보들 따로 저장
				friendNameList.put(friendID, friendName);
				friendInfoList.put(friendID, infoObj);
				friendProfileList.put(friendID, icon);
			}

			// UI에 친구 정보 패널 추가
			for (Entry<String, String> sortedMap : Collections.entriesSortedByValues(friendNameList)) {
				JsonObject friendInfo = friendInfoList.get(sortedMap.getKey());
				String friendID = friendInfo.get("id").getAsString();
				String friendName = friendInfo.get("name").getAsString();
				String state = friendInfo.get("state").getAsString();
				String last = friendInfo.get("last").getAsString();
				ImageIcon icon = friendProfileList.get(friendID);

				UI.addNewFriend(friendID, friendName, icon, state, last);
			}
		}
	}

	/* 프로그램 시작시 채팅 정보 초기화 */
	private static void initChatRoom(ChatterUI UI) throws IOException {

		out.write("REQ-ChatRoomList\n"); // 서버에 채팅방 리스트 요청
		out.flush();
		System.out.println("\n채팅방 목록 요청:");

		String line = in.readLine(); // 내가 포함되어있는 채팅방 리스트

		if (line.startsWith("RES-ChatRoomList")) {
			String chatRoomJson = line.substring(17);
			JsonArray chatRoomList = JSON.fromJson(chatRoomJson, JsonArray.class);

			// 채팅방 수 만큼 채팅방 정보 요청
			for (int i = 0; i < chatRoomList.size(); i++) {

				// 서버에 해당 채팅방 정보 요청
				out.write("REQ-ChatRoomInfo " + chatRoomList.get(i).getAsString() + "\n");
				out.flush();
				System.out.println("\n채팅방 정보 요청: " + chatRoomList.get(i).getAsString());

				// 응답 대기
				System.out.println();
				String response = in.readLine();
				System.out.println("서버로부터의 응답: " + response);

				if (response.startsWith("RES-ChatRoomInfo")) {
					String json = response.substring(17); // 받은 json을 json object로 변환
					JsonObject chatRoomInfo = JSON.fromJson(json, JsonObject.class);
					String chatCode = chatRoomInfo.get("chatCode").getAsString(); // 정보 뽑아내기
					String chatName = chatRoomInfo.get("name").getAsString();

					myChatRoomInfo.put(chatCode, chatRoomInfo); // Json Object를 리스트에 추가
					UI.addNewChatRoomPanel(chatCode, chatName); // UI에 있는 패널도 새로 생성
				}

			}
		}
	}

	//
	/* 사용자 활동에 대한 응답 처리 */
	private static void handleActiveResponse(ChatterUI UI) throws IOException, InterruptedException {

		while (true) {
			System.out.println();
			String line = in.readLine(); // 서버로부터의 응답
			System.out.println("서버로부터: " + line);

			if (line.startsWith("RES-UserInfo")) { // 유저 검색
				String subline = line.substring(13);

				// 만약 검색한 아이디가 존재하지 않는다면
				if (subline.startsWith("NotFound")) {
					UI.setSearchedPanel("그 아이디는 존재하지 않습니다", "넹", "\n", null, false);
					System.out.println("해당 유저 존재 X"); // serchedPanel에 없다고 표시
					// 그 유저가 존재한다면
				} else {	// TODO
					JsonObject info = JSON.fromJson(subline, JsonObject.class);
					String name = info.get("name").getAsString();
					String id = info.get("id").getAsString();
					String state = info.get("state").getAsString();
					ImageIcon icon = null;
					
					line = in.readLine();
					System.out.println(line);
					if (line.startsWith("RES-YesFriendProfile")) {
						byte[] oneByte = new byte[8192];
						int bytesRead;

						InputStream fileInput = server.getInputStream();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();

						do {
							bytesRead = fileInput.read(oneByte, 0, oneByte.length);
							baos.write(oneByte, 0, bytesRead);
						} while (bytesRead == 8192);

						icon = new ImageIcon(baos.toByteArray());
					}
					
					// searchedPanel에 그 유저 정보 표시
					UI.setSearchedPanel(name, id, state, icon, true);
					System.out.println("해당 유저 존재: " + subline);
				}
			}

			// 친구 새로 추가
			if (line.startsWith("RES-NewFriend")) {
				String friendJson = line.substring(14);

				JsonObject newFriendObj = JSON.fromJson(friendJson, JsonObject.class);
				String newFriendName = newFriendObj.get("name").getAsString();
				String newFriendID = newFriendObj.get("id").getAsString();
				System.out.println("친구 추가: " + newFriendID);

				// 친구 목록에 없다면
				if (!friendNameList.containsKey(newFriendID)) {
					// 친구 목록 업데이트
					friendNameList.put(newFriendID, newFriendName);

					out.write("REQ-FriendProfile " + newFriendID + "\n");
					out.flush();
					System.out.println("친구 프로필 사진 요청");
					line = in.readLine();

					// 친구 프로필 사진 있으면 받기
					ImageIcon icon = null;
					if (line.startsWith("RES-YesFriendProfile"))
						icon = receiveImage();

					// 친구들 정보들 따로 저장
					friendInfoList.put(newFriendID, newFriendObj);
					friendNameList.put(newFriendID, newFriendName);
					friendProfileList.put(newFriendID, icon);
				}
				// UI에 친구 정보 패널 정렬해서 업데이트
				for (Entry<String, String> sortedMap : Collections.entriesSortedByValues(friendNameList)) {
					if (!newFriendID.equals(sortedMap.getKey()))
						UI.removeFriendPanel(sortedMap.getKey());

					JsonObject friendInfo = friendInfoList.get(sortedMap.getKey());
					String id = friendInfo.get("id").getAsString();
					String name = friendInfo.get("name").getAsString();
					String state = friendInfo.get("state").getAsString();
					String last = friendInfo.get("last").getAsString();
					ImageIcon icon = friendProfileList.get(id);

					UI.addNewFriend(id, name, icon, state, last);
				}
			}

			// 친구 제거 확인
			else if (line.startsWith("RES-RemoveFriend")) {
				String json = line.substring(17);
				JsonObject notFriend = JSON.fromJson(json, JsonObject.class);
				friendInfoList.remove(notFriend.get("id").getAsString());
				friendProfileList.remove(notFriend.get("id").getAsString());
				friendNameList.remove(notFriend.get("id").getAsString());
			}

			// 채팅방 생성 완료
			else if (line.startsWith("RES-NewChatRoom")) {
				String chatRoomInfoJson = line.substring(16);
				JsonObject infoObj = JSON.fromJson(chatRoomInfoJson, JsonObject.class);

				// 정보 뽑아내기
				String chatCode = infoObj.get("chatCode").getAsString();
				String chatName = infoObj.get("name").getAsString();

				// UI
				if (!myChatRoomInfo.containsKey(chatCode)) {
					UI.addNewChatRoomPanel(chatCode, chatName);
					// 정보를 리스트에 추가
					myChatRoomInfo.put(chatCode, infoObj);
				}
			}

			// 이미 존재하는 채팅방
			else if (line.startsWith("RES-ExistChatRoom")) {
				String json = line.substring(18);
				JsonObject infoObj = JSON.fromJson(json, JsonObject.class);

				// 정보 뽑아내기
				String chatCode = infoObj.get("chatCode").getAsString();
				String chatName = infoObj.get("name").getAsString();

				// UI
				if (!myChatRoomInfo.containsKey(chatCode)) {
					UI.addNewChatRoomPanel(chatCode, chatName);
					// 정보를 리스트에 추가
					myChatRoomInfo.put(chatCode, infoObj);
				}
			}

			// 채팅방 나가기
			else if (line.startsWith("RES-LeaveChatRoom")) {
				String chatCode = line.substring(18);
				myChatRoomInfo.remove(chatCode);
				System.out.println("채팅방 나가기 완료: " + chatCode);
			}

			// 채팅 하나 새로 추가
			else if (line.startsWith("RES-Chat")) {
				String json = line.substring(9);

				// json 해석
				JsonObject chatObj = JSON.fromJson(json, JsonObject.class);
				String chatCode = chatObj.get("chatCode").getAsString();
				String senderID = chatObj.get("senderID").getAsString();
				String senderName = chatObj.get("senderName").getAsString();
				String chatContent = chatObj.get("content").getAsString();
				String time = chatObj.get("time").getAsString();
				String type = chatObj.get("type").getAsString();

				ImageIcon icon = null;
				if (type.equals("image")) {
					byte[] oneByte = new byte[8192];
					int bytesRead;

					InputStream fileInput = server.getInputStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					
					do {
						bytesRead = fileInput.read(oneByte, 0, oneByte.length);
						baos.write(oneByte, 0, bytesRead);
					} while (bytesRead == 8192);

					icon = new ImageIcon(baos.toByteArray());
				}
				// UI에 표시
				UI.addNewChat(chatCode, senderID, senderName, chatContent, time, type, icon);

				// 콘솔에 표시
				System.out.println("새로운 채팅>> " + senderName + "(" + senderID + ") : " + chatContent);
				UI.update(chatContent);
			}
			
			else if (line.startsWith("RES-END")) {
				UI.setClickable(true);
			}

			// 파일 다운로드
			else if (line.startsWith("RES-DownloadFile")) {
				String fileName = line.substring(17);
				String extension = fileName.substring(fileName.lastIndexOf('.'));

				byte[] oneByte = new byte[8192];
				int bytesRead;

				InputStream fileInput = server.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				BufferedOutputStream bos = null;
				if (UI.isDirChanged()) {
					String downPath = UI.getDownPath();
					String temp = downPath.substring(downPath.lastIndexOf('/') + 1);
					if (!temp.contains("."))
						downPath += extension;
					bos = new BufferedOutputStream(
							new FileOutputStream(downPath));
				}
				else {
					File path = new File("C:/Users/" + System.getProperty("user.name") + "/Documents/채터 다운로드");
					if (!path.exists())
						path.mkdir();
					bos = new BufferedOutputStream(
							new FileOutputStream(path.getPath() + "/" + fileName));
				}
				

				do {
					bytesRead = fileInput.read(oneByte);
					baos.write(oneByte, 0, bytesRead);
				} while (bytesRead == 8192);

				bos.write(baos.toByteArray());
				bos.flush();
				bos.close();
				UI.setDefaultDownPath();
				
				System.out.println("파일 다운 완료");
			}
			
			// 상태 메세지 변경
			else if (line.startsWith("RES-UpdateState")) {
				String json = line.substring(16);
				JsonObject info = JSON.fromJson(json, JsonObject.class);
				String friendID = info.get("id").getAsString();
				String stateMessage = info.get("state").getAsString();
				
				UI.updateState(friendID, stateMessage);
				System.out.println("상태 메세지 변경 완료: " + friendID + "-" + stateMessage);
			}
			
			else if (line.startsWith("RES-UpdateProfile")) {
				String check = line.substring(17, 20);
				String friendID = line.substring(21);

				ImageIcon icon = null;
				if (check.equals("Yes")) {
					byte[] oneByte = new byte[8192];
					int bytesRead;

					InputStream fileInput = server.getInputStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();

					do {
						bytesRead = fileInput.read(oneByte, 0, oneByte.length);
						baos.write(oneByte, 0, bytesRead);
					} while (bytesRead == 8192);

					icon = new ImageIcon(baos.toByteArray());
				}
				
				UI.updateProfile(friendID, icon);
				System.out.println("프로필 사진 업데이트 완료: " + friendID);
			}

		}

	}

	// 이미지 받기
	private static ImageIcon receiveImage() throws IOException {
		InputStream inputStream = server.getInputStream();

		byte[] sizeAr = new byte[4];
		inputStream.read(sizeAr);
		int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();

		byte[] imageAr = new byte[size];
		inputStream.read(imageAr);

		BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAr));
		ImageIcon icon = null;
		if (image != null)
			icon = new ImageIcon(image);
		return icon;
	}

}
