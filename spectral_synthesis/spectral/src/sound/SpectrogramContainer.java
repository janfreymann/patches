package sound;

import java.util.ArrayList;

public class SpectrogramContainer {
	public ArrayList<Float[]> data;
	public ArrayList<Complex[]> complexData;
	public ArrayList<Complex[]> complexDataBackup;
	public double scale;
	public int signalLength;
	public int windowCount;
	public int windowSize;
	
	//used to make container modifiable:
	
	public ArrayList<Boolean[]> filtered;
	
	public SpectrogramContainer(int windowCount, int signalLength) {
		data = new ArrayList<Float[]>(windowCount);
		filtered = new ArrayList<Boolean[]>(windowCount);
		complexData = new ArrayList<Complex[]>(windowCount);
		complexDataBackup = new ArrayList<Complex[]>(windowCount);
		this.signalLength = signalLength;
		this.windowCount = windowCount;
		this.windowSize = signalLength / windowCount; //number of windows should divide signal length
	}

	public void add(Complex[] cSpectrum) {
		complexData.add(cSpectrum);
		Complex[] backup = new Complex[cSpectrum.length];
		for(int i = 0; i < cSpectrum.length; i++) {
			backup[i] = new Complex(cSpectrum[i]);
		}
		complexDataBackup.add(backup);
		Float[] magnitudes = new Float[cSpectrum.length / 2];  //use symmetry!
		Boolean[] filt = new Boolean[cSpectrum.length / 2];
		for(int i = 0; i < magnitudes.length; i++) {
			magnitudes[i] = (float) cSpectrum[i].getMagnitude();
			filt[i] = new Boolean(false);
		}
		data.add(magnitudes);
		filtered.add(filt);
	}
}
