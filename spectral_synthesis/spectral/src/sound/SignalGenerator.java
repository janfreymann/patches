package sound;

import java.util.Random;

public class SignalGenerator {
	
	private static double sampleRate = 44100;
	/*
	 * generate Sine-Wave of frequency freq in block of n samples with amplitude a
	 */
	public static float[] oscillatorSine(int n, double freq, double a) {
		float[] output = new float[n];
		
		//TODO: initial phase
		
		for(int i = 0; i < n; i++) {
			output[i] = (float) (Math.sin(2 * Math.PI * i * freq / sampleRate) * a);
		}
		return output;
	}
	/*
	 * generate white noise of amplitude a
	 */
	public static float[] oscillatorNoise(int n, double a) {
		float[] output = new float[n];
		
		Random gen = new Random();
		
		for(int i = 0; i < n; i++) {
			output[i] = (float) (gen.nextFloat()*2.0 - 1.0); // between 0 and 1.0
		}		
		return output;
	}
	public static float[] oscillatorSineAndNoise(int n, double freq, double a) {
		float[] output = new float[n];
		
		//TODO: initial phase
		
		Random gen = new Random();
		
		for(int i = 0; i < n; i++) {
			output[i] = (float) (Math.sin(2 * Math.PI * i * freq / sampleRate) * a);
			if(gen.nextBoolean() == true) {
				output[i] += gen.nextFloat() / 10; // 10 percent noise
			}
			else {
				output[i] -= gen.nextFloat() / 10; // 10 percent noise
			}
		}
		return output;
	}
	public static float[] oscillatorSineSweep(int n, double freqA, double freqB, double a) {
		double freqshift = (freqB - freqA) / (double) n;
		float[] output = new float[n];
		
		//TODO: initial phase
		
		double freq = freqA;
		
		for(int i = 0; i < n; i++) {
			output[i] = (float) (Math.sin(2 * Math.PI * i * freq / sampleRate) * a);
			freq += freqshift;
		}
		return output;
	}
	public static float[] makeSilence(int n) {
		float[] output = new float[n];
		for(int i = 0; i < n; i++) {
			output[i] = 0.0f;
		}
		return output;
	}
}
