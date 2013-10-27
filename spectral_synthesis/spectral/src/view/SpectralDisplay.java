package view;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import sound.SoundGUI;
import sound.SpectrogramContainer;

import control.SoundManager;

public class SpectralDisplay {
	/*private SoundGUI asio;
	public SpectralDisplay(SoundGUI asio) {
		this.asio = asio;
	}*/
	public void start() {
		try {
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		//init OPENGL here
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, 800, 0, 600, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		/*SoundManager man = new SoundManager(asio);
		
		boolean playContent = false;
		boolean showFileDialog = false;
		boolean drawSpecGram = false;
				
		//Main Loop (all events go here):
		while(!Display.isCloseRequested()) {
			//render OpenGL here
			// Clear the screen and depth buffer
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			boolean drawSpec = Keyboard.isKeyDown(Keyboard.KEY_S);  // press s: draw spectrum (just for testing)
			boolean genWave = Keyboard.isKeyDown(Keyboard.KEY_W); //press w: generate sine wave (just for testing)
			boolean playPressed = Keyboard.isKeyDown(Keyboard.KEY_P);
			boolean reconstructSpecGram = Keyboard.isKeyDown(Keyboard.KEY_R);
			
			if(genWave) {
				//man.generateTestWave();
				drawAudioBuffer(man.getContent());
			}
			if(drawSpec) {
				man.computeContentSpectrum();
				drawSpectrum(man.getContentSpec());
			}
			if(reconstructSpecGram) {
				man.reconstructFromSpectrogram();
				drawAudioBuffer(man.getContent());
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_G)) {
				if(!drawSpecGram) {
					System.out.println("drawing...");
					man.computeContentSpectrogram(16384);
					System.out.println("done");
				}
				drawSpecGram = true;
				drawSpectrogram(man.getContentSpectrogram());
			}
			else { drawSpecGram = false; }
			if(playPressed) {
				if(playContent == false) {
					man.playContent();
					playContent = true;
				}
			}
			else { playContent = false; man.stopContent(); }
			
			if(Keyboard.isKeyDown(Keyboard.KEY_O)) {
				if(!showFileDialog) {
					showFileDialog = true;
					man.importWaveFile(asio.openWaveFile());
					
				}
			}
			else { showFileDialog = false; }
			
			Display.update();
		}
		Display.destroy();*/
	}
	public void destroy() {
		Display.destroy();
	}
	public void update() {
		Display.update();
	}
	public void drawAudioBuffer(float[] buffer) {
		if(buffer == null) { return; }		
		
		//dimensions of waveform:
		int width = 600;
		int height = 400;
		int startX = 80;
		int midY = 200;
		int n = buffer.length;
		
		GL11.glColor3f(0.5f,0.5f,1.0f);
		
		double samplesPerPix = (float) n / (float) width;
		
		for(int x = startX; x < startX + width; x++) {
			double strength = 0;
			double bias = 0;
			int from = (int) (samplesPerPix * (x - startX));
			int to = (int) (samplesPerPix * (x+1 - startX));
			for(int s = from; s < to && s < n; s++) {
				strength += Math.abs(buffer[s]);
				bias += buffer[s];
			}
			bias /= (float) (to-from);
			double y = strength / (float) (to-from);
			y *= bias;
			y *= (height/2);
			y += midY;
			
			GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex2i(x, midY);
			GL11.glVertex2i(x, (int) y);
			GL11.glEnd();	
		}
	}
	public void drawSpectrogram(SpectrogramContainer spec) {
		if(spec == null) { return; }
		int width = 600;
		int height = 600;
		int startX = 80;
		int startY = 80;
		int n = spec.data.get(0).length; //length of one spectrum buffer (windowsize) 
		int windowCount = spec.data.size();
				
		//double valuesPerPix = (float) n / (float) height;
		double pixPerWindow = (float) width / windowCount;
		
		double maxValue = -1.0;
		
		double logscale = Math.log(n) / Math.log(2);
		
		
		//determine scale:
		for(int i = 0; i < windowCount; i++) {
			for(int k = 1; k < height; k++) {
				//int from = (int) ( Math.pow(2, k-1) * logscale);
				//int to = (int) ( Math.pow(2, k) * logscale);
				int from = (int) (Math.pow(2.0, ((double) (k-1) / height) * logscale) / 2);
				int to = (int) (Math.pow(2.0, ((double) k / height) * logscale) / 2);
				//System.out.println(from + " ->" + to);
				double strength = 0.0;
				for(int s = from; s < to; s++) {
					strength += spec.data.get(i)[s];
				}
				double x = strength / (double) (to-from);
				
				if(x > maxValue) { maxValue = x; }
			}
		}
		spec.scale = pixPerWindow / maxValue;
		
		for(int i = 0; i < windowCount; i++) {
			int xOffset = (int) (startX + i*pixPerWindow);
			GL11.glColor3f(1.0f,0.2f,0.2f);
			GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex2i(xOffset, startY+height);
			GL11.glVertex2i(xOffset, startY);
			GL11.glEnd();
			for(int k = 1; k < height; k++) {
				int from = (int) (Math.pow(2.0, ((double) (k-1) / height) * logscale) / 2);
				int to = (int) (Math.pow(2.0, ((double) k / height) * logscale) / 2);
				double strength = 0.0;
				for(int s = from; s < to; s++) {
					strength += spec.data.get(i)[s];
				}
				if(to-from == 0) { to++; }
				double x = strength / (double) (to-from);
				x *= spec.scale;
				
				//if(x > 0.0) { System.out.println(x); }
				
				x += xOffset;
				
				//System.out.println(xOffset + " --> " + x);
				
				GL11.glColor3f(0.5f,0.5f,1.0f);
				GL11.glBegin(GL11.GL_LINES);
				GL11.glVertex2i(xOffset, k+startY);
				GL11.glVertex2i((int) x, k+startY);
				GL11.glEnd();
			}
		}
	}
	
	public void drawSpectrum(Float[] buffer) {  //similar, but different from drawAudioBuffer
		if(buffer == null) { return; }
		//dimensions of the spectrum:
		int width = 600;
		int height = 200;
		int startX = 80;
		int startY = 100;
		int n = buffer.length;
		
		GL11.glColor3f(0.5f,0.5f,1.0f);
		
		double valuesPerPix = (float) n / (float) width;
		
		//find max peak value:
		
		double max = 0.0;
		for(int x = startX; x < startX + width; x++) {
			double strength = 0;
			int from = (int) (valuesPerPix * (x - startX));
			int to = (int) (valuesPerPix * (x+1 - startX));
			for(int s = from; s < to && s < n; s++) {
				strength += buffer[s]*buffer[s];  //note: power spectrum!
			}
			double y = strength / (float) (to-from);
			if(y > max) { max = y; }
		}
		double scale = 1.0 / max;
		//System.out.println("scale: " + scale + " max: " + max);
		
		for(int x = startX; x < startX + width; x++) {
			double strength = 0;
			int from = (int) (valuesPerPix * (x - startX));
			int to = (int) (valuesPerPix * (x+1 - startX));
			for(int s = from; s < to && s < n; s++) {
				strength += buffer[s]*buffer[s];  //note: power spectrum!
			}
			double y = strength / (float) (to-from);
			y *= scale;
			y *= (height);
			y += startY;
			
			GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex2i(x, startY);
			GL11.glVertex2i(x, (int) y);
			GL11.glEnd();	
		}
	}
}
