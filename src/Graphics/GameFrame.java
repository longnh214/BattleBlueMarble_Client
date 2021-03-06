package Graphics;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import Game.DiceThread;
import Game.GameClientSocket;
import Map.BonusSpot;
import Map.ChanceCard;
import Map.ExpoSpot;
import Map.Ground;
import Map.Land;
import Map.LuckySpot;
import Map.PrisonSpot;
import Map.SpecialSpot;
import Map.StartSpot;
import Map.TrailSpot;
import Method.GetChanceCard;
import Method.InBonusSpot;
import Method.InExpoSpot;
import Method.InLuckySpot;
import Method.InPrisonSpot;
import Method.InStartSpot;
import Method.InTrailSpot;
import Method.Paying;
import Method.PurchaseDialog;
import Method.Update;

public class GameFrame extends JFrame {
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private BGPanel bgPanel;
   private Dice dice;
   private Ground ground = Ground.getSingleGround();
   private int rand1, rand2, randSum; // Dice 변수
   private int turn = 0;
   private JButton diceBtn;
   // private Purchase purchase;
   private Container c;
   private GPlayer [] gPlayers = new GPlayer[2];
   private GPlayerPanel [] gPlayerPanels = new GPlayerPanel[2];
   private PurchaseDialog purchaseDialog;
   private ArrayList<GPlayerPanel> players = new ArrayList<GPlayerPanel>();
   private AllStructurePanel allStructurePanel;
   private HashMap<Land, StructurePanel> structureMap;
   private StructurePanel structurepanel;
   private Paying payingDialog;
   private Update updateDialog;
   private GameClientSocket g;
   private DiceThread dThread1;
   private DiceThread dThread2;
   private boolean isPushDiceBtn = true;

   public GameFrame(String name, GameClientSocket gameClient) throws InterruptedException {
      super("Battle Blue Marble!");
      setSize(1150, 803);
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      c = getContentPane();
      setLayout(null);
      
      g = gameClient;
      this.allStructurePanel = AllStructurePanel.getSingleStructurePanel();
      structureMap = allStructurePanel.getStructureMap();
      bgPanel = BGPanel.getBGPanel();
      gPlayers[0] = new GPlayer("assets/greenPlayer.png", name, 1);
      gPlayers[0].setOpaque(false);

      gPlayers[1] = new GPlayer("assets/redPlayer.png", "", 2);
      gPlayers[1].setOpaque(false);

      dice = new Dice();

      dice.returnPanel().setLocation(505, 270);
      dice.returnPanel().setSize(135, 240);
      dice.returnPanel().setVisible(true);
      dice.returnPanel().setOpaque(false);

      diceBtn = new JButton(" R O L L ");
      diceBtn.setSize(100, 20);
      diceBtn.setLocation(520, 520);
      diceBtn.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            diceBtn.setEnabled(false);
            
            rand1 = (int)(Math.random() *6 +1);
            rand2 = (int)(Math.random() *6 +1);
            
            gameClient.send_Message("DICE><" + name + "><" + rand1 + "><" + rand2);            
            dThread1 = makeDiceThread(dice.getLabel1(), rand1, dice.getLoadingDice(), diceBtn);
            dThread2 = makeDiceThread(dice.getLabel2(), rand2, dice.getLoadingDice(), diceBtn);
            dice.setLoadingDice(false);
            dice.setIsUpdated(true);

         }

