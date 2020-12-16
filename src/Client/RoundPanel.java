package Client;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RoundPanel extends JPanel {
	/* Sets if it has an High Quality view */
	protected boolean highQuality = true;
	/* Double values for Horizontal and Vertical radius of corner arcs */
	protected Dimension arcs = new Dimension(65, 65);

	private Color color;
	private JLabel profile = new JLabel();
	private ImageIcon icon;
	private String name;
	
	private JPanel wrapper;
	private JPanel wrapper2;
	
	// 말풍선 만드는데 사용
	public RoundPanel(Dimension rounding, Color backColor) {
		super();
		setOpaque(false);
		this.arcs = rounding;
		this.color = backColor;
		setBackground(color);
	}
	
	// 프로필 만드는데 사용
	public RoundPanel(ImageIcon icon, String name, Color backColor, Dimension iconSize, int textSize) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
		this.color = backColor;
		this.name = name;
		
		setImage(icon, name, iconSize, textSize);
	}
	
	public void setImage(ImageIcon icon, String name, Dimension iconSize, int textSize) {
		profile.setVisible(false);
		profile.setIcon(null);
		if (icon == null) {
			profile.setText(name.substring(0, 1));
			profile.setForeground(color);
			profile.setAlignmentX(CENTER_ALIGNMENT);
			profile.setAlignmentY(CENTER_ALIGNMENT);
			profile.setFont(new Font("맑은 고딕", Font.PLAIN, textSize));
			profile.setBorder(new EmptyBorder(11, 2, 0, 0));
			if (name.substring(0, 1).equals("?"))
				profile.setBorder(new EmptyBorder(10, 3, 0, 0));
		}
		else {
			this.icon = ImageConverter.rounding(icon, iconSize);
			this.setPreferredSize(iconSize);
			profile.setBorder(new EmptyBorder(0, 0, 0, 10));
			profile.setText("");
			profile.setIcon(this.icon);
		}
		profile.setVisible(true);
		this.add(profile);
	}
	

	// 프로필 이미지 크기 설정
	public JPanel wrapRoundPanel(Dimension size) {
		JPanel wrapper = new JPanel();
		wrapper.setBackground(color);
		JPanel wrapper2 = new JPanel();
		wrapper2.setBackground(color);
		wrapper2.setPreferredSize(size);
		
		wrapper2.add(this);
		wrapper.add(wrapper2);
		
		return wrapper;
	}
	
	// 래퍼 색깔 조절용
	public JPanel[] wrapRoundPanelArr(Dimension size) {
		JPanel[] js = new JPanel[2];
		
		JPanel wrapper = new JPanel();
		wrapper.setBackground(color);
		js[0] = wrapper;
		
		JPanel wrapper2 = new JPanel();
		wrapper2.setBackground(color);
		wrapper2.setPreferredSize(size);
		js[1] = wrapper2;
		
		wrapper2.add(this);
		wrapper.add(wrapper2);
		
		return js;
	}

	// 패널 모서리가 둥글게 만들기
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int width = getWidth();
		int height = getHeight();
		Graphics2D graphics = (Graphics2D) g;

		// Sets antialiasing if HQ.
		if (highQuality) {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}

		// Draws the rounded opaque panel with borders.
		graphics.setColor(Color.WHITE);
		graphics.fillRoundRect(0, 0, width, height, arcs.width, arcs.height);
		graphics.setColor(color);

		// Sets strokes to default, is better.
		graphics.setStroke(new BasicStroke());
	}
	
	public static JButton RoundButton(Dimension arcs, Color pressed, Color on, Color defaultC) {
		JButton button = new JButton() {
			@Override
	        protected void paintComponent(Graphics g) {
				
				int width = getWidth();
				int height = getHeight();
				Graphics2D graphics = (Graphics2D) g;

				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
	            if (getModel().isPressed()) {
	            	graphics.setColor(pressed);	// new Color(255, 165, 0, 200)
	            } else if (getModel().isRollover()) {
	            	graphics.setColor(on);	// new Color(255, 180, 30)
	            } else {
	            	graphics.setColor(defaultC);	// Color.ORANGE
	            }
	            graphics.fillRoundRect(0, 0, width, height, arcs.width, arcs.height);
	            graphics.setStroke(new BasicStroke());
	            super.paintComponent(g);
	        }	
		};
		
		return button;
	}
	
	
}
