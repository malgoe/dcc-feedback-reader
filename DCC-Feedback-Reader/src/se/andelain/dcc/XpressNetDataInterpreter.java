package se.andelain.dcc;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class XpressNetDataInterpreter implements Runnable {

	private static final int ACCESSORY_DECODER_INFORMATION_RESPONSE = 64;
	// private HashMap<Integer, Integer> decoderMsgs = new HashMap<Integer,
	// Integer>(); //Address, number of msgs
	private LinkedBlockingQueue<Object[]> feedbackInfo;
	private LinkedBlockingQueue<byte[]> queue;
	private boolean notStopped = true;
	private byte[] currentMsg;

	public XpressNetDataInterpreter(LinkedBlockingQueue<byte[]> queue, LinkedBlockingQueue<Object[]> feedbackInfo) {
		this.queue = queue;
		this.feedbackInfo = feedbackInfo;
	}

	@Override
	public void run() {
		while (notStopped) {
			try {
				// Wait for / get latest message:
				currentMsg = queue.take();

				// We do not use bytes 0 and 1 at the moment.
				int function = currentMsg[2] & 0xF0;

				//System.out.println("Function: " + function);

				// Calculate the xor error checking bit
				int xor = currentMsg[2];
				for (int i = 3; i < currentMsg.length - 1; i++) {
					xor = (currentMsg[i] ^ xor);
				}

				if (xor != currentMsg[currentMsg.length - 1]) {
					// Something is wrong with this message.
					errorMsgType(currentMsg);
				} else {
					switch (function) {
					case ACCESSORY_DECODER_INFORMATION_RESPONSE:
						accessoryDecoderInformationResponse(currentMsg);
						break;

					default:
						unknownMsgType(currentMsg);
						break;

					}
				}

				// Check if we've been interrupted
				if (Thread.currentThread().isInterrupted()) {
					notStopped = false;
				}

			} catch (InterruptedException e) {
				System.out.println("Interpreter was interrupted!");
			}
		}

	}

	private void accessoryDecoderInformationResponse(byte[] msg) {
		int databytes = currentMsg[2] & 0x0F;
		//System.out.println("** Accessory decoder information response / broadcast **");
		// System.out.println("Bytes with data: " + databytes);
		// 2 bytes per message - first address, then data. First data byte at position 3
		// in the array


		for (int i = 3; i < databytes + 3; i++) {
			int decoderAddr;
			int[] bitNum = new int[4];
			boolean[] bitStatus = new boolean[4];


			decoderAddr = msg[i] + 1;

			i++; // Go to the byte with input data

			// Determine if we got the upper or lower nibble (1 = upper, 0 = lower):
			int nibble = (msg[i] & 0x10) >>> 4;

			if (nibble == 1) { // Bits 5-8

				bitNum[0] = 5;
				bitNum[1] = 6;
				bitNum[2] = 7;
				bitNum[3] = 8;
			} else { // Bits 1-4

				bitNum[0] = 1;
				bitNum[1] = 2;
				bitNum[2] = 3;
				bitNum[3] = 4;
			}

			//Loop through 4 bit statuses
			for (int j = 0; j < 3; j++) {
				if(getBit(msg[i], j) == 1) {
					bitStatus[j] = true;
				} else {
					bitStatus[j] = false;
				}
			}
			
			//Put received data into feedbackinfo
			try {
				for(int j = 0; j < 3; j++) {
					feedbackInfo.put(new Object[]{decoderAddr,bitNum[j],bitStatus[j]});
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	public int getBit(byte data, int position) {
		return (data >> position) & 1;
	}

	private void unknownMsgType(byte[] msg) {
		System.out.println("UNKNOWN MSG TYPE. RAW DATA:");
		System.out.println("Data (hex): ");
		System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(msg));

		System.out.println("Data (bin): ");
		for (byte b : msg) {
			System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
		}
	}

	private void errorMsgType(byte[] msg) {
		System.out.println("ERRONEOUS MESSAGE:");

		int xor = currentMsg[2];
		for (int i = 3; i < currentMsg.length - 1; i++) {
			xor = (currentMsg[i] ^ xor);
		}

		System.out.println("Calculated xor byte: " + xor);
		System.out.println("Message xor byte: " + currentMsg[currentMsg.length - 1]);
		// Calculate X-Or-Byte

		System.out.println("Data (hex): ");
		System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(msg));

		System.out.println("Data (bin): ");
		for (byte b : msg) {
			System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
		}
	}

}

// System.out.println("Done reading");
// System.out.println("Count: "+count);
// System.out.println("Data (hex): ");

// for(int i = 0; i < count; i++) {
// compactData[i] = data[i];
// }
