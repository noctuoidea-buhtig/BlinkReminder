package blinkreminder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.TimerTask;
import javax.swing.*;

public class ImageShapeWindow extends JFrame {

    final static boolean debug = false;
//	private final boolean forceGeneratePath=true;
    JLabel label;
	static ImageShapeWindow eye=null;

    public ImageShapeWindow(Color target, BufferedImage bi) {
        super("Blink Reminding");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setUndecorated(true);
		final Dimension imageSize=new Dimension(bi.getWidth(), bi.getHeight());

        setSize(imageSize);
		
		setFocusable(false);
		setFocusableWindowState(false);
		setAutoRequestFocus(false);
		try{
			setOpacity((float) 0.5);
		}catch(java.lang.UnsupportedOperationException oue){}
		
		GeneralPath path;
		if (debug) System.out.println("getting path");
		try{
//			if(forceGeneratePath) throw new IOException("Uncomment to force generate path");
			path=(GeneralPath) new ObjectInputStream(ClassLoader.getSystemResourceAsStream("pathes/big-eye.path")).readObject();
			if (debug) System.out.println("loaded path");
		}catch(	IOException | ClassNotFoundException ex){
			if (debug) System.out.println("generating path");
			path = getOutline(target, bi);
			if (debug) System.out.println("saving path");
			try{
				new ObjectOutputStream(
					new FileOutputStream(
						System.getProperty("user.dir")+"/src/pathes/big-eye.path"
					)
				).writeObject(path);
				System.out.println("saved path");
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		if (debug) System.out.println("created path");

        setShape(path);
		if (debug) System.out.println("setting shape");
        getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        label = new JLabel(
                "<html><center><img src=\""
                + ClassLoader.getSystemResource("icons/big-eye.png")
                + "\" /></center></html>"
        );
		if (debug) System.out.println("added label");

        add(label);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
    }

    private GeneralPath getOutline(Color target, BufferedImage bi) {
        GeneralPath gp = new GeneralPath();
        if (debug) {
            System.out.println("Scanning...");
        }
        boolean cont = false;
        int lastColor = target.getRGB();
		int xx;
		int yy;
		for (xx = 0; xx < bi.getWidth(); xx++) {
colScanDown:	for (yy = 0; yy < bi.getHeight(); yy++) {
                if (bi.getRGB(xx, yy) != lastColor) {
					if(cont){
						gp.lineTo(xx, yy);
					}else{
						gp.moveTo(xx, yy);
						cont=true;
					}
					break colScanDown;
                }
            }
        }
		for (xx = bi.getWidth()-1; xx > -1 ; xx--) {
colScanUp:	for (yy = bi.getHeight()-1; yy > -1; yy--) {
                if (bi.getRGB(xx, yy) != lastColor) {
						gp.lineTo(xx, yy);
					break colScanUp;
                }
            }
        }
        if (debug) {
            System.out.println("Scanning complete");
        }
        gp.closePath();
        return gp;
    }

    protected static ImageShapeWindow createEye() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.getDefaultScreenDevice().isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT)) {
            Image img = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("icons/big-eye-mask.png"));
            try {
                synchronized (img) {
                    while (img.getWidth(null) == -1 || img.getHeight(null) == -1) {
                        img.wait();
                    }
                    img.wait();
                }
                BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D bGr = bimage.createGraphics();
                bGr.drawImage(img, 0, 0, null);
                bGr.dispose();
                final ImageShapeWindow eye= new ImageShapeWindow(Color.BLACK, bimage);
				eye.label.addMouseListener(new MouseAdapter(){
					@Override
					public void mouseReleased(MouseEvent me) {
						eye.close();
					}
				});
                return eye;
            } catch (InterruptedException ie) {
                System.out.println("Interrupted while waiting for image to be loaded!");
            }
        }
        return null;
    }

	private void close(){
		setVisible(false);
//		dispose();
	}

    public static void spawnEye() {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
						if(eye==null) eye=createEye();
						eye.setVisible(true);
						(new java.util.Timer()).schedule(
							new TimerTask() {
								@Override
								public void run() {
									eye.close();
								}

							}, 500
						);
                    }
                }
        );
    }

}
