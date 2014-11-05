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
	private final JCheckBoxMenuItem useTrayMessage=new JCheckBoxMenuItem(
		useTrayMessageLabel,
		settings.getBoolean(useTrayMessageLabel, SystemTray.isSupported())
	);
	private final JCheckBoxMenuItem enabled=new JCheckBoxMenuItem("Enabled",true);
	private TimerTask tempDisableTask=new TimerTask() {
		@Override
		public void run(){
		}
	};
	final java.util.Timer tempDisableTimer=new java.util.Timer();

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
			JMenuItem tempDisable = new JMenuItem("Disable for 15 min");
			tempDisable.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e){
					enabled.setSelected(false);
					tempDisableTask.cancel();
					tempDisableTask=new TimerTask() {
						@Override
						public void run(){
							enabled.setSelected(true);
						}
					};
					tempDisableTimer.schedule(
						tempDisableTask,
						15*60*1000
					);
				}
			});
			enabled.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e){
					tempDisableTask.cancel();
				}
			});
			JMenuItem about = new JMenuItem("About");
			about.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e){
					JOptionPane.showMessageDialog(
						null,
						name+" was written in Java by Oliver Zurr.\n"
						+ "It willt help you blink more often, even though\n"
						+ "humans blink less when staring at their screen.\n"
						+ "This is version 4.\n"
						+ "Available on GitHub.com and SourceForge.net",
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
			trayMenu.add(tempDisable);
			trayMenu.add(useTrayMessage);
			trayMenu.add(about);
			trayMenu.add(new JSeparator());
			trayMenu.add(enabled);
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
					if(enabled.isSelected()){
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
