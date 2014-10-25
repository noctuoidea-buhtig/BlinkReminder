package blinkreminder;

import java.awt.*;
import java.awt.event.*;
import java.util.TimerTask;
import java.util.prefs.Preferences;
import javax.swing.*;

public class BlinkReminder{
	private TrayIcon mainIcon=null;
	private SystemTray tray=null;
	private static final String useTrayMessageLabel="Use Tray Message";
	private final Preferences settings=Preferences.userNodeForPackage(getClass());
	private final JRadioButtonMenuItem useTrayMessage=new JRadioButtonMenuItem(
		useTrayMessageLabel,
		settings.getBoolean(useTrayMessageLabel, SystemTray.isSupported())
	);

	public BlinkReminder(){
		final String name=getClass().getSimpleName();
		if (SystemTray.isSupported()) {
			tray=SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().createImage(
					ClassLoader.getSystemResource("icons/eye.png")
			);
			mainIcon = new TrayIcon(
					image,
					"Blink Reminder",
					null
			);
			mainIcon.setImageAutoSize(true);
			mainIcon.setToolTip(name);
			final JPopupMenu trayMenu=new JPopupMenu(name);
			useTrayMessage.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e){
					settings.putBoolean(useTrayMessageLabel, useTrayMessage.isSelected());
				}
			});
			JMenuItem about = new JMenuItem("About");
			about.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e){
					JOptionPane.showMessageDialog(
						null,
						name+" was written in Java by Oliver Zurr.\n"
						+ "It is supposed to help you blink more often, because\n"
						+ "humans blink less when staring at their screen.\n"
						+ "This is version 2. Available on GitHub.com .",
						"About "+name,
						JOptionPane.INFORMATION_MESSAGE
					);
				}
			});
			JMenuItem exit = new JMenuItem("Exit");
			exit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e){
					System.exit(0);
				}
			});
			trayMenu.add(useTrayMessage);
			trayMenu.add(about);
			trayMenu.add(new JSeparator());
			trayMenu.add(exit);
			mainIcon.addMouseListener(
				new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent e) {
						if(e.isPopupTrigger()){
							trayMenu.setLocation(e.getX(), e.getY());
							trayMenu.setInvoker(trayMenu);
							trayMenu.setVisible(true);
						}
					}
					@Override
					public void mousePressed(MouseEvent e){
						mouseReleased(e);
					}
				}
			);
            try {
                tray.add(mainIcon);
            } catch (AWTException e) {
				e.printStackTrace();
            }
			
		}
		(new java.util.Timer()).schedule(
			new TimerTask() {
				@Override
				public void run() {
					if(
						mainIcon==null || // no systray, use shaped window instead
						!useTrayMessage.isSelected()
					){
						ImageShapeWindow.spawnEye();
					}else{
						mainIcon.displayMessage("😉", "Blink!",
						TrayIcon.MessageType.NONE);
						if (System.getProperty("os.name").contains("Linux")){
							(new java.util.Timer()).schedule( // Linux messages often stay
								new TimerTask() {
									@Override
									public void run() {
										tray.remove(mainIcon);
										try{
											tray.add(mainIcon);
										}catch(AWTException ex){
											ex.printStackTrace();
										}
									}
								},
								3000
							);
						}
					}
				}
			}, (long) 1, (long) 10000
		);
	}
	
	/**
	 @param args the command line arguments
	 */
	public static void main(String[] args){
		final BlinkReminder app = new BlinkReminder();
	}
}
