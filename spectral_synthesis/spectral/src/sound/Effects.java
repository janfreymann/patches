package sound;

import java.util.LinkedList;
import java.util.Iterator;

public class Effects {
	public static void declicker(float[] signal) {
		double t = 0.2;
		int n = signal.length;
		
		/*LinkedList<Integer> clicks = new LinkedList<Integer>(); //stores positions of clicks
		LinkedList<Float> clickFixes = new LinkedList<Float>(); //desired shifts of clicks
		
		for(int i = 1; i < n; i++) {
			if(Math.abs(signal[i] - signal[i-1]) > t) {  //click detected
				clicks.add(i);
				Float x = new Float(Math.abs(signal[i] - signal[i-1]) - t);
				if(signal[i] > signal[i-1]) {
					clickFixes.add(-x);
				} else { clickFixes.add(x); }
			}
		}
		Iterator it = clicks.iterator();
		Iterator it2 = clickFixes.iterator();
		
		int lastPos = 0;
		float lastShift = 0;
		
		while(it.hasNext()) {
			int pos = ((Integer) it.next()).intValue();
			float shift = ((Float) it2.next()).floatValue();	
		}*/
		
		//analog-inspired solution:
		
		for(int i = 1; i < n; i++) {
			if(Math.abs(signal[i] - signal[i-1]) > t) {  //click detected
				//fade-out, fade-in:
				//bad: too many plops
				int a = Math.max(0, i-50);
				int b = Math.min(n,  i+50);
				float att = (float) (1.0 / (float) (i-a));
				for(int k = a; k <= i; k++) { //mini fade-out
					signal[k] *= 1-att*(k-a);
				}
				att = (float) (1.0 / (float) (b - i));
				for(int k = i+1; k < b; k++) {
					signal[k] *= att*(k-i-1);
				}	
			}
		}
	}
}
