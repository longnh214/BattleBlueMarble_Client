package Graphics;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import Chatting.ClientChat;
import Game.GameClientSocket;

public class WaitingUI extends JFrame{


   private JLabel roomMainLabel = new JLabel("Battle Blue Marble Room");
   private JLabel[] playerLabel = new JLabel[2];
   private String [] playerName = new String[2];
   private JButton readyButton = new JButton("Ready");
   private String msg = "";
   private boolean isReady = false;
   private GameClientSocket gameClientSocket;
   private ClientChat clientChat;
   private Container c;
   
   public WaitingUI(String name, GameClientSocket gameClientSocket) throws Exception {
	  c = this.getContentPane();
	  this.clientChat = new ClientChat(name);
      this.gameClientSocket = gameClientSocket;
      setLayout(null);
      setBackground(Color.darkGray);
      
      roomMainLabel.setBounds(120, 25, 300, 100);
      roomMainLabel.setFont(new Font("Serif", Font.BOLD, 15));
      roomMainLabel.setBackground(Color.GRAY);
      
      
      readyButton.setBounds(150,100,100,50);
      
      readyButton.addActionListener(new ActionListener() {
         
         @Override
         public void actionPerformed(ActionEvent arg0) {
            gameClientSocket.send_Message("READY><" + name);
            
            readyButton.setEnabled(false);
            readyButton.setText(" 곧 시작 ");
         }
      });
      playerLabel[0] = new JLabel();
      playerLabel[1] = new JLabel();
      playerLabel[0].setBounds(120,150,100,50);
      playerLabel[1].setBounds(120,200,100,50);
      playerLabel[0].setText("1번 플레이어");
      playerLabel[1].setText("2번 플레이어");
      
      clientChat.setBounds(400, 25, 288, 400);
      c.add(roomMainLabel);
      
      c.add(readyButton);
      c.add(playerLabel[0]);
      c.add(playerLabel[1]);
      c.add(clientChat);
      setSize(700,425);
      
      //양쪽이 레디가 되고 start가 되면 gameFrame.setVisible(true);
      //그럼 이 프레임은 setVisible(false);
      setResizable(false);
      setVisible(true);
   }


   public boolean isReady() {
      return isReady;
   }

   public String getReadyMsg() {
      return msg;
   }

   public JButton getReadyButton() {
      return readyButton;
   }

   public void setReadyButton(JButton readyButton) {
      this.readyButton = readyButton;
   }

   public JLabel getRoomMainLabel() {
      return roomMainLabel;
   }

   public void setRoomMainLabel(JLabel roomMainLabel) {
      this.roomMainLabel = roomMainLabel;
   }

   public JLabel[] getPlayerLabel() {
      return playerLabel;
   }

   public void setPlayerLabel(JLabel[] playerLabel) {
      this.playerLabel = playerLabel;
   }

   public String[] getPlayerName() {
      return playerName;
   }

   public void setPlayerName(String[] playerName) {
      this.playerName = playerName;
   }
   
   
}