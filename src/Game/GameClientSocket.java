package Game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.JLabel;

import Graphics.AllStructurePanel;
import Graphics.GPlayer;
import Graphics.GPlayerPanel;
import Graphics.MasterPanel;
import Graphics.StructurePanel;
import Map.Ground;
import Map.Land;
import Method.WinOrLoseDialog;

public class GameClientSocket {
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	private final int PORT = 30001;
	private final String IP = "127.0.0.1";
	private String msg;
	private String[] getMsg = new String[10];
	private MasterPanel masterPanel;

	// 1번 index = me 2번 & index = enemy
	private String[] playersName;
	private JLabel[] playerLabel;
	private GPlayerPanel[] panels = new GPlayerPanel[2];
	private GPlayer[] gPlayers = new GPlayer[2];

	public GameClientSocket(Object obj) {

		System.out.println("GameClientSock construct");
		if (obj instanceof MasterPanel)
			masterPanel = (MasterPanel) obj;
		network();
	}

	private void network() {

		try {
			socket = new Socket(IP, PORT);
			if (socket != null) {
				Connection();
			}
		} catch (IOException e) {
			System.out.println("접속했습니다!!\n");
		}
	}

	public void Connection() {
		try {
			is = socket.getInputStream();
			dis = new DataInputStream(is);
			os = socket.getOutputStream();
			dos = new DataOutputStream(os);
		} catch (IOException e) {
			System.out.println("접속 실패!!!\n");
		}

		Thread th = new Thread(new Runnable() {
			@SuppressWarnings("null")
			@Override
			public void run() {
				while (true) {
					try {
						msg = "";
						msg = dis.readUTF(); // 무한대기
						msg = msg.trim();
						System.out.println("server -> client " + msg);
						StringTokenizer stringTokenizer = new StringTokenizer(msg, "><");
						int index = 0; // 실제로 메시지를 담을 공간의 크기를 위한 변수

						// '|'을 기준으로 메세지를 토큰화한다.
						while (stringTokenizer.hasMoreTokens()) {
							getMsg[index] = stringTokenizer.nextToken("><");
							index++;
						}

						// 기록한 index를 기반으로 실제 메시지 담을 공간 만든다.
						String editMsg[] = new String[index];

						// 실제 저장할 공간으로 메시지 옮긴다.
						for (int i = 0; i < index; i++)
							editMsg[i] = getMsg[i].trim();
						playersName = masterPanel.getPlayerName();
						switch (editMsg[0]) {
						case "CONN":
							if (editMsg.length == 3) {
								playersName[0] = editMsg[2];
								masterPanel.getGameFrame().getPlayer1().getPlayer().setName(editMsg[2]);
								break;
							} else if (editMsg.length == 4) {
								String[] playersName = masterPanel.getPlayerName();
								if (playersName[0].equals(editMsg[3])) {
									playersName[1] = editMsg[2];
									masterPanel.getGameFrame().getPlayer2().getPlayer().setName(editMsg[2]);
								} else {
									playersName[1] = editMsg[3];
									masterPanel.getGameFrame().getPlayer2().getPlayer().setName(editMsg[3]);
								}
								playerLabel = masterPanel.getPlayerLabel();
								playerLabel[0].setText(playersName[0]);
								playerLabel[1].setText(playersName[1]);
								masterPanel.setPlayerLabel(playerLabel);
								masterPanel.repaint();
								break;
							}
							// 게임 시작
						case "START":
							// 패널 체인지
							masterPanel.getGameFrame().setVisible(true);
							masterPanel.getWaitingUI().setVisible(false);

							// setName
							if (playersName[0].equals(masterPanel.getGameFrame().getPlayer1().getName())) {
								masterPanel.getGameFrame().getEnemyPanel().getNameLabel().setText(playersName[1]);
							} else {
								masterPanel.getGameFrame().getEnemyPanel().getNameLabel().setText(playersName[0]);
							}

							panels[0] = masterPanel.getGameFrame().getMyPanel();
							gPlayers[0] = masterPanel.getGameFrame().getPlayer1();
							panels[1] = masterPanel.getGameFrame().getEnemyPanel();
							gPlayers[1] = masterPanel.getGameFrame().getPlayer2();

							gPlayers[0].setX(Ground.getSingleGround().getGround()[gPlayers[0].getIndex()].getX1());
							gPlayers[0].setY(Ground.getSingleGround().getGround()[gPlayers[0].getIndex()].getY1());
							gPlayers[1].setX(Ground.getSingleGround().getGround()[gPlayers[1].getIndex()].getX2());
							gPlayers[1].setY(Ground.getSingleGround().getGround()[gPlayers[1].getIndex()].getY2());

							masterPanel.getGameFrame().setMyPanel(panels[0]);
							masterPanel.getGameFrame().setEnemyPanel(panels[1]);
							masterPanel.getGameFrame().setPlayer1(gPlayers[0]);
							masterPanel.getGameFrame().setPlayer2(gPlayers[1]);
							masterPanel.getGameFrame().getDiceBtn().setEnabled(false);

							masterPanel.repaint();
							break;
						case "TURN":
							if (editMsg[1].equals(playersName[0]))
								masterPanel.getGameFrame().getDiceBtn().setEnabled(true);
							break;
						case "MOVE":
							// MOVE><NAME><RAND1><RAND2><AFTERINDEX
							int afterIndex = Integer.parseInt(editMsg[4]);
							masterPanel.getGameFrame().setRand1(Integer.parseInt(editMsg[3]));
							masterPanel.getGameFrame().setRand2(Integer.parseInt(editMsg[4]));
							masterPanel.getGameFrame().repaint();
							// 내 턴일때
							if (editMsg[1].equals(playersName[0])) {
								System.out.println("들어옴?");
								gPlayers[0].setIndex(afterIndex);
								gPlayers[0].setX(
										Ground.getSingleGround().getGround()[gPlayers[0].getIndex()].getX1() - 15);
								gPlayers[0].setY(
										Ground.getSingleGround().getGround()[gPlayers[0].getIndex()].getY1() - 50);
								masterPanel.getGameFrame().run();
							} // 상대 턴일때
							else {
								System.out.println("들어옴?냐");
								gPlayers[1].getPlayer().setIndex(afterIndex);
								gPlayers[1].setX(
										Ground.getSingleGround().getGround()[gPlayers[1].getIndex()].getX2() - 15);
								gPlayers[1].setY(
										Ground.getSingleGround().getGround()[gPlayers[1].getIndex()].getY2() - 50);

								new DiceThread(masterPanel.getGameFrame().getDice().getLabel1(),
										Integer.parseInt(editMsg[3]),
										masterPanel.getGameFrame().getDice().getLoadingDice(),
										masterPanel.getGameFrame().getDiceBtn());
								new DiceThread(masterPanel.getGameFrame().getDice().getLabel2(),
										Integer.parseInt(editMsg[4]),
										masterPanel.getGameFrame().getDice().getLoadingDice(),
										masterPanel.getGameFrame().getDiceBtn());
							}
							masterPanel.getGameFrame().getDiceBtn().setEnabled(false);
							break;

						case "PURCHASE":
							// 구매 다이얼로그 실행
							if (editMsg[1].equals(playersName[0])) {
								gPlayers[0].getPlayer().setBalance(
										gPlayers[0].getPlayer().getBalance() - Integer.parseInt(editMsg[2]));
								if (editMsg[3].equals("TOURSPOT")) {
									masterPanel.getGameFrame()
											.setStructurepanel(masterPanel.getGameFrame().getStructureMap().get(
													Ground.getSingleGround().getIndexTourSpot(gPlayers[0].getIndex())));
									gPlayers[0].getPlayer()
											.addLand(Ground.getSingleGround().getIndexTourSpot(gPlayers[0].getIndex()));
									masterPanel.getGameFrame().getStructurepanel().getGreenLandLabel().setVisible(true);
									masterPanel.getGameFrame().getStructureMap()
											.get(Ground.getSingleGround().getIndexTourSpot(gPlayers[0].getIndex()));
								} else if (editMsg[3].equals("CITY")) {
									masterPanel.getGameFrame()
											.setStructurepanel(masterPanel.getGameFrame().getStructureMap().get(
													Ground.getSingleGround().getIndexCity(gPlayers[0].getIndex())));
									boolean[] buyType = new boolean[4];
									gPlayers[0].getPlayer()
											.addLand(Ground.getSingleGround().getIndexCity(gPlayers[0].getIndex()));
									if (editMsg[4].equals("TRUE")) {
										buyType[0] = true;
										masterPanel.getGameFrame().getStructurepanel().getGreenLandLabel()
												.setVisible(true);
									} else
										buyType[0] = false;
									if (editMsg[5].equals("TRUE")) {
										buyType[1] = true;
										masterPanel.getGameFrame().getStructurepanel().getGreenVillaLabel()
												.setVisible(true);
										masterPanel.getGameFrame().getStructurepanel().getGreenLandLabel()
												.setVisible(false);
									} else
										buyType[1] = false;
									if (editMsg[6].equals("TRUE")) {
										buyType[2] = true;
										masterPanel.getGameFrame().getStructurepanel().getGreenBuildingLabel()
												.setVisible(true);
										masterPanel.getGameFrame().getStructurepanel().getGreenLandLabel()
												.setVisible(false);
									} else
										buyType[2] = false;
									if (editMsg[7].equals("TRUE")) {
										buyType[3] = true;
										masterPanel.getGameFrame().getStructurepanel().getGreenHotelLabel()
												.setVisible(true);
										masterPanel.getGameFrame().getStructurepanel().getGreenLandLabel()
												.setVisible(false);
									} else
										buyType[3] = false;
									Ground.getSingleGround().getIndexCity(gPlayers[0].getIndex()).setStructure(buyType);
								}
							} else if (editMsg[1].equals(playersName[1])) {
								gPlayers[1].getPlayer().setBalance(
										gPlayers[1].getPlayer().getBalance() - Integer.parseInt(editMsg[2]));
								if (editMsg[3].equals("TOURSPOT")) {
									masterPanel.getGameFrame()
											.setStructurepanel(masterPanel.getGameFrame().getStructureMap().get(
													Ground.getSingleGround().getIndexTourSpot(gPlayers[1].getIndex())));
									gPlayers[1].getPlayer()
											.addLand(Ground.getSingleGround().getIndexTourSpot(gPlayers[1].getIndex()));
									masterPanel.getGameFrame().getStructurepanel().getRedLandLabel().setVisible(true);
								} else if (editMsg[3].equals("CITY")) {
									masterPanel.getGameFrame()
											.setStructurepanel(masterPanel.getGameFrame().getStructureMap().get(
													Ground.getSingleGround().getIndexCity(gPlayers[1].getIndex())));
									boolean[] buyType = new boolean[4];
									gPlayers[1].getPlayer()
											.addLand(Ground.getSingleGround().getIndexCity(gPlayers[1].getIndex()));
									if (editMsg[4].equals("TRUE")) {
										buyType[0] = true;
										masterPanel.getGameFrame().getStructurepanel().getRedLandLabel()
												.setVisible(true);
									} else
										buyType[0] = false;
									if (editMsg[5].equals("TRUE")) {
										buyType[1] = true;
										masterPanel.getGameFrame().getStructurepanel().getRedVillaLabel()
												.setVisible(true);
										masterPanel.getGameFrame().getStructurepanel().getRedLandLabel()
												.setVisible(false);
									} else
										buyType[1] = false;
									if (editMsg[6].equals("TRUE")) {
										buyType[2] = true;
										masterPanel.getGameFrame().getStructurepanel().getRedBuildingLabel()
												.setVisible(true);
										masterPanel.getGameFrame().getStructurepanel().getRedLandLabel()
												.setVisible(false);
									} else
										buyType[2] = false;
									if (editMsg[7].equals("TRUE")) {
										buyType[3] = true;
										masterPanel.getGameFrame().getStructurepanel().getRedHotelLabel()
												.setVisible(true);
										masterPanel.getGameFrame().getStructurepanel().getRedLandLabel()
												.setVisible(false);
									} else
										buyType[3] = false;
									Ground.getSingleGround().getIndexCity(gPlayers[1].getIndex()).setStructure(buyType);
								}
							}
							break;
						case "INTERCEPT":
							// 인터셉트 다이얼로그 실행
							if (editMsg[1].equals(playersName[0])) {
								boolean[] buyType = Ground.getSingleGround().getIndexCity(gPlayers[0].getIndex()).getStructure();
								gPlayers[0].getPlayer().setBalance(
										gPlayers[0].getPlayer().getBalance() - Integer.parseInt(editMsg[2]));

								masterPanel.getGameFrame()
										.setStructurepanel(masterPanel.getGameFrame().getStructureMap()
												.get(Ground.getSingleGround().getIndexCity(gPlayers[0].getIndex())));
								
								gPlayers[0].getPlayer()
										.addLand(Ground.getSingleGround().getIndexCity(gPlayers[0].getIndex()));
								gPlayers[1].getPlayer().sellingLand(Ground.getSingleGround().getIndexCity(gPlayers[1].getIndex()));
								if (buyType[0] == true) {
									masterPanel.getGameFrame().getStructurepanel().getRedLandLabel().setVisible(false);
									masterPanel.getGameFrame().getStructurepanel().getGreenLandLabel().setVisible(true);
								}
								if (buyType[1] == true) {
									masterPanel.getGameFrame().getStructurepanel().getRedVillaLabel().setVisible(false);
									masterPanel.getGameFrame().getStructurepanel().getGreenVillaLabel().setVisible(true);
									masterPanel.getGameFrame().getStructurepanel().getGreenLandLabel().setVisible(false);
								}
								if (buyType[2] == true) {
									masterPanel.getGameFrame().getStructurepanel().getRedBuildingLabel().setVisible(false);
									masterPanel.getGameFrame().getStructurepanel().getGreenBuildingLabel().setVisible(true);
									masterPanel.getGameFrame().getStructurepanel().getGreenLandLabel().setVisible(false);
								}
								if (buyType[3] == true) {
									masterPanel.getGameFrame().getStructurepanel().getRedHotelLabel().setVisible(false);
									masterPanel.getGameFrame().getStructurepanel().getGreenHotelLabel().setVisible(true);
									masterPanel.getGameFrame().getStructurepanel().getGreenLandLabel().setVisible(false);
								}
								//masterPanel.getGameFrame().getStructurepanel().

							} else if (editMsg[1].equals(playersName[1])) {
								boolean[] buyType = Ground.getSingleGround().getIndexCity(gPlayers[1].getIndex()).getStructure();
								gPlayers[1].getPlayer().setBalance(
										gPlayers[1].getPlayer().getBalance() - Integer.parseInt(editMsg[2]));
								gPlayers[0].getPlayer().sellingLand(Ground.getSingleGround().getIndexCity(gPlayers[1].getIndex()));
								masterPanel.getGameFrame()
										.setStructurepanel(masterPanel.getGameFrame().getStructureMap()
												.get(Ground.getSingleGround().getIndexCity(gPlayers[1].getIndex())));
								
								
								if (buyType[0] == true) {
									masterPanel.getGameFrame().getStructurepanel().getGreenLandLabel().setVisible(false);
									masterPanel.getGameFrame().getStructurepanel().getRedLandLabel().setVisible(true);
								}
								if (buyType[1] == true) {
									masterPanel.getGameFrame().getStructurepanel().getGreenVillaLabel().setVisible(false);
									masterPanel.getGameFrame().getStructurepanel().getRedVillaLabel().setVisible(true);
									masterPanel.getGameFrame().getStructurepanel().getRedLandLabel().setVisible(false);
								}
								if (buyType[2] == true) {
									masterPanel.getGameFrame().getStructurepanel().getGreenBuildingLabel().setVisible(false);
									masterPanel.getGameFrame().getStructurepanel().getRedBuildingLabel().setVisible(true);
									masterPanel.getGameFrame().getStructurepanel().getRedLandLabel().setVisible(false);
								}
								if (buyType[3] == true) {
									masterPanel.getGameFrame().getStructurepanel().getGreenHotelLabel().setVisible(false);
									masterPanel.getGameFrame().getStructurepanel().getRedHotelLabel().setVisible(true);
									masterPanel.getGameFrame().getStructurepanel().getRedLandLabel().setVisible(false);
								}
							}
							break;

						case "PAYING":
							// 지불 다이얼로그 실행
							if (editMsg[1].equals(playersName[0])) {
								gPlayers[0].getPlayer().setBalance(
										gPlayers[0].getPlayer().getBalance() - Integer.parseInt(editMsg[2]));
							} else if (editMsg[1].equals(playersName[1])) {
								gPlayers[1].getPlayer().setBalance(
										gPlayers[1].getPlayer().getBalance() - Integer.parseInt(editMsg[2]));
							}
							break;

						case "UPDATE":
							// 판매 다이얼로그 실행
							//UPDATE><TRUE><TRUE><TRUE><TRUE><가격
							if (editMsg[1].equals(playersName[0])) {
								boolean[] buyType = Ground.getSingleGround().getIndexCity(gPlayers[0].getIndex()).getStructure();
								gPlayers[0].getPlayer().setBalance(
										gPlayers[0].getPlayer().getBalance() - Integer.parseInt(editMsg[2]));

								masterPanel.getGameFrame()
										.setStructurepanel(masterPanel.getGameFrame().getStructureMap()
												.get(Ground.getSingleGround().getIndexCity(gPlayers[0].getIndex())));
								
								gPlayers[0].getPlayer()
										.addLand(Ground.getSingleGround().getIndexCity(gPlayers[0].getIndex()));
								
								if (buyType[0] == true) {
									masterPanel.getGameFrame().getStructurepanel().getRedLandLabel().setVisible(false);
									masterPanel.getGameFrame().getStructurepanel().getGreenLandLabel().setVisible(true);
								}
								if (buyType[1] == true) {
									masterPanel.getGameFrame().getStructurepanel().getRedVillaLabel().setVisible(false);
									masterPanel.getGameFrame().getStructurepanel().getGreenVillaLabel().setVisible(true);
									masterPanel.getGameFrame().getStructurepanel().getGreenLandLabel().setVisible(false);
								}
								if (buyType[2] == true) {
									masterPanel.getGameFrame().getStructurepanel().getRedBuildingLabel().setVisible(false);
									masterPanel.getGameFrame().getStructurepanel().getGreenBuildingLabel().setVisible(true);
									masterPanel.getGameFrame().getStructurepanel().getGreenLandLabel().setVisible(false);
								}
								if (buyType[3] == true) {
									masterPanel.getGameFrame().getStructurepanel().getRedHotelLabel().setVisible(false);
									masterPanel.getGameFrame().getStructurepanel().getGreenHotelLabel().setVisible(true);
									masterPanel.getGameFrame().getStructurepanel().getGreenLandLabel().setVisible(false);
								}
								//masterPanel.getGameFrame().getStructurepanel().
							} else if (editMsg[1].equals(playersName[1])) {
								boolean[] buyType = Ground.getSingleGround().getIndexCity(gPlayers[1].getIndex()).getStructure();
								gPlayers[1].getPlayer().setBalance(
										gPlayers[1].getPlayer().getBalance() - Integer.parseInt(editMsg[2]));

								masterPanel.getGameFrame()
										.setStructurepanel(masterPanel.getGameFrame().getStructureMap()
												.get(Ground.getSingleGround().getIndexCity(gPlayers[1].getIndex())));
								
								gPlayers[1].getPlayer()
										.addLand(Ground.getSingleGround().getIndexCity(gPlayers[1].getIndex()));
								
								if (buyType[0] == true) {
									masterPanel.getGameFrame().getStructurepanel().getGreenLandLabel().setVisible(false);
									masterPanel.getGameFrame().getStructurepanel().getRedLandLabel().setVisible(true);
								}
								if (buyType[1] == true) {
									masterPanel.getGameFrame().getStructurepanel().getGreenVillaLabel().setVisible(false);
									masterPanel.getGameFrame().getStructurepanel().getRedVillaLabel().setVisible(true);
									masterPanel.getGameFrame().getStructurepanel().getRedLandLabel().setVisible(false);
								}
								if (buyType[2] == true) {
									masterPanel.getGameFrame().getStructurepanel().getGreenBuildingLabel().setVisible(false);
									masterPanel.getGameFrame().getStructurepanel().getRedBuildingLabel().setVisible(true);
									masterPanel.getGameFrame().getStructurepanel().getRedLandLabel().setVisible(false);
								}
								if (buyType[3] == true) {
									masterPanel.getGameFrame().getStructurepanel().getGreenHotelLabel().setVisible(false);
									masterPanel.getGameFrame().getStructurepanel().getRedHotelLabel().setVisible(true);
									masterPanel.getGameFrame().getStructurepanel().getRedLandLabel().setVisible(false);
								}
							}
							break;
						case "RESULT":
		                     if (editMsg[1].equals(playersName[0]))
		                        new WinOrLoseDialog(playersName[0], true);
		                     else
		                        new WinOrLoseDialog(playersName[0], false);
		                     break;
						default:
							// break;
						}
					} catch (IOException e) {
						System.out.println("수신 에러!!\n");
						try {
							os.close();
							is.close();
							dos.close();
							dis.close();
							socket.close();
							break;
						} catch (Exception e1) {
							e1.printStackTrace();
						}

					}
				} // while 끝
			}// run 끝
		});
		th.start();
	}

