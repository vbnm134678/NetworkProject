package Server;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import Client.ImageConverter;

public class ChatterServer {

	// 회원가입한 모든 유저의 ID
	private static TreeSet<String> Users = new TreeSet<String>();
	// 온라인 접속자 리스트 (id : output stream)
	private static HashMap<String, Socket> online = new HashMap<String, Socket>();
	// 채팅방 코드 리스트
	private static TreeSet<String> chatRooms = new TreeSet<String>();

	public static void main(String[] args) {

		ServerSocket listener;
		try {
			BufferedWriter gUserListWriter = new BufferedWriter(new FileWriter("users/user.txt", true));
			gUserListWriter.append(" ".trim());
			gUserListWriter.close();
			
			// 유저목록 불러오기
			Scanner userList = new Scanner(new File("users/@UserList.txt"));
			while (userList.hasNextLine()) {
				String userID = userList.nextLine();
				Users.add(userID);
			}
			userList.close();

			File chatRoomsFolder = new File("chatRooms");
			if (!chatRoomsFolder.exists())
				chatRoomsFolder.mkdir();
			
			BufferedWriter ccListWriter = new BufferedWriter(new FileWriter("chatRooms/@chatCodeList.txt", false));
			ccListWriter.append(" ".trim());
			ccListWriter.close();
			
			// 채팅 목록 불러오기
			Scanner chatroomsList = new Scanner(new File("chatRooms/@chatCodeList.txt"));
			while (chatroomsList.hasNextLine()) {
				String aChatCode = chatroomsList.nextLine();
				chatRooms.add(aChatCode);
			}
			chatroomsList.close();

			// 유저 목록 확인
			System.out.println("회원가입한 유저 리스트:\n" + Users + "\n");

			// 서버 생성
			listener = new ServerSocket(50505);
			System.out.println("Server is Running...");
			// 동접 500명 제한
			ExecutorService thread = Executors.newFixedThreadPool(500);

			while (true) {
				Socket newClient = listener.accept();
				// 새로운 접속자
				System.out.println("\nNew Client: IP" + newClient.getInetAddress() + " Port#/" + newClient.getPort());
				
				
				thread.execute(new totalServer(newClient));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static class totalServer implements Runnable {
		
		private Socket socket;
		
		public totalServer(Socket client) {
			socket = client;
		}
		
		@Override
		public void run() {
			BufferedReader reader;
			try {
				reader = new BufferedReader(new InputStreamReader((socket.getInputStream())));
				String line = reader.readLine();
				
				if (line.startsWith("REQ-LogIn")) {
					new Thread(new Handler(socket)).start();
				}
				else if (line.startsWith("REQ-Chatting")) {
					new Thread(new Server(socket)).start();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			
		}
	}
	
	private static class Handler implements Runnable {

		private static BufferedReader inputStream = null;
		private static PrintWriter outputStream = null;
		private static int user;
		
		private Socket socket;
		private Scanner in;
		private PrintWriter out;
		
		static Gson g = new Gson();

		public Handler(Socket socket) {
			this.socket = socket;

		}

		public void run() {
			try {

				// 소켓 스트림
				in = new Scanner(socket.getInputStream());
				out = new PrintWriter(socket.getOutputStream(), true);
				String line;
				while (true) {
					try {
						File user = new File("users");
						if (!user.exists())
							user.mkdir();
						
						outputStream = new PrintWriter(new FileWriter(
								"users/user.txt", true));
						outputStream.append(" ".trim());
						
						inputStream = new BufferedReader(new FileReader(
								"users/user.txt"));
					} catch (FileNotFoundException e) {
						System.out.println("There's no file");
					}
					
					// 텍스트파일 한줄씩 읽기
					ArrayList<String> userList = new ArrayList<String>();
					String aline = inputStream.readLine();
					user = 0;
					while(aline != null) {
						user++;
						userList.add(aline);
						aline = inputStream.readLine();
					}
					String[] js = userList.toArray(new String[0]);	

					// 읽은 String을 JsonObject로 변환
					JsonObject[] obj = new JsonObject[user];
					for (int i = 0; i < user; i++) {
						obj[i] = g.fromJson(js[i], JsonObject.class);
					}
					line = in.nextLine();
					if (line.startsWith("LOGIN")) {
						out.println("LOGININFO");
						line = in.nextLine();
						String[] info = line.split(",");
						// info[0]는 id info[1]은 password
						for (int i = 0; i < user; i++) {
							// id, password 둘다 같으면 login성공
							if (info[0].equals(obj[i].get("id").getAsString())
									&& info[1].equals(obj[i].get("password").getAsString())) {
								obj[i].addProperty("condi", "yes");
								out.println("SUCCESS");
								break;
							} else if (info[0].equals(obj[i].get("id").getAsString())
									&& !info[1].equals(obj[i].get("password").getAsString())) {
								out.println("WRONGPASSWORD");
								break;
							} else if (i == user - 1) {
								out.println("WRONGID");
							}
						}
					}
					// 비밀번호 찾기
					else if (line.startsWith("FIND")) {
						out.println("FINDINFO");
						line = in.nextLine();
						String[] info = line.split(",");
						for (int i = 0; i < user; i++) {
							if (info[0].equals(obj[i].get("id").getAsString())
									&& info[1].equals(obj[i].get("address").getAsString()))
								out.println("PW" + obj[i].get("password").getAsString());
						}
					}
					// id중복체크
					else if (line.startsWith("CHECKID")) {
						out.println("ID");
						line = in.nextLine();
						for (int i = 0; i < user; i++) {
							if (line.equals(obj[i].get("id").getAsString())) {
								out.println("SAMEID");
								break;
							} else if (i == user - 1)
								out.println("UNIQUEID");
						}
					}
					// nickname 중복체크
					else if (line.startsWith("CHECKNICK")) {
						out.println("NICK");
						line = in.nextLine();
						for (int i = 0; i < user; i++) {
							if (line.equals(obj[i].get("nickname").getAsString())) {
								out.println("SAMENICK");
								break;
							} else if (i == user - 1)
								out.println("UNIQUENICK");
						}
					}
					// 회원가입
					else if (line.startsWith("SIGN")) {
						out.println("NEWSIGN");
						line = in.nextLine();
						String[] info = line.split(",");
						String newUser = "{\"id\":" + "\"" + info[0] + "\"" + "," + "\"nickname\":" + "\"" + info[1]
								+ "\"" + "," + "\"password\":" + "\"" + info[2] + "\"" + "," + "\"address\":" + "\""
								+ info[3] + "\"" + "," + "\"birth\":" + "\"" + info[4] + "\"" + ","
								+ "\"condi\":\"no\"}";
						outputStream.println(newUser);
						user++;
						outputStream.close();
						inputStream.close();
						
						// 채팅 부분이랑 호환되게 수정 TODO
						JsonObject chatObj = new JsonObject();
						chatObj.addProperty("id", info[0]);
						chatObj.addProperty("name", info[1]);
						chatObj.add("friend", new JsonArray());
						chatObj.add("chat", new JsonArray());
						chatObj.addProperty("state", "");
						String logOutTime = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(Calendar.getInstance().getTime());
						chatObj.addProperty("last", logOutTime);

						BufferedWriter userListWriter = new BufferedWriter(new FileWriter(
								"users/@UserList.txt", true));
						userListWriter.append(info[0] + "\n");
						userListWriter.close();
						
						File userFolder = new File("users/" + info[0]);
						if (!userFolder.exists())
							userFolder.mkdir();
						
						BufferedWriter userInfoWriter = new BufferedWriter(new FileWriter(
								"users/" + info[0] + "/info.txt", false));
						userInfoWriter.append(chatObj.toString() + "\n");
						userInfoWriter.close();
						
					}
					else if(line.startsWith("THANKS")) {
						break;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	private static class Server implements Runnable {

		// 이 유저의 아이디
		private String myID;
		// 이름
		private String myName;
		// 클라이언트와 연결된 소켓
		private Socket socket;
		// 클라이언트와 연결된 스트림
		private BufferedReader in;
		private BufferedWriter out;

		// 파일에서 읽어온 유저의 정보
		private JsonObject myData;
		// 파일에서 읽어온 친구 정보
		private JsonArray myFriendJsonArray;
		// 파일에서 읽어온 채팅방 리스트
		private TreeSet<String> myChatRoomList;
		
		private Gson JSON = new Gson();

		/* 생성자 */
		public Server(Socket client) {
			this.socket = client;
		}

		// 멀티 스레드 생성시 자동으로 실행되는 함수
		@Override
		public void run() {

			try {
				// 스트림 생성
				in = new BufferedReader(new InputStreamReader((socket.getInputStream())));
				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

				/* 로그인 넣을 자리인데 임시로 대충 때움 */

				// 클라이언트가 접속하면 바로 ID 요청
				while (true) {
					out.write("REQ-ID\n");
					out.flush();
					System.out.println("Requeset ID: 아이디 요청");

					myID = in.readLine(); // 클라로부터 받기

					/* 원래는 로그인 넣을 자리인데 일단 대충 때움 */
					if (myID == null) {
						System.out.println("ID: null");
						return;
					} else if (myID.length() > 0 && Users.contains(myID)) {
						online.put(myID, socket); // 접속중인 사람에 추가
						
						// 내 정보 불러오기
						JsonReader myJsonReader = new JsonReader(new FileReader("users/" + myID + "/info.txt"));
						myData = JSON.fromJson(myJsonReader, JsonObject.class);
						myJsonReader.close();
						
						// 내 접속 시간 온라인으로 변경
						myData.addProperty("last", "online");
						try {
							PrintWriter toMyInfo = new PrintWriter(
									new FileOutputStream("users/" + myID + "/info.txt", false));
							toMyInfo.println(myData);
							toMyInfo.close();
						} catch(Exception e) {}
						
						out.write("RES-AcceptID " + myData + "\n");
						out.flush();
						System.out.println("아이디 확인: " + myID);

						// 프로필 사진이 존재하면 전송
						try {
							sendImageIcon("RES-YesProfile\n", "users/" + myID + "/profile.png");
							System.out.println("\n프로필 사진 전송");
						} catch (Exception e) { // 존재하지 않으면 전송 X
							out.write("RES-NoProfile\n");
							out.flush();
							System.out.println("프로필 사진 존재 안함");
						}

						break;
					}
				}

				
				// 내 이름
				JsonReader myJsonReader;
				myName = myData.get("name").getAsString();
				// 친구 정보
				myFriendJsonArray = myData.get("friend").getAsJsonArray();
				// 채팅 정보
				myChatRoomList = JSON.fromJson(JSON.toJson(myData.get("chat").getAsJsonArray()), TreeSet.class);

				
				// 여러가지 요청들 처리
				while (true) {
					System.out.println();
					String line = in.readLine(); // 클라이언트로부터 받은 요청
					System.out.println("from Client: " + line);

					// 자신의 친구 리스트를 내놓으라는 요청
					if (line.startsWith("REQ-FriendList")) {
						// 친구 정보를 JSON으로 바꾸기
						String json = JSON.toJson(myFriendJsonArray);

						// 내 친구 리스트를 JSON 형태로 클라이언트에게 보내기
						out.write("RES-FriendList " + json + "\n");
						out.flush();
						System.out.println("친구 목록 전송: " + json);
					}
					
					// 친구 상세 정보 요청 (최종 접속 시간, 상태메세지 등등)
					else if (line.startsWith("REQ-FriendInfo")) {
						String friendID = line.substring(15);
						Scanner reader = new Scanner(new File("users/" + friendID + "/info.txt"));
						JsonObject friendInfoObj = JSON.fromJson(reader.nextLine(), JsonObject.class);
						friendInfoObj.remove("password");
						reader.close();
						out.write("RES-FriendInfo " + friendInfoObj + "\n");
						out.flush();
						System.out.println("친구 상세 정보 요청");
					}
					
					// 친구의 프로필 사진 달라는 요청
					else if (line.startsWith("REQ-FriendProfile")) {
						String friendID = line.substring(18);
						// 프로필 사진이 존재하면 전송
						try {
							sendImageIcon("RES-YesFriendProfile\n", "users/" + friendID + "/profile.png");
							System.out.println("프로필 사진 전송: " + friendID);
						} catch (Exception e) { // 존재하지 않으면 전송 X
							out.write("RES-NoFriendProfile\n");
							out.flush();
							System.out.println("프로필 사진 존재 안함: " + friendID);
						}
					}
					

					// 자신이 속한 채팅방 리스트 달라는 요청
					else if (line.startsWith("REQ-ChatRoomList")) {
						// 내가 참가한 채팅방 리스트 JSON으로 변환
						String json = JSON.toJson(myChatRoomList);

						out.write("RES-ChatRoomList " + json + "\n");
						out.flush();
						System.out.println("채팅방 목록 전송: " + json);
					}

					// 채팅방 정보(이름, chatCode(채팅방들을 구분하기 위해 임의로 붙인 코드네임), 참가자 리스트 등) 달라는 뜻
					else if (line.startsWith("REQ-ChatRoomInfo")) {
						String chatCode = line.substring(17);
						// 파일에서 채팅방 정보 읽어오기
						Scanner chatInfoReader = new Scanner(new File("chatRooms/" + chatCode + "/info.txt"));
						String json = chatInfoReader.nextLine();

						// 정보를 클라이언트에게 전송
						out.write("RES-ChatRoomInfo " + json + "\n");
						out.flush();
						System.out.println("채팅방 정보 전송: " + json);
					}

					// 채팅 내역 초기화 요청
					else if (line.startsWith("REQ-ChatHistory")) {
						String chatCode = line.substring(16);

						// 로그에 있는거 읽어서 몽땅 클라이언트로 보내기
						Scanner logReader = new Scanner(new File("chatRooms/" + chatCode + "/log.txt"));
						while (logReader.hasNextLine()) {
							
							String chatting = logReader.nextLine().trim();
							if (chatting.equals("")) // 암것도 안써있으면 패스
								break;
							out.write("RES-Chat " + chatting + "\n");
							out.flush();
							
							JsonObject chatInfo = JSON.fromJson(chatting, JsonObject.class);
							String type = chatInfo.get("type").getAsString();
							
							if (type.equals("image")) {
								String fileName = chatInfo.get("content").getAsString();
								
								BufferedImage toSend = ImageIO.read(new File("chatRooms/" + chatCode + "/Files/" + fileName));
								
								double width = toSend.getWidth();
								double height = toSend.getHeight();
								while (width > 200) {
									width *= 0.95;
									height *= 0.95;
								}
								width = Math.round(width);
								height= Math.round(height);
								
								toSend = ImageConverter.scale(toSend, (int) width, (int) height);
								
								ByteArrayOutputStream bos = new ByteArrayOutputStream();
								ImageIO.write(toSend, "png", bos);
								byte[] byteArray = bos.toByteArray();

								BufferedOutputStream fileOutput = new BufferedOutputStream(socket.getOutputStream());

								fileOutput.write(byteArray, 0, byteArray.length);
								fileOutput.flush();
							}
						}
						logReader.close();

						
						out.write("RES-END\n"); // 내역 보내기 끝남
						out.flush();
						System.out.println("채팅 내역 전송 완료");
					}

					// 유저 정보 탐색
					else if (line.startsWith("REQ-UserInfo")) {
						String searchedID = line.substring(13); // 찾고자 하는 유저의 id
						System.out.print("유저 검색: " + searchedID + " ");

						// 유저 목록에 그 아이디가 있으면 TODO
						if (Users.contains(searchedID)) {
							// 파일에서 정보 읽어오기
							myJsonReader = new JsonReader(new FileReader("users/" + searchedID + "/info.txt"));
							JsonObject userData = JSON.fromJson(myJsonReader, JsonObject.class);

							// 클라이언트로 정보 보내기
							out.write("RES-UserInfo " + userData + "\n");
							out.flush();
							System.out.println("찾음");
							
							File profile = new File("users/" + searchedID + "/profile.png");
							if (profile.exists()) {
								out.write("RES-YesFriendProfile\n");
								out.flush();
								new Thread(new FileSender(socket, profile.getPath(), FileSender.PROFILE)).start();
								System.out.println("프로필 사진 전송: " + searchedID);
							}
							else {
								out.write("RES-NoFriendProfile\n");
								out.flush();
								System.out.println("프로필 사진 존재 안함: " + searchedID);
							}
							
							// 프로필 사진이 존재하면 전송
							try {
								
							} catch (Exception e) { // 존재하지 않으면 전송 X
								
							}
							
						}
						// 없으면 없다고 알리기
						else {
							out.write("RES-UserInfo NotFound\n");
							out.flush();
							System.out.println("못 찾음");
						}
					}

					// 실제로 친구 추가, 친구목록 업데이트
					else if (line.startsWith("REQ-NewFriend")) {
						String userID = line.substring(14);

						// 친구로 추가할 유저 정보 읽어오고
						myJsonReader = new JsonReader(new FileReader("users/" + userID + "/info.txt"));
						JsonObject infoObj = JSON.fromJson(myJsonReader, JsonObject.class);
						infoObj.remove("password");
						String userName = infoObj.get("name").getAsString();
						myJsonReader.close();

						// JsonObject로 만든 다음에
						JsonObject newFriend = new JsonObject();
						newFriend.addProperty("id", userID);
						newFriend.addProperty("name", userName);

						// 내 친구 목록에 그 유저 추가
						if (!myFriendJsonArray.contains(newFriend)) {
							myFriendJsonArray.add(newFriend);
							myData.add("friend", myFriendJsonArray);
							String myJson = JSON.toJson(myData);

							// 내 친구 목록 업데이트
							PrintWriter toMyInfo = new PrintWriter(
									new FileOutputStream("users/" + myID + "/info.txt", false));
							toMyInfo.println(myJson);
							toMyInfo.close();

							// 클라이언트에게 성공했다고 알리기
							out.write("RES-NewFriend " + infoObj + "\n");
							out.flush();
							System.out.println("친구 추가: " + userName);
						}
					}
					

					// 채팅방 새로 만들기
					else if (line.startsWith("REQ-NewChatRoom")) {
						String json = line.substring(16);

						// 채팅방 이름, 참가자 정보
						JsonObject chatRoomInfo = JSON.fromJson(json, JsonObject.class);

						// 이미 같은 참가자들로 이루어진 채팅방이 있으면 안됨
						JsonArray ja = chatRoomInfo.get("participant").getAsJsonArray();
						TreeSet<String> participantSet = JSON.fromJson(ja, TreeSet.class);

						// chatCode는 채팅방 텍스트 파일을 구분하기 위한 것임
						// chatCode는 참가자 리스트를 기준으로 만들어짐
						StringBuffer buffer = new StringBuffer("");
						for (String s : participantSet) {
							buffer.append(s);
							if (s != participantSet.last())
								buffer.append("=");
						}

						// JsonObject에 chatCode 추가
						String chatCode = buffer.toString();
						chatRoomInfo.addProperty("chatCode", chatCode); // JsonObject에 chatCode 정보 추가

						for (String userID : participantSet) {
							
							Scanner reader = new Scanner(new File("users/" + userID + "/info.txt"));
							String info = reader.nextLine();
							JsonObject infoObj = JSON.fromJson(info, JsonObject.class);
							reader.close();

							// 다른 채팅 참가자도 채팅방 내역 업데이트
							JsonArray chatList = infoObj.get("chat").getAsJsonArray();
							HashSet<String> temp = JSON.fromJson(chatList, HashSet.class);
							temp.add(chatCode);
							chatList = JSON.fromJson(JSON.toJson(temp), JsonArray.class);
							infoObj.add("chat", chatList);
							if (userID.equals(myID))
								myData.add("chat", chatList);

							// 수정된 정보 파일에 넣기
							PrintWriter updator = new PrintWriter(
									new FileOutputStream("users/" + userID + "/info.txt"));
							updator.println(infoObj);
							updator.close();
							
							// 클라이언트에 생성 완료되었다고 미리 보내기
							if (online.containsKey(userID)) {
								BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(online.get(userID).getOutputStream()));
								writer.write("RES-NewChatRoom " + chatRoomInfo + "\n");
								writer.flush();
							}
						}
						
						// 내가 속한 채팅방 목록 리스트에 추가
						myChatRoomList.add(chatCode);
						
						// 만약 같은 chat code를 가진 채팅방이 있다면 중지
						if (chatRooms.contains(chatCode)) {
							Scanner reader = new Scanner(new File("chatRooms/" + chatCode + "/info.txt"));
							String info = reader.nextLine();
							JsonObject infoObj = JSON.fromJson(info, JsonObject.class);
							reader.close();
							infoObj.add("participant", ja);
							
							PrintWriter writer = new PrintWriter(new FileOutputStream("chatRooms/" + chatCode + "/info.txt"));
							writer.println(infoObj);
							writer.close();
							
							out.write("RES-ExistChatRoom " + chatRoomInfo + "\n");
							out.flush();
							System.out.println("채팅방 이미 존재");
							continue;
						}
						System.out.println("새로운 채팅방 생성: " + chatRoomInfo);
						

						// 모든 채팅방 리스트에 추가
						chatRooms.add(chatCode);

						// chatCode랑 이름이 같은 새로운 폴더
						File folder = new File("chatRooms/" + chatCode);
						if (!folder.exists())
							folder.mkdir(); // 폴더 만드는 부분
						
						File FilesFolder = new File("chatRooms/" + chatCode + "/Files");	// 전송된 파일들 보관할 폴더
						if(!FilesFolder.exists())
							FilesFolder.mkdir();
							
						// 새 information 파일 작성
						PrintWriter chatRoomCreator = new PrintWriter(
								new FileOutputStream("chatRooms/" + chatCode + "/info.txt"));
						chatRoomCreator.println(chatRoomInfo); // 파일에 채팅방 정보 작성
						chatRoomCreator.close();

						// 새로그 파일 작성
						chatRoomCreator = new PrintWriter(
								new FileOutputStream("chatRooms/" + chatCode + "/log.txt", true));
						chatRoomCreator.print(" ".trim());
						chatRoomCreator.close();

						// 모든 chat code들이 들어있는 메타 데이터에 chat code를 추가
						PrintWriter chatListUpdator = new PrintWriter(
								new FileOutputStream("chatRooms/@chatCodeList.txt", true));
						chatListUpdator.println(chatCode);
						chatListUpdator.close();

					}

					// 채팅 기능
					else if (line.startsWith("REQ-Chat")) {
						String json = line.substring(9);

						// json 해석
						JsonObject requestObj = JSON.fromJson(json, JsonObject.class);
						String chatCode = requestObj.get("chatCode").getAsString();
						String chatContent = requestObj.get("content").getAsString();
						String time = requestObj.get("time").getAsString();

						// 채팅방에 보낼 채팅 json으로 변환
						JsonObject chattingObj = new JsonObject();
						chattingObj.addProperty("chatCode", chatCode);
						chattingObj.addProperty("senderID", myID);
						chattingObj.addProperty("senderName", myName);
						chattingObj.addProperty("content", chatContent);
						chattingObj.addProperty("type", "chat");
						chattingObj.addProperty("time", time);
						String chatting = JSON.toJson(chattingObj);

						// chatCode 가지고 채팅 정보에서 채팅 참가자 리스트 읽어오기
						Scanner chatInfoReader = new Scanner(new File("chatRooms/" + chatCode + "/info.txt"));
						JsonObject chatInfoJson = JSON.fromJson(chatInfoReader.nextLine(), JsonObject.class);
						TreeSet<String> participant = JSON.fromJson(chatInfoJson.get("participant").getAsJsonArray(),
								TreeSet.class);
						chatInfoReader.close();

						// 채팅 참가자가 온라인이면 채팅 내용 보내기
						for (String user : participant) {
							if (online.containsKey(user)) {
								BufferedWriter userWriter = new BufferedWriter(new OutputStreamWriter(online.get(user).getOutputStream()));
								userWriter.write("RES-Chat " + chattingObj + "\n");
								userWriter.flush();
							}
						}

						// 채팅 로그 업데이트
						PrintWriter logUpdator = new PrintWriter(
								new FileOutputStream("chatRooms/" + chatCode + "/log.txt", true));
						logUpdator.println(chattingObj);
						logUpdator.close();

						System.out.println(myID + "가 채팅을 보냄: " + chatContent);
					}

					// 친구 삭제
					else if (line.startsWith("REQ-RemoveFriend")) {
						String friendID = line.substring(17);

						myJsonReader = new JsonReader(new FileReader("users/" + friendID + "/info.txt"));
						String friendName = (String) ((HashMap<?, ?>) JSON.fromJson(myJsonReader, HashMap.class)).get("name");
						myJsonReader.close();

						JsonObject friend = new JsonObject();
						friend.addProperty("id", friendID);
						friend.addProperty("name", friendName);

						// 유저 중에 그 아이디가 있다고, 친구중에는 없다면
						if (Users.contains(friendID) && myFriendJsonArray.contains(friend)) {
							out.write("RES-RemoveFriend " + friend + "\n");
							out.flush();
							System.out.println("친구 제거: " + friend);
							
							myFriendJsonArray.remove(friend);
							myData.add("friend", myFriendJsonArray);
							String myJson = JSON.toJson(myData);

							// 내 친구 목록 업데이트
							PrintWriter toMyInfo = new PrintWriter(
									new FileOutputStream("users/" + myID + "/info.txt", false));
							toMyInfo.println(myJson);
							toMyInfo.close();
							
						}
					}

					// 채팅방 나가기
					else if (line.startsWith("REQ-LeaveChatRoom")) {
						String json = line.substring(18);
						JsonObject chatRoom = JSON.fromJson(json, JsonObject.class);
						String chatCode = chatRoom.get("chatCode").getAsString();

						if (myChatRoomList.contains(chatCode)) {
							myChatRoomList.remove(chatCode);
							JsonArray list = JSON.fromJson(JSON.toJson(myChatRoomList), JsonArray.class);

							myData.add("chat", list);

							myJsonReader = new JsonReader(new FileReader("chatRooms/" + chatCode + "/info.txt"));
							JsonObject chatRoomInfo = JSON.fromJson(myJsonReader, JsonObject.class);

							TreeSet participant = JSON.fromJson(chatRoomInfo.get("participant"), TreeSet.class);
							participant.remove(myID);
							chatRoomInfo.add("participant", JSON.fromJson(JSON.toJson(participant), JsonArray.class));

							PrintWriter toChatRoomInfo = new PrintWriter(
									new FileOutputStream("chatRooms/" + chatCode + "/info.txt", false));
							toChatRoomInfo.println(chatRoomInfo);
							toChatRoomInfo.close();

							// 내 채팅 목록 업데이트
							PrintWriter toMyInfo = new PrintWriter(
									new FileOutputStream("users/" + myID + "/info.txt", false));
							toMyInfo.println(myData);
							toMyInfo.close();

							out.write("RES-LeaveChatRoom " + chatCode + "\n");
							out.flush();
							System.out.println("채팅 나가기: " + chatRoom);
						}
					}

					// 프로필 사진 업데이트 TODO
					else if (line.startsWith("REQ-UpdateProfile")) {
						
						File oldProfile = new File("users/" + myID + "/profile.png");
						Files.deleteIfExists(oldProfile.toPath());

						File newFile = null;
						if (line.substring(17).equals("Yes")) {

							InputStream inputStream = socket.getInputStream();

							byte[] sizeAr = new byte[4];
							inputStream.read(sizeAr);
							int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();

							byte[] imageAr = new byte[size];
							inputStream.read(imageAr);
							for (int i = 0; i < imageAr.length; i++) {
							}

							BufferedImage newProfile = ImageIO.read(new ByteArrayInputStream(imageAr));

							newFile = new File("users/" + myID + "/profile.png");
							if (newProfile != null)
								ImageIO.write(newProfile, "png", newFile);
						}

						// 프로필 사진 실시간 변경
						for (String user : online.keySet()) {
							myJsonReader = new JsonReader(new FileReader("users/" + user + "/info.txt"));
							JsonObject userInfo = JSON.fromJson(myJsonReader, JsonObject.class);
							myJsonReader.close();
							
							JsonArray friends = userInfo.get("friend").getAsJsonArray();
							
							for (int i = 0; i < friends.size(); i++) {
								JsonObject friend_i = friends.get(i).getAsJsonObject();
								if (friend_i.get("id").getAsString().equals(myID)) {
									BufferedWriter toFriend = new BufferedWriter(new OutputStreamWriter(online.get(user).getOutputStream()));
									
									if (newFile == null) {
										toFriend.write("RES-UpdateProfileNoo " + myID + "\n");
										toFriend.flush();
									}
									else {
										toFriend.write("RES-UpdateProfileYes " + myID + "\n");
										toFriend.flush();
										
										new Thread(new FileSender(online.get(user), newFile.getPath(), FileSender.PROFILE)).start();
									}
								}
							}
						}
						
						System.out.println("프로필 사진 업데이트 완료");
					}

					
					// 채팅으로 파일 전송
					else if(line.startsWith("REQ-FileChat")) {
						String json = line.substring(12);
						JsonObject fileInfo = JSON.fromJson(json, JsonObject.class);
						String fileName = fileInfo.get("content").getAsString();
						String chatCode = fileInfo.get("chatCode").getAsString();
						String time = fileInfo.get("time").getAsString();
						
						String extension = fileName.substring(fileName.indexOf('.') + 1);
						String[] arr = {"jpg", "png", "jpeg"};
						HashSet<String> imageExtensions = new HashSet<String>(Arrays.asList(arr));
						String type = null;
						if (imageExtensions.contains(extension))
							type = "image";
						else
							type = "file";
						
						JsonObject obj = new JsonObject();
						obj.addProperty("chatCode", chatCode);
						obj.addProperty("senderID", myID);
						obj.addProperty("senderName", myName);
						obj.addProperty("content", fileName);
						obj.addProperty("type", type);
						obj.addProperty("time", time);

						byte[] oneByte = new byte[8192];
						int bytesRead;
						
						InputStream fileInput = socket.getInputStream();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						File file = new File("chatRooms/" + chatCode + "/Files/" + fileName);
						BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file.getPath()));
						
						do {
							bytesRead = fileInput.read(oneByte);
							baos.write(oneByte, 0, bytesRead);
						} while (bytesRead == 8192);
														
						bos.write(baos.toByteArray());
						bos.flush();
						bos.close();
						
						System.out.println("파일 다운 완료: " + fileName);
						
						// 채팅 로그 업데이트
						PrintWriter logUpdator = new PrintWriter(
								new FileOutputStream("chatRooms/" + chatCode + "/log.txt", true));
						logUpdator.println(obj);
						logUpdator.close();
						
						// 채팅 참여자 읽어오기
						myJsonReader = new JsonReader(new FileReader("chatRooms/" + chatCode + "/info.txt"));
						JsonObject chatRoomInfo = JSON.fromJson(myJsonReader, JsonObject.class);
						TreeSet<String> participant = JSON.fromJson(chatRoomInfo.get("participant"), TreeSet.class);
						myJsonReader.close();
						
						// 채팅 참가자가 온라인이면 채팅 보내기
						for (String user : participant) {
							if (online.containsKey(user)) {
								BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(online.get(user).getOutputStream()));
								writer.write("RES-Chat " + obj + "\n");
								writer.flush();
								
								if (type.equals("image"))
									new Thread(new FileSender(online.get(user), file.getPath(), FileSender.SHOW)).start();
								
							}
						}

					}
					
					// 파일 다운로드 요청
					else if (line.startsWith("REQ-DownloadFile")) {
						String json = line.substring(16);
						JsonObject downInfo = JSON.fromJson(json, JsonObject.class);
						String chatCode = downInfo.get("chatCode").getAsString();
						String fileName = downInfo.get("fileName").getAsString();
						
						File file = new File("chatRooms/" + chatCode + "/Files/" + fileName);
						if (file == null)
							continue;
						
						out.write("RES-DownloadFile " + fileName + "\n");
						out.flush();
						
						byte[] byteArray = new byte[(int) file.length()];
						BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
						
						BufferedOutputStream fileOutput = new BufferedOutputStream(socket.getOutputStream());
						
						bis.read(byteArray, 0, byteArray.length);
						fileOutput.write(byteArray, 0, byteArray.length);
						fileOutput.flush();
						bis.close();
						
						System.out.println("파일 전송: " + fileName);
						
					}
					
					// 상태 메세지 변경
					else if (line.startsWith("REQ-UpdateState")) {
						String stateMessage = line.substring(16);
						myData.remove("state");
						myData.addProperty("state", stateMessage);
						
						PrintWriter toMyInfo = new PrintWriter(
								new FileOutputStream("users/" + myID + "/info.txt", false));
						toMyInfo.println(myData);
						toMyInfo.close();
						
						// 상태 메세지 실시간 변경
						for (String user : online.keySet()) {
							myJsonReader = new JsonReader(new FileReader("users/" + user + "/info.txt"));
							JsonObject userInfo = JSON.fromJson(myJsonReader, JsonObject.class);
							myJsonReader.close();
							
							JsonArray friends = userInfo.get("friend").getAsJsonArray();
							
							for (int i = 0; i < friends.size(); i++) {
								JsonObject friend_i = friends.get(i).getAsJsonObject();
								if (friend_i.get("id").getAsString().equals(myID)) {
									JsonObject updated = new JsonObject();
									updated.addProperty("id", myID);
									updated.addProperty("state", stateMessage);
									
									BufferedWriter toFriend = new BufferedWriter(new OutputStreamWriter(online.get(user).getOutputStream()));
									toFriend.write("RES-UpdateState " + updated + "\n");
									toFriend.flush();
								}
							}
						}
						System.out.println("상태 메세지 변경 완료: " + stateMessage);
					}
					
					
					
					
					
					// 채팅 나가기, 친구 삭제,
					// 나머지 기타 기능들 넣을 자리
					
					
					
					

				}

			} catch (IOException e) {
			} finally {
				// 나간다면 온라인 상태에서 제거
				if (myID != null) {
					System.out.println("\n" + myID + "가 접속을 종료하였습니다!");
					online.remove(myID);
					
					String logOutTime = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(Calendar.getInstance().getTime());
					myData.addProperty("last", logOutTime);
					
					try {
						socket.close();
						
						// 내 마지막 접속시간
						PrintWriter toMyInfo = new PrintWriter(
								new FileOutputStream("users/" + myID + "/info.txt", false));
						toMyInfo.println(myData);
						toMyInfo.close();
						System.out.println("마지막 접속 시간 업데이트: " + logOutTime);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			}
		}
		
		
		// 클라에 파일 전송
		private void sendImageIcon(String protocol, String path) throws IOException {
			OutputStream outputStream = socket.getOutputStream();
			BufferedImage image = ImageIO.read(new File(path));

			// 오류 안뜨면 사진 존재하는 것
			out.write(protocol);
			out.flush();

			ByteArrayOutputStream byteArrayOutputStream;
			int s;
			byte[] size;
			do {
				// 바이트 단위로 전송
				byteArrayOutputStream = new ByteArrayOutputStream();
				ImageIO.write(image, "jpg", byteArrayOutputStream);
				s = byteArrayOutputStream.size();
				size = ByteBuffer.allocate(4).putInt(s).array();
			} while(s < 0);

			outputStream.write(size);
			outputStream.write(byteArrayOutputStream.toByteArray());
			outputStream.flush();

			byteArrayOutputStream.close();
		}

	}

}
