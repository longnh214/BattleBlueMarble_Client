package Graphics;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Chatting.ClientChat;
import Game.GameClientSocket;

public class MasterPanel extends JFrame {

   private GameFrame gameFrame;
   private ClientChat clientChat;
   private WaitingUI waitingUI;
   private boolean isID = true;
   private GameClientSocket gameClientSocket;
   private String msg = "";
   private Container c;
   private JLabel[] playerLabel = new JLabel[2];
   private String [] playerName = new String[2];
   private boolean isTurn = false;

   public MasterPanel() throws Exception {
      while (isID) {
         playerName[0] = JOptionPane.showInputDialog("ID를 입력해주세요.");
         if (playerName[0] != null)
            break;
      }
      gameClientSocket = new GameClientSocket(this);
      gameFrame = new GameFrame(playerName[0], gameClientSocket);
      gameFrame.setVisible(false);
      
      waitingUI = new WaitingUI(playerName[0], gameClientSocket);
      waitingUI.getPlayerLabel()[0].setText(playerName[0]);
      playerLabel = waitingUI.getPlayerLabel();
      //clientChat = new ClientChat(playerName[0]);

      c = getContentPane();
      // server에게 정보 전달.
      //clientChat.setVisible(true);

      /*   setSize(1550, 803);
      setLayout(new BorderLayout());*/
      gameClientSocket.send_Message("CONN><" + playerName[0]);
   }

   public GameFrame getGameFrame() {
      return gameFrame;
   }

   public ClientChat getClientChat() {
      return clientChat;
   }

   public WaitingUI getWaitingUI() {
      return waitingUI;
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

   public void setGameFrame(GameFrame gameFrame) {
      this.gameFrame = gameFrame;
   }

   public void setClientChat(ClientChat clientChat) {
      this.clientChat = clientChat;
   }

   public void setWaitingUI(WaitingUI waitingUI) {
      this.waitingUI = waitingUI;
   }
   
   
}