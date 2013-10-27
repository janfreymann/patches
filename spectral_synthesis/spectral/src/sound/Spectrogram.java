package sound;

import java.util.ArrayList;

public class Spectrogram {
	
	private static double[] stencil;
	private static double[] amplitudeMod = null; //used for reconstruction
	private static int lastWindowSize = -1;
	private static int Rec_lastWindowCount = -1;
	private static int Rec_lastWindowSize = -1;
	
	public static void applyAmplitudeModulation(float[] buffer, int windowSize, int windowCount) {
		int n = windowSize*windowCount;
		if((Rec_lastWindowCount != windowCount) || (Rec_lastWindowSize != windowSize)) {
			amplitudeMod = new double[n];
			if(lastWindowSize != windowSize/2) {
				createStencil(windowSize/2);
				lastWindowSize = windowSize/2;
			}
			//compute amplitude modulation by applying Gabor Window to brickwall signal of height 1
			for(int i = 0; i < windowCount; i++) {
				//float[] windowed = applyGaborWindow(windowSize / 2, i*windowSize + windowSize / 2, );
				for(int j = 0; j < windowSize*2; j++) {
					int k = j + (i * windowSize) - windowSize;
					if(k < 0) { continue; }
					if(k >= n) { break; }
					amplitudeMod[k] += stencil[j];
					//if(windowed[j] < 0.0) { System.out.println("negative windowing?"); }
				}
			}
			Rec_lastWindowCount = windowCount;
			Rec_lastWindowSize = windowSize;
		}
		
		for(int i = 0; i < n; i++) {
			buffer[i] /= amplitudeMod[i];
		}
	}
	
	public static float[] applyGaborWindow(int w, int pos, float[] buffer) {
		//w: half the window size
		//mirror the signal at the ends
		int n = buffer.length;
		float[] windowed = new float[4*w];
		
		
		if(w != lastWindowSize) { 
			createStencil(w); 
		}
		
		for(int i = pos-2*w; i < pos+2*w; i++) {
			//int x = i % n; //extend signal peridodically
			//mirror boundaries:
			//if(x < 0) { x = -i; }
			//else if(x >= n) { x = n-(i-n); }
			
			//avoid out-of-bounds exception
			if(i < 0) { continue; }
			if(i >= n) { break; }
			
			//use sampled gaussian as stencil (sufficient?)
			
			//windowed[i] = (float) (Gaussian.phi((double) i, (double) pos, (double) w))*buffer[i];
			
			windowed[i+2*w-pos] = (float) stencil[i+2*w-pos] * buffer[i];
			
			//System.out.println(Gaussian.phi((double) i, (double) pos, (double) w));
		}
		return windowed;
	}
	/*
	 * compute spectrogram for given buffer and window size
	 */
	public static SpectrogramContainer computeSpectrogram(int w, float[] buffer) {
		int n = buffer.length;
		int iterations = n / (2*w);
		SpectrogramContainer spec = new SpectrogramContainer(iterations, buffer.length);
		for(int i = 0; i < iterations; i++) {
			int windowPos = 2*w*i + w;
			float[] wSignal = applyGaborWindow(w, windowPos, buffer);
			Complex[] cSpectrum = FFT.computeComplexSpectrumOnRealInput(wSignal);
			spec.add(cSpectrum);
		}
		return spec;
	}
	
	/*
	 * reconstruct signal from given spectrogram
	 */
	public static float[] reconstructSignal(SpectrogramContainer spec) {
		float[] signal = new float[spec.signalLength];
		//for(int i = 0; i < spec.signalLength; i++) { signal[i] = (float) 0.0; }
		for(int i = 0; i < spec.windowCount; i++) {
			float[] partialSignal = FFT.reconstructSignal(spec.complexData.get(i));
			for(int j = 0; j < spec.windowSize*2; j++) {
				int k = j + (i * spec.windowSize) - spec.windowSize;
				if(k < 0 ) { continue; }
				if(k >= spec.signalLength) { break; }
				
				signal[k] += partialSignal[j];
			}
		}
		applyAmplitudeModulation(signal, spec.windowSize, spec.windowCount);
		//TODO proper declicker (or get rid of the clicks otherwise)
		//Effects.declicker(signal);
		return signal;
	}
	
	/*
	 * produce a stencil for given window size
	 * w represents half the window size (~window radius)
	 */
	private static void createStencil(int w) {
		lastWindowSize = w;
		stencil = new double[4*w];
		for(int i = 0; i < 4*w; i++) {
			double x = (double) i / (double) w - 2.0;
			stencil[i] = (double) Gaussian.phi(x);
		}
	}
	
	public static SpectrogramContainer intersect(SpectrogramContainer specA, SpectrogramContainer specB) {
		if(specA.signalLength != specB.signalLength) { System.out.println("specs should have the same length"); return null; }
		if(specA.windowSize != specB.windowSize) {  System.out.println("specs should have the same window size"); return null; }
		SpectrogramContainer spec = new SpectrogramContainer(specA.windowCount, specA.signalLength);
		
		for(int k = 0; k < specA.windowCount; k++) {
			//intersect complex data:
			Complex[] cData = new Complex[specA.complexData.get(0).length];
			for(int i = 0; i < specA.complexData.get(0).length; i++) {
				Complex d = new Complex();
				double reA = specA.complexData.get(k)[i].getRe();
				double reB = specB.complexData.get(k)[i].getRe();
				//d.setRe(Math.min(reA, reB));
				if(reB > 0.1) {
					d.setRe(reA);
				}
				else {
					d.setRe(0.0);
				}
				d.setIm(specA.complexData.get(k)[i].getIm());
				cData[i] = d;
			}
			spec.add(cData);
		}
		return spec;
	}
}
