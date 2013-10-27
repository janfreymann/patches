package control;

import java.io.File;

import sound.Complex;
import sound.SignalGenerator;
import sound.FFT;
import sound.SoundGUI;
import sound.Spectrogram;
import sound.SpectrogramContainer;
import sound.WavIO;

/*
 * this class is called from the MainLoop of SpectralDisplay.java to generate sounds & spectrums
 */
public class SoundManager {
	private float[] content; //current sound sample
	private Float[] contentSpec; //current spectrum
	private SpectrogramContainer specGram;
	private float[] noSound = {0.0f, 0.0f, 0.0f, 0.0f};
	
	private SoundGUI asio;
	
	public SoundManager(SoundGUI asio) {
		this.asio = asio;
	}
	
	public void generateTestWave() {
		//content = SignalGenerator.oscillatorSine(131072, 440.0, 1.0);
		
		content = SignalGenerator.oscillatorSineSweep(131072, 440.0, 880.0, 1.0);
		
		//Complex[] fourier = FFT.computeComplexSpectrum(content);
		
		//content = FFT.reconstructSignal(fourier);
		
		//float[] content2 = SignalGenerator.oscillatorSine(131072, 220.0, 0.6);
		//superimpose(content2);
		
		//float[] nonwindow = SignalGenerator.oscillatorNoise(131072, 4.0);
		//content = Spectrogram.applyGaborWindow(10000, 40000, nonwindow);
		//content = nonwindow;
		//content = SignalGenerator.oscillatorNoise(65536, 1.0);
		//content = SignalGenerator.oscillatorSineAndNoise(65536, 440.0, 1.0);
	}
	public void computeContentSpectrum() {
		if(content == null) { return; }
		contentSpec = FFT.computeSpectrum(content);
	}
	public void computeContentSpectrogram(int w) {
		if(content == null) { return; }
		specGram = Spectrogram.computeSpectrogram(w, content);
	}
	
	public void playContent() {
		System.out.println("play!");
		asio.setPlaybackBuffer(content);
	}
	public void playContent(int begin, int end) {
		//if(begin >= end) { return; }
		float[] partialBuffer = new float[end-begin];
		for(int i = begin; i < end; i++) {
			partialBuffer[i-begin] = content[i];
		}
		asio.setPlaybackBuffer(partialBuffer);
	}
	public void stopContent() {
		asio.setPlaybackBuffer(noSound);
	}

	public SpectrogramContainer getContentSpectrogram() {
		return specGram;
	}
	public float[] getContent() {
		return content;
	}
	public Float[] getContentSpec() {
		return contentSpec;
	}
	public void setContentSpectrogram(SpectrogramContainer spec) {
		this.specGram = spec;
	}
	
	public void reconstructFromSpectrogram() {
		if(specGram == null) { return; }
		content = Spectrogram.reconstructSignal(specGram);
	}
	
	/*
	 * add content of 'buffer' (used to combine multiple waveforms etc.)
	 */
	private void superimpose(float[] buffer) {
		for(int i = 0; i < content.length; i++) {
			content[i] += buffer[i];
		}
	}
	public void playClip(float[] clip) {
		asio.setPlaybackBuffer(clip);
	}
	
	public void importWaveFile(File wav) {
		System.out.println("importing " + wav.getName());
		content = WavIO.readWaveFile(wav);
		//content = SignalGenerator.oscillatorSineSweep(131072, 440.0, 880.0, 1.0);
	}
	
	public void storeWaveFile(File wav) {
		System.out.println("exporting " + wav.getName());
		WavIO.storeWaveFile(wav, content);
	}

	public void generateNoise(int n) {
		content = SignalGenerator.oscillatorNoise(n, 1.0);
	}
	public void generateSilence(int n) {
		content = SignalGenerator.makeSilence(n);
	}
}
