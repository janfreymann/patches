package sound;

import java.util.Arrays;
import java.util.Iterator;

public class FFT {
	
	public static Float[] computeSpectrum(float[] buffer) {		
		//note: because of symmetry, I just need the first half of the values
		int n = buffer.length;
		Complex[] f = computeComplexSpectrumOnRealInput(buffer);
		Float[] spectrum = new Float[n/2];  
		
		for(int i = 0; i < n/2; i++) {
			spectrum[i] = new Float(f[i].getMagnitude());
		}
		
		return spectrum;
	}
	
	public static Complex[] computeComplexSpectrumOnRealInput(float[] buffer) {
		//assume: buffer has a size of 2^n for some n
		int n = buffer.length;
		Complex[] f = new Complex[n];
		
		for(int i = 0; i < n; i++) {
			f[i] = new Complex(buffer[i], 0);
		}
		
		fft(0, n, f);
		
		return f;
	}
	public static float[] reconstructSignal(Complex[] f) {
		int n = f.length;
		Complex[] fcopy = new Complex[n];
		//deep copy:
		for(int i = 0; i < n; i++) {
			fcopy[i] = f[i];
		}
		irfft(n, fcopy);
		float[] signal = new float[n];
		
		for(int i = 0; i < n; i++) {
			signal[i] = (float) fcopy[n-i-1].getRe() / (float) n;        //divide by n to normalize?, reverse order
		}
		
		return signal;
	}
	
	public static void irfft(int n, Complex[] f) {
		for(Complex c : f) {
			c = c.getConjugate();
		}
		
		fft(0, n, f);
	}
	
	private static void rfft(int n, Complex[] f) { //perform FFT, but use symmetry property for real inputs
		if(n <= 1) { return; }
		
		//System.out.println("performing real FFT of size " + n);
		
		Complex[] g = new Complex[n/2];
		
		int j = 0;
		for(int i = 0; i < n; i+=2, j++) {
			g[j] = f[i];
		}
		
		fft(0, n/2, g);
		
		for(int i = 0; i < n/2; i++) {
			f[i] = g[i];
		}
		for(int i = n/2; i < n; i++) {
			f[i] = g[n-i-1].getConjugate();
		}
	}

	
	private static void fft(int m, int n, Complex[] f) {
		if(n <= 1) { return; }

		Complex[] g = new Complex[n/2];
		Complex[] u = new Complex[n/2];

		int j = 0;
		for(int i = 0; i < n; i+=2, j++) {
			g[j] = f[i];
			u[j] = f[i+1];
		}

		//conquer:
		fft(m, n/2, g);
		fft(m+n/2, n/2, u);

		//combine:
		for (int i = 0; i < n/2; i++) {
			Complex t = new Complex();
			t = t.ffthelper(n, i, u[i]);
			f[i] = t.add(g[i]);
			f[i + n/2] = g[i].sub(t);
		}
	}

	
	//in-place?
	/*private static void fft(int m, int n, Complex[] f) {
		if(n <= 1) { return; }
		
		//Complex[] g = new Complex[n/2];
		//Complex[] u = new Complex[n/2];
		
		int j = m;
		for(int i = m; i < m+n; i+=2, j++) {
			//g[j] = f[i];
			//u[j] = f[i+1];
			f[j] = f[i];
			f[j+n/2] = f[i+1];
		}
		
		//conquer:
		fft(m, n/2, f);
		fft(m+n/2, n/2, f);
		
		//combine:
		for (int i = m; i < m+n/2; i++) {
			Complex t = new Complex();
			//t = t.ffthelper(n, i, u[i]);
			t = t.ffthelper(n, i-m, f[i+n/2]);
			//System.out.println(t.toString());
			Complex tmp = new Complex(f[i]);
			f[i] = t.add(f[i]);
			f[i + n/2] = tmp.sub(t);
		}
	}*/
}
