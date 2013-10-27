package control;

import sound.SoundGUI;

import view.SampleManipulator;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//ExampleHost host = new ExampleHost();
		
		SoundGUI asio = new SoundGUI();
		
		/*float[] sinewave = SignalGenerator.oscillatorSine(1024, 440.0, 1.0);
		float[] noise = SignalGenerator.oscillatorNoise(1024, 1.0);
		float[] sinespec = FFT.computeSpectrum(sinewave);
		float[] noisespec = FFT.computeSpectrum(noise);
		
		System.out.println("start spectrum of sine");
		for(int i = 0; i < 1024; i++) {
			if(sinespec[i] > 0.0) { System.out.println("nonzero value at" + i); }
			//System.out.print(sinespec[i]);
			//System.out.print(" ");
			//if(i % 16 == 0) { System.out.print("\n"); }
		}
		System.out.println("end spectrum of sine");*/
		SoundManager sman = new SoundManager(asio);
		
		new SampleManipulator(sman);
	}

}
