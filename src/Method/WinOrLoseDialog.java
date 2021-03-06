package Method;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class WinOrLoseDialog extends JDialog{
	private JLabel label;
	private String result;
	private JButton exitBtn;
	public WinOrLoseDialog(String name, boolean isWin) {
		setSize(250, 100);
		setLocation(450, 320);
		setLayout(new GridLayout(2, 1));
		
		if(isWin) {
			setTitle("  승  리  ");
			result = "승리!!";
		}
		
		else {
			setTitle("  패 배 ");
			result = "패배!! ㅠㅠ";
		}
		
		label = new JLabel(name + "의 " + result);
		exitBtn = new JButton("  종  료  ");
		exitBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
		});
		
		add(label);
		add(exitBtn);
		setVisible(true);
	}
}
