package Client;


import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

public class ImageConverter {

	private static Dimension arcs = new Dimension(65, 65);

	public static ImageIcon rounding(ImageIcon original, Dimension size) {

		int w = size.width;
		int h = size.height;
		Image t = original.getImage().getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
		ImageIcon sized = new ImageIcon(t);
		BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2 = output.createGraphics();

		// This is what we want, but it only does hard-clipping, i.e. aliasing
		// g2.setClip(new RoundRectangle2D ...)

		// so instead fake soft-clipping by first drawing the desired clip shape
		// in fully opaque white with antialiasing enabled...
		g2.setComposite(AlphaComposite.Src);
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.WHITE);
		g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arcs.width, arcs.height));

		// ... then compositing the image on top,
		// using the white shape from above as alpha source
		g2.setComposite(AlphaComposite.SrcAtop);
		g2.drawImage(sized.getImage(), 0, 0, null);

		g2.dispose();

		return new ImageIcon(output);
	}
	
	// 이미지 사이즈 크기 조절
	public static BufferedImage scale(BufferedImage img, int newW, int newH) {
		 Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		    BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

		    Graphics2D g2d = dimg.createGraphics();
		    g2d.drawImage(tmp, 0, 0, null);
		    g2d.dispose();

		    return dimg;
	}

}
