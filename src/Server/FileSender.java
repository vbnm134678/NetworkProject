package Server;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import Client.ImageConverter;

public class FileSender implements Runnable {

	private Socket socket;
	private String path;
	private int category;

	public final static int SHOW = 0;
	public final static int DOWN = 1;
	public final static int PROFILE = 2;

	public FileSender(Socket receiver, String filePath, int category) {
		socket = receiver;
		path = filePath;
		this.category = category;
	}

	@Override
	public void run() {
		if (category == DOWN) {
			try {
				File file = new File(path);
				byte[] byteArray = new byte[(int) file.length()];
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

				BufferedOutputStream fileOutput = new BufferedOutputStream(socket.getOutputStream());

				bis.read(byteArray, 0, byteArray.length);
				int sleepTime = byteArray.length / 100;
				Thread.sleep(sleepTime);
				fileOutput.write(byteArray, 0, byteArray.length);
				fileOutput.flush();
				bis.close();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		else if (category == SHOW) {
			try {
				BufferedImage toSend = ImageIO.read(new File(path));
				
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
			} catch (Exception e) {
			}
		}

		else if (category == PROFILE) {
			try {
				BufferedImage toSend = ImageIO.read(new File(path));
				toSend = ImageConverter.scale(toSend, 200, 200);

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ImageIO.write(toSend, "png", bos);
				byte[] byteArray = bos.toByteArray();

				BufferedOutputStream fileOutput = new BufferedOutputStream(socket.getOutputStream());

				fileOutput.write(byteArray, 0, byteArray.length);
				fileOutput.flush();
			} catch (Exception e) {
			}
		}
	}

}
