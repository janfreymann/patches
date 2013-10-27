package sound;

import com.synthbot.jasiohost.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class SoundGUI extends JFrame implements AsioDriverListener {
	
	private static final long serialVersionUID = 1L;
	
	private AsioDriver asioDriver;
	private Set<AsioChannel> activeChannels;
	private int sampleIndex; //able to track ~13 hours of playback before overflow (assuming sample rat of 44.1 khz)
	private int bufferSize;
	private double sampleRate;
	private float[] output;
	
	//variables to control playback of buffers:
	private int playbackStart;
	private int playbackStop;
	private float[] playbackBuffer;
	private final int playbackDelay = 512; //512 samples delay
	
	public SoundGUI() {
	    super("ASIO Host");
	    
	    activeChannels = new HashSet<AsioChannel>();
	    
	    final JComboBox comboBox = new JComboBox(AsioDriver.getDriverNames().toArray());
	    final JButton buttonStart = new JButton("Start");
	    final JButton buttonStop = new JButton("Stop");
	    final JButton buttonControlPanel = new JButton("Control Panel");
	    
	    final AsioDriverListener host = this;
	    buttonStart.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent event) {
	        if (asioDriver == null) {
	          asioDriver = AsioDriver.getDriver(comboBox.getSelectedItem().toString());
	          asioDriver.addAsioDriverListener(host);
	          activeChannels.add(asioDriver.getChannelOutput(0));
	          activeChannels.add(asioDriver.getChannelOutput(1));
	          sampleIndex = 0;
	          bufferSize = asioDriver.getBufferPreferredSize();
	          sampleRate = asioDriver.getSampleRate();
	          output = new float[bufferSize];
	          asioDriver.createBuffers(activeChannels);
	          asioDriver.start();
	        }
	      }
	    });
	    
	    buttonStop.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent event) {
	        if (asioDriver != null) {
	          asioDriver.shutdownAndUnloadDriver();
	          activeChannels.clear();
	          asioDriver = null;
	        }
	      }
	    });

	    buttonControlPanel.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent event) {
	        if (asioDriver != null && 
	            asioDriver.getCurrentState().ordinal() >= AsioDriverState.INITIALIZED.ordinal()) {
	          asioDriver.openControlPanel();          
	        }
	      }
	    });
	    
	    this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
	    JPanel panel = new JPanel();
	    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	    this.add(comboBox);
	    panel.add(buttonStart);
	    panel.add(buttonStop);
	    panel.add(buttonControlPanel);
	    this.add(panel);
	    
	    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    this.addWindowListener(new WindowAdapter() {
	      @Override
	      public void windowClosing(WindowEvent event) {
	        if (asioDriver != null) {
	        	System.out.println("shutdown and unload");
	          asioDriver.shutdownAndUnloadDriver();
	        }
	      }
	    });
	    
	    this.setSize(240, 85);
	    this.setResizable(false);
	    this.setVisible(true);
	  }
	
      public File openWaveFile() {
    	final JFileChooser fc = new JFileChooser();
    	//fc.setCurrentDirectory(new File("C:\\water\\loops"));
    	fc.showOpenDialog(this);
    	return fc.getSelectedFile();
      }
	  
	  public void bufferSwitch(long systemTime, long samplePosition, Set<AsioChannel> channels) {
	    for (int i = 0; i < bufferSize; i++, sampleIndex++) {
	      //output[i] = (float) Math.sin(2 * Math.PI * sampleIndex * 440.0 / sampleRate);
	    	if((sampleIndex >= playbackStart) && (sampleIndex < playbackStop)) {
	    		output[i] = playbackBuffer[sampleIndex-playbackStart];
	    		//output[i] = (float) 0.0;
	    	}
	    	else {
	    		output[i] = (float) 0.0;
	    	}
	    }
	    for (AsioChannel channelInfo : channels) {
	      channelInfo.write(output);
	    }
	  }
	  
	  public void setPlaybackBuffer(float[] buffer) {
		  playbackStart = sampleIndex + playbackDelay;
		  playbackBuffer = buffer;
		  playbackStop = playbackStart + buffer.length;
	  }
	  
	  public void bufferSizeChanged(int bufferSize) {
	    System.out.println("bufferSizeChanged() callback received.");
	  }

	  public void latenciesChanged(int inputLatency, int outputLatency) {
	    System.out.println("latenciesChanged() callback received.");
	  }

	  public void resetRequest() {
	    /*
	     * This thread will attempt to shut down the ASIO driver. However, it will
	     * block on the AsioDriver object at least until the current method has returned.
	     */
	    new Thread() {
	      @Override
	      public void run() {
	        System.out.println("resetRequest() callback received. Returning driver to INITIALIZED state.");
	        asioDriver.returnToState(AsioDriverState.INITIALIZED);
	      }
	    }.start();
	  }

	  public void resyncRequest() {
	    System.out.println("resyncRequest() callback received.");
	  }

	  public void sampleRateDidChange(double sampleRate) {
	    System.out.println("sampleRateDidChange() callback received.");
	  }
}
