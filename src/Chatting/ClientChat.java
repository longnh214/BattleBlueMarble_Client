package Chatting;

// MainView.java : Java Chatting Client �� �ٽɺκ�
// read keyboard --> write to network (Thread �� ó��)
// read network --> write to textArea

import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

public class ClientChat extends JPanel {
	private JPanel contentPane;
	private JTextField textField;
	private String id;
	private String ip = "127.0.0.1";
	private int port = 30000;
	private Canvas canvas;
	JButton sendBtn;
	JTextPane textArea;
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	private String[] getMsg = new String[50];
	private String getId = "";
	private String myMsg = "";

	public ClientChat(String id) {
		this.id = id;
		/*
		 * this.ip = ip; this.port = port;
		 */
		init();
		start();
		append_Message("Connected..." + "\n");
		network();
	}

	public void network() {

		try {
			socket = new Socket(ip, port);
			if (socket != null) {
				Connection();
			}
		} catch (UnknownHostException e) {

		} catch (IOException e) {
			append_Message("접속했습니다!!\n");
		}
	}

	public void Connection() {
		try {
			is = socket.getInputStream();
			dis = new DataInputStream(is);
			os = socket.getOutputStream();
			dos = new DataOutputStream(os);
		} catch (IOException e) {
			append_Message("접속 실패!!!\n");
		}
		send_Message(id);
		Thread th = new Thread(new Runnable() {
			@SuppressWarnings("null")
			@Override
			public void run() {
				while (true) {
					try {
						String msg = dis.readUTF(); // 무한대기
						msg = msg.trim();

						StringTokenizer stringTokenizer = new StringTokenizer(msg, "|");
						int index = 0;
						while (stringTokenizer.hasMoreTokens()) {
							getMsg[index] = stringTokenizer.nextToken("|");
							index++;
						}
						String editMsg[] = new String[index];
						for (int i = 0; i < index; i++)
							editMsg[i] = getMsg[i];

						editMsg[0] = editMsg[0].replace("[", "");
						editMsg[0] = editMsg[0].replace("]", "");

						getId = editMsg[0];

						for (int i = 1; i < editMsg.length; i++)
							myMsg += editMsg[i] + " ";

						if (id.equals(getId)) {
							myMsg = myMsg.trim();
							append_Message("\t\t\t\t" + myMsg + "\n");
						} else {
							msg = msg.trim();
							append_Message(msg + "\n");
						}
					} catch (IOException e) {
						append_Message("수신 에러!!\n");
						try {
							os.close();
							is.close();
							dos.close();
							dis.close();
							socket.close();
							break;
						} catch (IOException e1) {

						}

					}
				} // while 끝
			}// run 끝
		});
		th.start();
	}

	public void send_Message(String str) {
		try {
			System.out.println(id + " " + str);
			dos.writeUTF(str);
		} catch (IOException e) {
			append_Message("메시지 송신 오류!!\n");
		}
	}

	public void init() {
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 288, 392);
		
		setBorder(new EmptyBorder(5, 5, 5, 5));
		//setContentPane(contentPane);
		setLayout(null);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 272, 302);
		add(scrollPane);
		textArea = new JTextPane();
		scrollPane.setViewportView(textArea);
		textArea.setDisabledTextColor(new Color(0, 0, 0));
		textField = new JTextField();
		textField.setBounds(0, 312, 155, 42);
		add(textField);
		textField.setColumns(10);
		sendBtn = new JButton("  전    송  ");
		sendBtn.setBounds(161, 312, 111, 42);
		add(sendBtn);
		textArea.setEnabled(false);
		setVisible(true);
	}

	public void start() {
		Myaction action = new Myaction();
		sendBtn.addActionListener(action);
		textField.addActionListener(action);
	}

	class Myaction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == sendBtn || e.getSource() == textField) {
				String msg = null;
				msg = String.format("[%s] %s\n", id, textField.getText());
				send_Message(msg);
				textField.setText("");
				textField.requestFocus();
			}
		}
	}

	public void append_Message(String str) {
		int len = textArea.getDocument().getLength(); // same value as
		textArea.setCaretPosition(len); // place caret at the end (with no selection)
		textArea.replaceSelection(str); // there is no selection, so inserts at caret
	}

}