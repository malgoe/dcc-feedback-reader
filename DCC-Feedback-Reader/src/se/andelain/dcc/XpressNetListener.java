/**
 * 
 */
package se.andelain.dcc;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Martin Algo
 * 
 *         Basic TCP test
 *
 */
public class XpressNetListener implements Runnable {

	private Socket skt;
	private InputStream stream;
	private String ip;
	private LinkedBlockingQueue<byte[]> queue;
	private int port;
	private int bufSize;
	private boolean notStopped = true;

	public XpressNetListener(String ip, int port, int bufSize, LinkedBlockingQueue<byte[]> queue) {
		this.ip = ip;
		this.queue = queue;
		this.port = port;
		this.bufSize = bufSize;
	}

	public void run() {
		try {
			skt = new Socket(ip, port);
			skt.setSoTimeout(1000);
			//System.out.println("Opened socket");

			stream = skt.getInputStream();
			byte[] data;
			int count;

			while (notStopped) {
				data = new byte[bufSize];

				try {
					count = stream.read(data);

					// Compact it
					byte[] compactData = new byte[count];
					System.arraycopy(data, 0, compactData, 0, count);

					// Put it in the queue
					queue.put(compactData);
					//System.out.println("MSG received!");

				} catch (SocketTimeoutException e) {
					//System.out.print(".");
				}

				// Check if we've been interrupted
				if (Thread.currentThread().isInterrupted()) {
					notStopped = false;
				}
			}

		} catch (InterruptedException e) {
			System.out.println("Listener was interrupted!");
		} catch (SocketException e) {
			System.out.println(
					"Listener socket was forcibly closed. Unless you where exiting the application this may be an error.");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// Close stream & socket
			try {
				stream.close();
				skt.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
}
