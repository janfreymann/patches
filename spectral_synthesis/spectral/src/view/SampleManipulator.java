package view;

import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sound.Spectrogram;

import control.SoundManager;

public class SampleManipulator extends JFrame {
	private static final long serialVersionUID = 1L;
	private final SoundManager man;
	private final PaintPanel paint;
	final SpinnerListModel windowSizesModel;
	final SpinnerNumberModel fileSizesModel;
	private final String[] editModes = { "Filter", "Unfilter", "Paint", "Listen 1", "Listen 2"};
	final JComboBox modeSelector;

	public SampleManipulator(SoundManager sm) {
		super("Proof of Concept");
		this.man = sm;
		final SampleManipulator smp = this;
		final JButton btLoadSample = new JButton("Load");
		final JButton btComputeSpecGram = new JButton("Spectrogram");
		final JButton btReconstruct = new JButton("Reconstruct");
		final JButton btPlayback = new JButton("Play");
		final JButton btStoreWave = new JButton("Store");
		final JButton btZoom = new JButton("Zoom");
		final JButton btResetZoom = new JButton("Reset Zoom");
		
		this.setFocusable(true);
		this.requestFocusInWindow();
		
		// combo Box for editing mode:
		
		//Create the combo box, select item at index 4.
		//Indices start at 0, so 4 specifies the pig.
		modeSelector = new JComboBox(editModes); //^^
		
		modeSelector.setSelectedIndex(0);
		modeSelector.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				paint.setEditMode(modeSelector.getSelectedIndex());
			}
			
		});
		
				
		btResetZoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				paint.resetZoom();
				paint.repaint();
			}
		});
		
		btZoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				paint.beginBoxZoom();
			}
		});
		
		btStoreWave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();
				//fc.setCurrentDirectory(new File("C:\\water\\loops"));
				fc.showSaveDialog(smp);
				man.storeWaveFile(fc.getSelectedFile());
			} 
		});	
		
		btPlayback.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				man.playContent();
			}
		});
		btReconstruct.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				man.setContentSpectrogram(paint.getSpec());
				man.reconstructFromSpectrogram();
			}
		});

		btLoadSample.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();
				//fc.setCurrentDirectory(new File("C:\\water\\loops"));
				fc.showOpenDialog(smp);
				man.importWaveFile(fc.getSelectedFile());
			}
		});
		
		btComputeSpecGram.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionComputeSpectrogram();
			}
		});
		
	    this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
	    JPanel panel = new JPanel();
	    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	    panel.add(btLoadSample);
	    panel.add(btStoreWave);
	    panel.add(btComputeSpecGram);
	    panel.add(btPlayback);
	    panel.add(btReconstruct);
	    //panel.add(btFilterMode);
	    panel.add(btZoom);
	    panel.add(btResetZoom);
	    panel.add(modeSelector);
	    this.add(panel);
	    
	    paint = new PaintPanel(man);
	    this.add(paint);
	    
	    JPanel moreControls = new JPanel();
	    
	    final JButton btInvert = new JButton("Invert filter");
	    
	    btInvert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				paint.invertFilter();
			}
	    	
	    });
	    
	    moreControls.add(btInvert);
	    
	    JLabel spinnerC = new JLabel("cursor size: ");
	    
	    moreControls.add(spinnerC);
	    
	    //add cursor and window size:
	    
	    //final Integer[] cursorSizes = {5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30};
	    final SpinnerModel cursorSizesModel = new SpinnerNumberModel(5, 1, 100, 1);
	    //final SpinnerListModel cursorSizesModel = new SpinnerListModel(cursorSizes);
	    final JSpinner cursorSpinner = new JSpinner(cursorSizesModel);
	    Dimension csd = cursorSpinner.getPreferredSize();
	    csd.width = 40;
	    cursorSpinner.setPreferredSize(csd);
	    
	    cursorSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				Object val = cursorSizesModel.getValue();
				if(val instanceof Integer) { //stooopid
					paint.setCursorSize(((Integer) cursorSizesModel.getValue()).intValue());
				}
			} });
	    
	    moreControls.add(cursorSpinner);
	    
	    JLabel spinnerW = new JLabel("window size: ");
	    
	    moreControls.add(spinnerW);
	    
	    final Integer[] windowSizes = {512, 1024, 2048, 4096, 8192, 16384, 32768, 65536};
	    windowSizesModel = new SpinnerListModel(windowSizes);
	    final JSpinner windowSpinner = new JSpinner(windowSizesModel);
	    Dimension wsd = windowSpinner.getPreferredSize();
	    wsd.width = 80;
	    windowSpinner.setPreferredSize(wsd);
	    
	    windowSizesModel.setValue(8192);
	    
	    moreControls.add(windowSpinner);
	    
	    //add signal generators:
	    
	    final JButton btNoise = new JButton("white noise");
	    
	    btNoise.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				man.generateNoise((int) (Math.pow(2.0, ((Integer) fileSizesModel.getValue()).intValue())));
			}
		});
	    
	    moreControls.add(btNoise);
	    
	    final JButton btSilence = new JButton("silence");
	    
	    btSilence.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				man.generateSilence((int) (Math.pow(2.0, ((Integer) fileSizesModel.getValue()).intValue())));
			}
		});
	    
	    moreControls.add(btSilence);
	    
	    JLabel fileSizeLabel = new JLabel("file size (2^n): ");
	    
	    moreControls.add(fileSizeLabel);
	    
	    fileSizesModel = new SpinnerNumberModel(16, 8, 20, 1);
	    JSpinner fileSizeSpinner = new JSpinner(fileSizesModel);
	    
	    moreControls.add(fileSizeSpinner);
	    
	    this.add(moreControls);
	    
	    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    
	    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());
	    
	    this.setSize(800, 800);
	    this.setResizable(false);
	    this.setVisible(true);
	}
	
	protected void actionComputeSpectrogram() {
		Object val = windowSizesModel.getValue();
		if(val instanceof Integer) { //stoopid again
			int windowSize = ((Integer) val).intValue() / 2;
			man.computeContentSpectrogram(windowSize);
			paint.drawSpectroGram(man.getContentSpectrogram());
		}
	}

	private void createSpectralIntersection(SoundManager man2) {
		man.computeContentSpectrogram(4096);
		man2.computeContentSpectrogram(4096);
		man.setContentSpectrogram(Spectrogram.intersect(man.getContentSpectrogram(), man2.getContentSpectrogram()));
		paint.setSpec(man.getContentSpectrogram());
	}

	private class MyDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_TYPED) {
                char kc = e.getKeyChar();
                if(kc == 'u') {
                	paint.setEditMode(1);
                	modeSelector.setSelectedIndex(1);
                }
                else if(kc == 'f') {
                	paint.setEditMode(0);
                	modeSelector.setSelectedIndex(0);
                }
                else if(kc == 'p') {
                	paint.setEditMode(2);
                	modeSelector.setSelectedIndex(2);
                }
                else if(kc == 'l') {
                	paint.setEditMode(3);
                	modeSelector.setSelectedIndex(3);
                }
                else if(kc == 'L') {
                	paint.setEditMode(4);
                	modeSelector.setSelectedIndex(4);
                }
                else if(kc == 'z') {
                	paint.beginBoxZoom();
                }
                else if(kc == 'x') {
                	paint.resetZoom();
                	paint.repaint();
                }
                else if(kc == 's') {
                	actionComputeSpectrogram();
                }
                else if(kc == ' ') {
                	man.playContent();
                }
            }
            return false;
        }
    }
}