	public void send_Message(String str) {
		try {
			dos.writeUTF(str);
			System.out.println("client -> server " + str);

		} catch (IOException e) {

			System.out.println("메시지 송신 오류!!\n");
		}
	}
}

// 기본 로직
/*
 * panels[0] = masterPanel.getGameFrame().getMyPanel(); gPlayers[0] =
 * masterPanel.getGameFrame().getPlayer1(); panels[1] =
 * masterPanel.getGameFrame().getEnemyPanel(); gPlayers[1] =
 * masterPanel.getGameFrame().getPlayer2();
 * 
 * gPlayers[0].setX(Ground.getSingleGround().getGround()[gPlayers[0].getIndex()]
 * .getX1());
 * gPlayers[0].setY(Ground.getSingleGround().getGround()[gPlayers[0].getIndex()]
 * .getY1());
 * gPlayers[1].setX(Ground.getSingleGround().getGround()[gPlayers[1].getIndex()]
 * .getX2());
 * gPlayers[1].setY(Ground.getSingleGround().getGround()[gPlayers[1].getIndex()]
 * .getY2());
 * 
 * masterPanel.getGameFrame().setMyPanel(panels[0]);
 * masterPanel.getGameFrame().setEnemyPanel(panels[1]);
 * masterPanel.getGameFrame().setPlayer1(gPlayers[0]);
 * masterPanel.getGameFrame().setPlayer2(gPlayers[1]); masterPanel.repaint();
 */