         public DiceThread makeDiceThread(JLabel label, int randNo, boolean loadingDice, JButton btn) {
            return new DiceThread(label, randNo, loadingDice, btn);
         }
      });
      diceBtn.setOpaque(false);

      gPlayerPanels[0] = new GPlayerPanel(gPlayers[0]);
      gPlayerPanels[0].setLocation(getWidth() - gPlayerPanels[0].getWidth(), getHeight() - gPlayerPanels[0].getHeight());

      gPlayerPanels[1] = new GPlayerPanel(gPlayers[1]);
      gPlayerPanels[1].setLocation(5, 5);

      players.add(gPlayerPanels[0]);
      players.add(gPlayerPanels[1]);

      /* 가위바위보롤 턴을 정하는 method, 임의로 선 1p set */
      // player1.getPlayer().setTurn(true);

      c.add(gPlayerPanels[0]);
      c.add(gPlayerPanels[1]);
      c.add(diceBtn);
      c.add(dice.returnPanel());
      c.add(gPlayers[0]);
      c.add(gPlayers[1]);
      c.add(allStructurePanel);
      c.add(bgPanel);
      
      

      setVisible(true);
      setResizable(false);
   }

   public static void main(String[] a) throws Exception {
      new MasterPanel();
   }

   public void run() {
      GameFrame.this.repaint();

         eventInGround(gPlayerPanels[0], gPlayers[0]);
         System.out.println("나의 인덱스는 " + gPlayers[0].getIndex());
      
   }
   
   private void eventInGround(GPlayerPanel playerPanel, GPlayer player) {
      //찬스카드
      if (ground.getGround()[player.getIndex()] instanceof ChanceCard) {
         new GetChanceCard(player);
         g.send_Message("TURNEND><" + player.getName());
         diceBtn.setEnabled(false);
      }
      //특수발판
      else if (ground.getGround()[player.getIndex()] instanceof SpecialSpot) {
         inSpecialSpot(player);
         g.send_Message("TURNEND><" + player.getName());
         diceBtn.setEnabled(false);
      }
      //땅
      else if (ground.getGround()[player.getIndex()] instanceof Land) {
         Land getLand = (Land) ground.getGround()[player.getIndex()];
         inLand(playerPanel, player, getLand);
      }
   }

   private void inLand(GPlayerPanel playerPanel, GPlayer player, Land land) {
      // 소유주가 없을때 v
      if (land.getOwnerPlayerName() == null) {
         purchaseDialog = new PurchaseDialog(playerPanel, player, land, this, g);
         purchaseDialog.setVisible(true);
         setStructurepanel(structureMap.get(land));
         allStructurePanel.refresh();
      }
      // 자신의 소유지일때 v
      else if (land.getOwnerPlayerName() == player.getName()) {
         if (!land.isTourSpot()) {
            updateDialog = new Update(playerPanel, player, land, g);
            updateDialog.setVisible(true);
         }
      }
      // 상대방의 소유지일때 v
      else {
         // 통행료 지불 method v
         if (player.getPlayer().hasAngelCard())   {
            ;   
         }// 천사카드 쓰시겠습니까? 그렇다면 로직후 break 아니면 else
         else {
            if (player.getPlayer().getAllSellingBalance() + player.getPlayer().getBalance() < land.passingPrice())
               // 승패갈림
            	g.send_Message("RESULT><"+player.getName()+"><WIN");
            else {
               payingDialog = new Paying(playerPanel, player, land, this, g);
               payingDialog.setVisible(true);
               
            }
         }

      }
   }

   private void inSpecialSpot(GPlayer player) {
      int playerIndex = player.getIndex();
      // 시작지점 e
      if (ground.getGround()[playerIndex] instanceof StartSpot)
         new InStartSpot(player);
      // 감옥
      else if (ground.getGround()[playerIndex] instanceof PrisonSpot)
         new InPrisonSpot(this, player);
      // 엑스포
      else if (ground.getGround()[playerIndex] instanceof ExpoSpot)
         new InExpoSpot(this, player);
      // 세계여행
      else if (ground.getGround()[playerIndex] instanceof TrailSpot)
         new InTrailSpot(this, player);
      // 복불복
      else if (ground.getGround()[playerIndex] instanceof LuckySpot)
         new InLuckySpot(this, player);
      // 보너스게임
      else if (ground.getGround()[playerIndex] instanceof BonusSpot)
         new InBonusSpot(this, player);
      

   }

