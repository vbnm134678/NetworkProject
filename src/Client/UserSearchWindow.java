package Client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class UserSearchWindow extends JFrame {
	
	private String myID;
	private BufferedWriter out;
	private Collections data;								// 여러 정보 모아놓은 것
	
	private JPanel searchedPanel = new JPanel();			// 유저 아이디, 이름을 한데 묵기 위한 패널
	private RoundPanel profileImage;				// 프로필 이미지
	private JTextField searchTextField = new JTextField();	// 검색 단어 입력창
	private JButton searchButton = 
			RoundPanel.RoundButton(new Dimension(60, 60), new Color(255, 155, 0, 200), 
					new Color(255, 165, 0, 200), new Color(255, 180, 30));			// 검색 버튼
	private JPanel searchPanel = new JPanel();				// 검색창이랑 검색 버튼 모아놓는 패널
	private JPanel profileWrapper;
	private JLabel searchedName = new JLabel("", SwingConstants.CENTER);
	private JLabel searchedID = new JLabel("", SwingConstants.CENTER);	// 찾아낸 유저 정보(ID, 이름)
	private JLabel searchedState = new JLabel("", SwingConstants.CENTER);	// 상태 메세지
	private JButton friendConfirmButton = 
			RoundPanel.RoundButton(new Dimension(25, 25), new Color(255, 180, 30), 
					new Color(255, 165, 0, 200), Color.ORANGE);	// 친구 추가 확인 버튼	
	private JPanel friendConfirmButtonPanel = new JPanel();	// 버튼 크기 고정용 패널
	

	public UserSearchWindow(String id, BufferedWriter writer, Collections friendInfo) {
		super();
		myID = id;
		out = writer;
		data = friendInfo;
		
		this.setTitle("Chatter - 친구 검색");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible(true);
		this.setMinimumSize(new Dimension(420, 350));
		JPanel bodyPanel = (JPanel) this.getContentPane();
		bodyPanel.setBackground(Color.WHITE);

		ImageIcon icon = new ImageIcon("resources/MainIcon.png");
		Image image = icon.getImage().getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH);
		this.setIconImage(image);

		searchTextField.setFont(new Font("맑은 고딕", Font.PLAIN, 20)); // 검색창 크기 설정
		searchTextField.setText("");
		searchTextField.setBorder(new EmptyBorder(0, 0, 0, 0));
		searchTextField.setPreferredSize(new Dimension(220, 30));
		// 검색 기능 추가
		searchTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String ID = searchTextField.getText().trim();
				// 현재 ID 라벨 내용
				if (!ID.equals("")) {
					if (out != null) {
						try { 
							out.write("REQ-UserInfo " + ID + "\n");	// 서버에 서치 내영 날리기
							out.flush();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					} else
						setSearchedPanel("서버에 연결되지 않았습니다.", "넹", null, null, false);
				}
			}
		});

		// 디자인
		icon = new ImageIcon("resources/SearchIcon.png");
		Image sizedImg = icon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
		searchButton.setIcon(new ImageIcon(sizedImg));
		searchButton.setBorderPainted(false);
		searchButton.setFocusPainted(false);
		searchButton.setContentAreaFilled(false);
		searchButton.setPreferredSize(new Dimension(45, 45));
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String ID = searchTextField.getText().trim();
				// 현재 ID 라벨 내용
				if (!ID.equals("")) {
					if (out != null) {
						try { 
							out.write("REQ-UserInfo " + ID + "\n");	// 서버에 서치 내영 날리기
							out.flush();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					} else
						setSearchedPanel("서버에 연결되지 않았습니다.", "넹", null, null, false);
				}
			}
		});
		
		searchPanel.setBackground(Color.ORANGE);
		searchPanel.add(searchTextField);
		searchPanel.add(searchButton);

		// 프로필 이미지
		profileImage = new RoundPanel(null, "?", Color.ORANGE, new Dimension(80, 80), 40);
		profileImage.setPreferredSize(new Dimension(80, 80));
		profileImage.setBackground(Color.ORANGE);
		profileImage.setVisible(false); // TODO
		profileWrapper = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				
				Dimension arcs = new Dimension(70, 70);
				int width = getWidth() - 4;
				int height = getHeight() - 4;
				Graphics2D graphics = (Graphics2D) g;
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				graphics.setColor(Color.ORANGE);
				graphics.fillRoundRect(2, 2, width, height, arcs.width, arcs.height);
				graphics.setColor(Color.ORANGE);

				graphics.setStroke(new BasicStroke());
			}
		};
		profileWrapper.setBackground(Color.WHITE);
		profileWrapper.setVisible(false);
		profileWrapper.add(profileImage);
		
		
		// 상태 메세지
		searchedState.setText("\n");
		searchedState.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		searchedState.setForeground(new Color(206, 109, 60));
		
		searchedName.setText("검색창에 ID를 입력해주세요");
		searchedName.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
		searchedName.setBorder(new EmptyBorder(5, 0, 0, 0));

		searchedID.setText("(넹)");
		searchedID.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
		searchedID.setBorder(new EmptyBorder(0, 0, 5, 0));

		friendConfirmButton.setPreferredSize(new Dimension(100, 50));
		friendConfirmButton.setText("친구 추가");
		friendConfirmButton.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		friendConfirmButton.setBorderPainted(false);
		friendConfirmButton.setFocusPainted(false);
		friendConfirmButton.setContentAreaFilled(false);
		friendConfirmButton.setPreferredSize(new Dimension(100, 40));
		friendConfirmButton.setVisible(false); // 기본적으로 안보임

		// 이미 친구가 아니라면 친구 추가 확인
		friendConfirmButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				String friendID = searchedID.getText().substring(1, searchedID.getText().length() - 1);
				if (out != null && !data.friends.containsKey(friendID)) {
					try {
						out.write("REQ-NewFriend " + friendID + "\n");
						out.flush();
						// 친구 추가 됐으면 버튼 비활성화
						friendConfirmButton.setVisible(false);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		friendConfirmButtonPanel.setPreferredSize(new Dimension(400, 60));
		friendConfirmButtonPanel.add(friendConfirmButton, BorderLayout.CENTER);
		friendConfirmButtonPanel.setBackground(Color.WHITE);
		
		
		searchedPanel.setLayout(new GridBagLayout());
		GridBagConstraints con = new GridBagConstraints();
		searchedPanel.setBackground(Color.WHITE);
		con.gridx = 0;
		con.gridy = 0;
		con.gridheight = 2;
		con.anchor = GridBagConstraints.CENTER;
		searchedPanel.add(profileWrapper, con);
		con.gridx = 0;
		con.gridy = 2;
		con.gridheight = 1;
		searchedPanel.add(searchedState, con);
		con.gridx = 0;
		con.gridy = 3;
		con.gridheight = 1;
		searchedPanel.add(searchedName, con);
		con.gridx = 0;
		con.gridy = 4;
		searchedPanel.add(searchedID, con);
		con.gridx = 0;
		con.gridy = 5;
		con.gridheight = 2;
		searchedPanel.add(friendConfirmButtonPanel, con);
		
		// 메인 패널에 검색 패널 추가
		bodyPanel.add(searchPanel, BorderLayout.NORTH);
		bodyPanel.add(searchedPanel, BorderLayout.CENTER);
	}
	
	
	/* searchedPanel에 있는 라벨 내용 업데이트 해주는 함수 */
	public void setSearchedPanel(String id, String name, String state, ImageIcon profile, boolean isExistUser) {
		// 라벨을 내용 업데이트
		searchedName.setText(name);
		searchedID.setText("(" + id + ")");
		profileImage.setVisible(true);
		
		if (isExistUser) {
			profileWrapper.setVisible(false);
			profileImage.setVisible(false);
			searchedState.setText(state);
			if (state.equals(""))
				searchedState.setText("\n");
			profileImage.setImage(profile, name, new Dimension(80, 80), 40);
			profileImage.setVisible(true);
			profileWrapper.setVisible(true);
		}
		else {
			searchedState.setText("\n");
			profileImage.setVisible(false);
			profileWrapper.setVisible(false);
		}

		boolean isFriend = data.friends.containsKey(searchedID.getText().substring(1, searchedID.getText().length() - 1));

		// 만약 그 유저가 존재하고 나 또는 내 친구가 아니라면
		if (isExistUser && !isFriend && !id.equals(myID))
			friendConfirmButton.setVisible(true); // 확인 버튼 활성화
		else
			friendConfirmButton.setVisible(false); // 확인 버튼 비활서화
	}
	
}
