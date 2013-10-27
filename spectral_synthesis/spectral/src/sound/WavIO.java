package sound;

import java.io.*;

import wav.*;

public class WavIO {

	public static void storeWaveFile(File f, float[] content) {
		try {
			// Calculate the number of frames required for specified duration
			int numFrames = content.length;

			// Create a wav file with the name specified as the first argument
			WavFile wavFile = WavFile.newWavFile(f, 1, numFrames, 16, 44100);
						
			//int[] buffer = new int[1024];
			int[] buffer = new int[numFrames];
			
			/*int frameCounter = 0;
			
			//System.out.println("writing " + numFrames + " frames");
			
			while(frameCounter < numFrames) {
				int bufferedFrames = 0;
				for(int i = 0; i < 1024 && frameCounter < numFrames; i++) {
					buffer[i] = (int) (content[frameCounter] * 32000);
					frameCounter++;
					bufferedFrames++;
				}
				//System.out.println("write frames until " + frameCounter);
				wavFile.writeFrames(buffer, bufferedFrames);
				//System.out.println("remaining: " + wavFile.getFramesRemaining());
			}*/
			
			for(int i = 0; i < numFrames; i++) {
				buffer[i] = (int) (content[i] * 32000);
			}
			wavFile.writeFrames(buffer, numFrames);
			wavFile.close();
		}
		catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		} catch (WavFileException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}

	public static float[] readWaveFile(File f) {
		try {
			WavFile wf = WavFile.openWavFile(f);


			int numChannels = wf.getNumChannels();
			int size = (int) wf.getFramesRemaining();  //small files only
			double[][] buffer = new double[numChannels][size];  //ignore second channel for now

			wf.readFrames(buffer, size);

			//pad with zeros, s. t. the size of the sample is 2^something (for FFT!)

			double logSize = Math.log(size) / Math.log(2.0);
			int desiredSize = (int) Math.pow(2.0, Math.floor(logSize) + 1);
			int desiredSize2 = (int) Math.pow(2.0, Math.floor(logSize));
			if(desiredSize2 == size) { desiredSize = desiredSize2; }
			System.out.println("desired size: " + desiredSize);

			float[] samples = new float[desiredSize];

			//System.out.println("old size: " + size + "  new size:" + desiredSize);

			for(int i = 0; i < desiredSize; i++) {
				if(i >= size) {
					samples[i] = (float) 0.0;
				}
				else { //pad with zeros
					samples[i] = (float) buffer[0][i];
					//System.out.println(samples[i]);
				}
			}
			wf.close();
			return samples;

		} catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		} catch (WavFileException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}

		return null;
	}
}