/*   public void playerMove() {
      if (turn % 2 == 1) {
         int playerIndex = gPlayers[0].getIndex();

         if (playerIndex + randSum > ground.getGround().length)
            new InStartSpot(gPlayers[0]);

         gPlayers[0].setIndex((playerIndex + randSum) % ground.getGround().length);
         playerIndex = gPlayers[0].getIndex();
         gPlayers[0].setX(ground.getGround()[playerIndex].getX1() - 15);
         gPlayers[0].setY(ground.getGround()[playerIndex].getY1() - 50);
         gPlayers[0].repaint();
      } else {
         int playerIndex = gPlayers[1].getIndex();

         if (playerIndex + randSum > ground.getGround().length)
            new InStartSpot(gPlayers[1]);

         gPlayers[1].setIndex((playerIndex + randSum) % ground.getGround().length);
         // 인덱스 변경, 재정의
         playerIndex = gPlayers[1].getIndex();
         gPlayers[1].setX(ground.getGround()[playerIndex].getX2() - 15);
         gPlayers[1].setY(ground.getGround()[playerIndex].getY2() - 50);
         gPlayers[1].repaint();
      }
   }*/

   public ArrayList<GPlayerPanel> getPlayers() {
      return players;
   }

   public JButton getDiceBtn() {
      return diceBtn;
   }

   public void setDiceBtn(JButton diceBtn) {
      this.diceBtn = diceBtn;
   }

   public StructurePanel getStructurepanel() {
      return structurepanel;
   }

   public void setStructurepanel(StructurePanel structurepanel) {
      this.structurepanel = structurepanel;
   }

   public BGPanel getBgPanel() {
      return bgPanel;
   }

   public void setBgPanel(BGPanel bgPanel) {
      this.bgPanel = bgPanel;
   }

   public GPlayer getPlayer1() {
      return gPlayers[0];
   }

   public void setPlayer1(GPlayer player1) {
      this.gPlayers[0] = player1;
   }

   public GPlayer getPlayer2() {
      return gPlayers[1];
   }

   public void setPlayer2(GPlayer player2) {
      this.gPlayers[1] = player2;
   }

   public Dice getDice() {
      return dice;
   }

   public void setDice(Dice dice) {
      this.dice = dice;
   }

   public Ground getGround() {
      return ground;
   }

   public void setGround(Ground ground) {
      this.ground = ground;
   }

   public int getRand1() {
      return rand1;
   }

   public void setRand1(int rand1) {
      this.rand1 = rand1;
   }

   public int getRand2() {
      return rand2;
   }

   public void setRand2(int rand2) {
      this.rand2 = rand2;
   }

   public int getRandSum() {
      return randSum;
   }

   public void setRandSum(int randSum) {
      this.randSum = randSum;
   }

   public int getTurn() {
      return turn;
   }

   public void setTurn(int turn) {
      this.turn = turn;
   }

   public Container getC() {
      return c;
   }

   public void setC(Container c) {
      this.c = c;
   }

   public GPlayerPanel getMyPanel() {
      return gPlayerPanels[0];
   }

   public void setMyPanel(GPlayerPanel player1Panel) {
      this.gPlayerPanels[0] = player1Panel;
   }

   public GPlayerPanel getEnemyPanel() {
      return gPlayerPanels[1];
   }

   public void setEnemyPanel(GPlayerPanel player2Panel) {
      this.gPlayerPanels[1] = player2Panel;
   }

   public PurchaseDialog getPurchaseDialog() {
      return purchaseDialog;
   }

   public void setPurchaseDialog(PurchaseDialog purchaseDialog) {
      this.purchaseDialog = purchaseDialog;
   }

   public AllStructurePanel getAllStructurePanel() {
      return allStructurePanel;
   }

   public void setAllStructurePanel(AllStructurePanel allStructurePanel) {
      this.allStructurePanel = allStructurePanel;
   }

   public HashMap<Land, StructurePanel> getStructureMap() {
      return structureMap;
   }

   public void setStructureMap(HashMap<Land, StructurePanel> structureMap) {
      this.structureMap = structureMap;
   }

   public Paying getPayingDialog() {
      return payingDialog;
   }

   public void setPayingDialog(Paying payingDialog) {
      this.payingDialog = payingDialog;
   }

   public Update getUpdateDialog() {
      return updateDialog;
   }

   public void setUpdateDialog(Update updateDialog) {
      this.updateDialog = updateDialog;
   }

   public void setPlayers(ArrayList<GPlayerPanel> players) {
      this.players = players;
   }

   public DiceThread getdThread1() {
      return dThread1;
   }

   public void setdThread1(DiceThread dThread1) {
      this.dThread1 = dThread1;
   }

   public DiceThread getdThread2() {
      return dThread2;
   }

   public void setdThread2(DiceThread dThread2) {
      this.dThread2 = dThread2;
   }
}