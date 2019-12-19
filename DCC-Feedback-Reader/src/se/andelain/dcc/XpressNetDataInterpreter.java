package se.andelain.dcc;
import java.util.concurrent.LinkedBlockingQueue;

public class XpressNetDataInterpreter implements Runnable {

	private static final int ACCESSORY_DECODER_INFORMATION_RESPONSE = 64;
	private static final int ERRONEOUS_XOR = 400;
	private LinkedBlockingQueue<Object[]> feedbackInfoMsgs;
	private LinkedBlockingQueue<Object[]> incomingPkgQueue;

	private boolean notStopped = true;

	public XpressNetDataInterpreter(LinkedBlockingQueue<Object[]> incomingPkgQueue, LinkedBlockingQueue<Object[]> feedbackInfo) {
		this.incomingPkgQueue = incomingPkgQueue;
		this.feedbackInfoMsgs = feedbackInfo;
	}


	//run just gets the packages from the incomingPkgQueue and sends them on to getMsgs
	@Override
	public void run() {
        //System.out.println("Started interpreter.");
		Object[] inData;
		while (notStopped) {

            try {
                inData = incomingPkgQueue.take();

                //inData[0] = busName, indata[1] = messages
                getMsgs((String)inData[0],(byte[])inData[1]);

            } catch (InterruptedException e) {
                //System.out.print("Interpreter thread interrupted.");

                //Ending up here is normal on shutdown. Do nothing.
            }

            // Check if we've been interrupted
            if (Thread.currentThread().isInterrupted()) {
                notStopped = false;
            }

        }

	}

	//getMsgs reads the package and splits the messages if there are multiple. Then sends them on to be handled.

    /**
     * Takes busName and package of messages. Here we attempt to split the package into individual messages
     * and pass them on for handling. While we do check for the correct call bytes here to make sure that we do not
     * split the package incorrectly we do no other error checking. That is up to the handleMsg() method.
     *
     * @param busName Name of the bus that sourced the package
     * @param remaining The package containing one or more Xpressnet messages
     */
	private void getMsgs(String busName, byte[] remaining){
        byte[] msg; //We have not extracted any message. Yet!
        byte[] tempMsg;
        int msgSize;

        while(remaining.length >= 5){ //While there is anything left to extract: (a valid msg is always at least 5 bytes)
            //Check that the call bytes are where we expect them to.
            //Call bytes are 0xFF and 0xFD. When converted to integers we get -1 and -3.
            if(remaining[0] != -1 || remaining[1] != -3){
                //If the call bytes are not in the right place something is really f***ed up. Abort mission! (And discard the whole package.)
                System.out.println("XpressNetDataInterpreter got a package from bus: "+busName+" and the call bytes are not where we expect them to.");
                System.out.println("Where we expected -1 and -3 we got: "+remaining[0]+" and "+remaining[1]+".");
                System.out.println("Discarding package!");
            }

            //Check the header for the msg size. The header is in byte 3 and consists of two parts. The first 4 bits tell us what kind of msg this is.
            //The next 4 bits tells us the size of the msg excluding call (2 bytes), header (1 byte) and error (XOR, 1 byte) bytes.
            //(So we add 4 bytes to the msgSize variable to account for those.)
            //Here, we only care about the size of the msg.
            msgSize = remaining[2] & 0x0F;
            msgSize = msgSize + 4;
            //System.out.println("msgSize: "+msgSize);
            //System.out.println("remaining.length: "+remaining.length);
            //Now we know enough to extract the msg:
            msg = new byte[msgSize];
            System.arraycopy(remaining,0,msg,0,msgSize);

            //And strip it from remaining:
            tempMsg = new byte[remaining.length-msgSize];
            System.arraycopy(remaining, msgSize, tempMsg, 0, remaining.length - msgSize);
            remaining = tempMsg;


            //Do the actual msg handling!
            handleMsg(busName,msg);

        }

        //Any garbage in remaining?
        if(remaining.length > 0){
            System.out.println("Unknown garbage left in package. Handling it as an unknown msg type:");
            printMsgBytes(busName,remaining);
        }
    }

    /**
     * Checks the XOR byte of a single XpressNet message.
     * Returns true if it is correct, false if not.
     *
     * @param msg The message to be checked
     * @return true or false
     */
    private boolean checkXOR(byte[] msg){

        // Calculate the xor error checking bit
        // We start at byte 2 because the call bytes are not included.
        int xor = msg[2];
        for (int i = 3; i < msg.length - 1; i++) {
            xor = (msg[i] ^ xor);
        }

        //Returns true if the calculated xor byte matches the one received, otherwise false.
        return (xor == msg[msg.length - 1]);

    }

	private void handleMsg(String busName, byte[] msg){

        //Bytes 0 and 1 are the call bytes that we do not use here (but we do check them in getMsgs())

        //Check that the XOR byte is correct. If it is, set the message type to what the message says. Otherwise set it as erroneous.
        int function;
        if(checkXOR(msg)) {
            function = msg[2] & 0xF0;
        } else {
            function = ERRONEOUS_XOR;
        }

        switch (function) {
            case ACCESSORY_DECODER_INFORMATION_RESPONSE:
                accessoryDecoderInformationResponse(busName,msg);
                break;

            case ERRONEOUS_XOR:
                errorMsgType(busName, msg);

            default:
                unknownMsgType(busName,msg);
                break;

        }



    }

	private void accessoryDecoderInformationResponse(String busName, byte[] msg) {
		int databytes = msg[2] & 0x0F;
		//System.out.println("** Accessory decoder information response / broadcast **");
		// System.out.println("Bytes with data: " + databytes);
		// 2 bytes per message - first address, then data. First data byte at position 3
		// in the array


		for (int i = 3; i < databytes + 3; i++) {
			int decoderAddr;
			int[] bitNum = new int[4];
			boolean[] bitStatus = new boolean[4];


			decoderAddr = msg[i] & 0x7F; //Bit 8 is reserved and unused, filter it just in case.

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
			for (int j = 0; j <= 3; j++) {

					bitStatus[j] = (getBit(msg[i], j) == 1);

			}
			
			//Put received data into feedbackinfo msg queue
			try {
				for(int j = 0; j <= 3; j++) {
					feedbackInfoMsgs.put(new Object[]{busName, decoderAddr,bitNum[j],bitStatus[j]});
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	private int getBit(byte data, int position) {
		return (data >> position) & 1;
	}

	private void unknownMsgType(String busName, byte[] msg) {
        int function = msg[2] & 0xF0;
	    System.out.print("Unknown msg type: " + function);
	    printMsgBytes(busName, msg);
	}

	private void printMsgBytes(String busName, byte[] msg){
        System.out.println("RAW MSG DATA from bus: "+busName);
        System.out.println("(hex): ");
        /*
        Deprecated in newer java versions.
        TODO: Find a replacement for javax.xml.bind.DatatypeConverter.printHexBinary(msg));
        System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(msg));
        */
        System.out.println("(bin): ");
        for (byte b : msg) {
            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
        }
    }

	private void errorMsgType(String busName, byte[] msg) {
		System.out.println("ERRONEOUS MESSAGE:");

		int xor = msg[2];
		for (int i = 3; i < msg.length - 1; i++) {
			xor = (msg[i] ^ xor);
		}

		System.out.println("Calculated xor byte: " + xor);
		System.out.println("Received xor byte: " + msg[msg.length - 1]);
		printMsgBytes(busName,msg);
	}

}