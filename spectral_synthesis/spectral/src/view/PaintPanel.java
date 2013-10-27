package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import control.SoundManager;

import sound.Complex;
import sound.FFT;
import sound.Spectrogram;
import sound.SpectrogramContainer;

class PaintPanel extends JPanel implements MouseMotionListener, MouseListener {
	private static final long serialVersionUID = 1L;
	int mode = 1;  //1: default mode (filter)
	private SpectrogramContainer spec;
	
	public SpectrogramContainer getSpec() {
		return spec;
	}
	public void setSpec(SpectrogramContainer sp) {
		this.spec = sp;
		this.repaint();
	}

	private int mouseX;
	private int mouseY;
	
	private int cursorSize = 5;

	private int boxZoom = 0; 
	private int bX1, bY1, bX2, bY2;
	
	private int editMode = 0;  //1: filter, 0: unfilter
	
	private SoundManager man;
	
	//spec dimensions and position:
	private int width = 700;
	private int height = 600;
	private int startX = 100;
	private int startY = 0;
	
	//displayed part of spectrogram:
	private int specFrom;
	private int specTo;
	private int windowFrom;
	private int windowTo;
	private double freqFrom;
	private double freqTo;
	private double freqRange;
	
	//private boolean mouseButtonDown = false;

    public int getFilterMode() {
		return editMode;
	}

	public void setEditMode(int filterMode) {
		this.editMode = filterMode;
	}

