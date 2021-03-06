package Method;

import Graphics.GPlayer;
import Graphics.GameFrame;

public class InStartSpot {

	public InStartSpot(GPlayer player) {
		int balance = player.getPlayer().getBalance();
		player.getPlayer().setBalance(balance + 300000);

		System.out.println(player.getName() + "이 밟았다 !" + this.getClass().getName());
		
	}
}