	public PaintPanel(SoundManager man) {
        setBorder(BorderFactory.createLineBorder(Color.black));
        
        this.man = man;
        
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
    }

    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);   
        
        if(mode == 1) {
        	paintSpectroGram(g);
        	
        	if(this.boxZoom == 2) {
        		//System.out.println("" + bX1 + " " + bY1 + " " + bX2 + " " + bY2);
        		g.setColor(Color.WHITE);
        		g.drawLine(bX1, bY1, bX1, bY2);
        		g.drawLine(bX1, bY1, bX2, bY1);
        		g.drawLine(bX1, bY2, bX2, bY2);
        		g.drawLine(bX2, bY1, bX2, bY2);
        	}
        	else {
        		this.drawCursor(g);
        	}
        }
    }
    private void paintSpectroGram(Graphics g) {
		if(spec == null) { return; } //nothing to paint
		
		g.setColor(Color.BLACK);
		g.fillRect(startX, startY, width, height);
		g.setColor(Color.RED);
		
		//int n = spec.data.get(0).length; //length of one spectrum buffer (windowsize)
		//int n = specTo - specFrom;
		int windowCount = windowTo - windowFrom;
				
		//double valuesPerPix = (float) n / (float) height;
		double pixPerWindow = (float) width / windowCount;
		
		double maxValue = -1.0;
		
		double logscale = Math.log(freqRange) / Math.log(2.0);
		double res = (double) spec.data.get(0).length / 22050.0;  //frequency resolution of fourier buffer
		
		//determine scale:
		for(int i = windowFrom; i < windowTo; i++) {
			for(int k = 1; k < height; k++) {
				int from = (int) ( (Math.pow(2.0, (double) (k-1.0) * logscale / (double) height) + freqFrom) * res);
				int to = (int) ( (Math.pow(2.0, (double) k * logscale / (double) height) + freqFrom) * res);
				//System.out.println(from + " --> " + to);
				double strength = 0.0;
				for(int s = from; s < to; s++) {
					strength += spec.data.get(i)[s];
				}
				double x = strength / (double) (to-from);
				
				if(x > maxValue) { maxValue = x; }
			}
		}
		spec.scale = pixPerWindow / maxValue;
		//System.out.println(freqFrom + " " + freqTo);
		
		for(int i = windowFrom; i < windowTo; i++) {	
			int xOffset = (int) (startX + (i-windowFrom)*pixPerWindow);
			g.setColor(Color.YELLOW);
			g.drawLine(xOffset, height - startY, xOffset, startY);
			for(int k = 1; k < height; k++) {
				int from = (int) ( (Math.pow(2.0, (double) (k-1.0) * logscale / (double) height) + freqFrom) * res);
				int to = (int) ( (Math.pow(2.0, (double) k * logscale / (double) height) + freqFrom) * res);
				//to = Math.min(to, n-1);
				double strength = 0.0;
				double filteredAmount = 0.0;
				for(int s = from; s < to; s++) {
					strength += spec.data.get(i)[s];
					if(spec.filtered.get(i)[s].booleanValue()) {
						filteredAmount += spec.data.get(i)[s];
						//System.out.println("filt");
					}
				}
				if(to-from == 0) { to++; }
				double x = strength / (double) (to-from);
				
				x *= spec.scale;
				
				if((int) x > 1) {				
					x += xOffset;
					float red = (float) (strength / (strength + filteredAmount));
					float blue = (float) (1.0 - red);
					float green = (float) ((x - xOffset) / (float) pixPerWindow) * (1.0f - blue);
					g.setColor(new Color(red, green, blue));
					g.drawLine(xOffset, height - (k+startY), (int) x, height - (k+startY));
				}
			}
		}
		
		paintFrequencyScale(g);
    }
    private void paintFrequencyScale(Graphics g) {
    	int[] values = {20, 50, 100, 500, 1000, 2000, 5000, 10000, 15000};
    	double logmax = Math.log(freqRange) / Math.log(2.0);
    	double scale = (double) height / logmax;
    	
    	g.setColor(Color.black);
    	
    	for(int v : values) {
    		if((v < (int) freqFrom) || (v > (int) freqTo)) { continue; }
    		double pos = Math.log((double) v - freqFrom) / Math.log(2.0);
    		pos *= scale;
    		pos = height - pos;
    		g.drawLine(80, (int) pos, 100, (int) pos);		
    		g.drawString(new Integer(v).toString(), 10, (int) pos);
    	}
    }

	public void drawSpectroGram(SpectrogramContainer contentSpectrogram) {
		this.spec = contentSpectrogram;
        this.resetZoom();
		mode = 1;
		this.repaint();
	}
	
	private void processMouse() {
		if(spec == null) { return; }
		if(this.boxZoom == 1) {
			bX1 = mouseX;
			bY1 = mouseY;
			boxZoom++; 
			return;
		}
		else if(this.boxZoom == 2) {
			bX2 = mouseX;
			bY2 = mouseY;
			this.repaint();
			return;
		}
		if(this.editMode >= 3) { return; }
		int wCount = windowTo - windowFrom;
		
		int wIndx = this.getWindowNumberFromXPos(mouseX);
		//System.out.println("Windx: " + wIndx);
		//int n = spec.data.get(0).length;
		
		double logscale = Math.log(freqRange) / Math.log(2.0);
		
		double freq = Math.pow(2.0, (double) (height-mouseY-cursorSize) * logscale / height) + freqFrom;
		double freq2 = Math.pow(2.0, (double) (height-mouseY+cursorSize) * logscale / height) + freqFrom;
		double res = (double) spec.data.get(0).length / 22050.0;  //frequency resolution of fourier buffer
		
		int from = (int) (res*freq);
		int to = (int) (res * freq2);
		
		//System.out.println(wIndx + " / " + freq);
		
		//remove frequencies from the spectrogram:
		
		//System.out.println(wIndx + " erasing " + from + " --> " + to);
		
		int bigN = spec.complexData.get(0).length;
		
		for(int i = from; i < to; i++) {
			//spec.data.get(wIndx)[i] = (float) 0.0;
			if(editMode == 0) { //filter
				spec.filtered.get(wIndx)[i] = new Boolean(true);
				spec.complexData.get(wIndx)[i] = new Complex(0.0, 0.0);
				spec.complexData.get(wIndx)[bigN-i-1] = new Complex(0.0, 0.0);
			}
			else if(editMode == 1) { //unfilter
				spec.filtered.get(wIndx)[i] = new Boolean(false);
				spec.complexData.get(wIndx)[i] = spec.complexDataBackup.get(wIndx)[i];
				spec.complexData.get(wIndx)[bigN-i-1] = spec.complexDataBackup.get(wIndx)[bigN-i-1];
			}
			else if(editMode == 2) { //paint
				double pixPerWindow = width / (windowTo - windowFrom);
				int xFrom = (int) (startX + (wIndx-windowFrom)*pixPerWindow);
				//int posX = (int) ((mouseX-startX) - (wIndx-windowFrom) * pixPerWindow);
				int posX = mouseX - xFrom;
				double freqStrength = posX / spec.scale;
				
				spec.complexData.get(wIndx)[i] = new Complex(freqStrength, 0.0);
				spec.complexDataBackup.get(wIndx)[i] = new Complex(freqStrength, 0.0);
				spec.filtered.get(wIndx)[i] = new Boolean(false);
				spec.data.get(wIndx)[i] = (float) freqStrength;
			}
		}	
		this.repaint();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		mouseX = arg0.getX();
		mouseY = arg0.getY();
		
		processMouse();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		mouseX = arg0.getX();
		mouseY = arg0.getY();
		
		this.repaint();
	}
		
	@Override
	public void mouseClicked(MouseEvent arg0) {
		if(this.editMode < 3) { return; }
		
		int wIndx = this.getWindowNumberFromXPos(mouseX);
		
		if(this.editMode == 3) { //playback a window chunk
			int begin = wIndx * spec.windowSize;
			int end = begin + spec.windowSize;
			man.playContent(begin, end);
			return;
		}
		else if(this.editMode == 4) { //playback filtered part of current window
			Complex[] cData = new Complex[spec.complexData.get(0).length];
			double logscale = Math.log(freqRange) / Math.log(2.0);
			double freq = Math.pow(2.0, (double) (height-mouseY-cursorSize) * logscale / height) + freqFrom;
			double freq2 = Math.pow(2.0, (double) (height-mouseY+cursorSize) * logscale / height) + freqFrom;
			double res = (double) spec.data.get(0).length / 22050.0;  //frequency resolution of fourier buffer
			int from = (int) (res*freq);
			int to = (int) (res * freq2);
			System.out.println("listen!");
			for(int i = 0; i < from; i++) {
				cData[i] = new Complex(0.0, 0.0);
			}
			for(int i = from; i < to; i++) {
				cData[i] = spec.complexData.get(wIndx)[i];
			}
			for(int i = to; i < cData.length; i++) {
				cData[i] = new Complex(0.0, 0.0);
			}
			float[] clip = FFT.reconstructSignal(cData);
			Spectrogram.applyAmplitudeModulation(clip, spec.windowSize, 1);
			man.playClip(clip);
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
				
	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		processMouse();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if(boxZoom == 2) { processBoxZoom(); boxZoom = 0; } 
	}  
		
	private void processBoxZoom() {
		//System.out.println("processing" + bX1 + " " + bY1 + " " + bX2 + " " + bY2);
		int y1 = Math.max(bY1, bY2);
		int y2 = Math.min(bY1, bY2);
		int x1 = Math.min(bX1, bX2);
		int x2 = Math.max(bX1, bX2);
		int wCount = windowTo - windowFrom;
		int wFrom = windowFrom;
		windowFrom = (int) ((double) (x1-startX) / (width / (double) wCount)) + wFrom;
		windowTo = (int) ((double) (x2-startX) / (width / (double) wCount)) + wFrom + 1;
		double logscale =  Math.log(freqRange) / Math.log(2.0); 
		double res = (double) (specTo - specFrom) / (double) freqRange;
		double ff = freqFrom;
		//double ft = freqTo;
		freqFrom = Math.pow(2.0, (double) (height-y1) * logscale / height) + ff;
		freqTo = Math.pow(2.0, (double) (height-y2) * logscale / height) + ff;
		freqRange = freqTo - freqFrom;
		specFrom = (int) (res*freqFrom);
		specTo = (int) (res * freqTo);
		
		//System.out.println(windowFrom + " -- " + windowTo);
		
		if(windowTo == windowFrom) { windowTo++; }
		
		this.repaint();
	}
	
	public void resetZoom() {
		windowFrom = 0;
		windowTo = spec.windowCount;
		specFrom = 0;
		specTo = spec.data.get(0).length;
		freqFrom = 0.0;
		freqTo = 22050.0;
		freqRange = freqTo - freqFrom;
	}
	public void beginBoxZoom() {
		boxZoom = 1;
	}
	public void setCursorSize(int cursorSize) {
		this.cursorSize = cursorSize;
	}
	
	private void drawCursor(Graphics g) {
		int wIndx = this.getWindowNumberFromXPos(mouseX);
		double pixPerWindow = (float) width / (windowTo - windowFrom);
		int xFrom = (int) (startX + (wIndx-windowFrom)*pixPerWindow);
		int xTo = (int) (startX + (1+wIndx-windowFrom) * pixPerWindow);
		int upperY = mouseY - this.cursorSize;
		int lowerY = mouseY + this.cursorSize;
		
		g.setColor(Color.WHITE);
		g.drawLine(xFrom, upperY, xTo, upperY);
		g.drawLine(xFrom, lowerY, xTo, lowerY);
	}
	
	private int getWindowNumberFromXPos(int x) {
		return (int) ((double) (x-startX) / (width / (double) (windowTo - windowFrom))) + windowFrom;
	}
	
	public void invertFilter() {
		if(spec == null) { return; }
		
		int bigN = spec.complexData.get(0).length;
		int n = spec.data.get(0).length;
		
		for(int k = 0; k < spec.windowCount; k++) {
			for(int i = 0; i < n; i++) {
				//spec.complexData.get(k)[i]
				if(spec.filtered.get(k)[i].booleanValue()) {
					spec.filtered.get(k)[i] = new Boolean(false);
					spec.complexData.get(k)[i] = spec.complexDataBackup.get(k)[i];
					spec.complexData.get(k)[bigN-i-1] = spec.complexDataBackup.get(k)[bigN-i-1];
				}
				else {
					spec.filtered.get(k)[i] = new Boolean(true);
					spec.complexData.get(k)[i] = new Complex(0.0, 0.0);
					spec.complexData.get(k)[bigN-i-1] = new Complex(0.0, 0.0);
				}
			}
		}
		this.repaint();
	}
}
