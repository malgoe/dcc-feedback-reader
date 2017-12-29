package se.andelain.dcc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
@DisplayName("XpressNetDataInterpreter")
class XpressNetDataInterpreterTest {
    Thread interpreter = null;
    LinkedBlockingQueue<Object[]> incomingMsgQueue = new LinkedBlockingQueue<>();
    LinkedBlockingQueue<Object[]> feedbackInfo = new LinkedBlockingQueue<>();

    @BeforeEach
    void setUp() {
        interpreter = new Thread(new XpressNetDataInterpreter(incomingMsgQueue, feedbackInfo));
        interpreter.start();
    }

    @AfterEach
    void tearDown() {
        interpreter.interrupt();
    }

    @DisplayName("Single Accessory Decoder Information Response")
    @Test
    void SingleMessage_AccessoryDecoderInformationResponse() {
        //Tests sending an accessory decoder information response.
        //Expects a FbBit object with the proper variables to be returned in the feedbackInfo queue.
        String busName = "TestBus1";

        //0xFF + 0xFD = call bytes. 0x42 = header, accessory decoder information response, data length 2 bytes.
        //0x5F = decoder addr: 95, 0x4A = feedback module, lower nibble (lower 4 feedback bits), 1010,
        //0x57 = XOR byte
        byte[] msgPack = new byte[]{(byte)0xFF,(byte)0xFD,(byte)0x42,(byte)0x5F,(byte)0x4A,(byte)0x57};

        try {
            incomingMsgQueue.put(new Object[]{busName,msgPack});
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //Give the interpreter time to work
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Expect 4 messages back on feedBackInfo (one for each FbBit) and null on the 5th poll.
        Object[] bit1 = feedbackInfo.poll();
        Object[] bit2 = feedbackInfo.poll();
        Object[] bit3 = feedbackInfo.poll();
        Object[] bit4 = feedbackInfo.poll();
        Object[] bit5 = feedbackInfo.poll();


        assertNotNull(bit1);
        assertNotNull(bit2);
        assertNotNull(bit3);
        assertNotNull(bit4);
        assertNull(bit5);

        //Is the data correct?
        assertEquals(busName,(String)bit1[0]); //Bus name
        assertEquals(95,(int)bit1[1]); //Addr
        assertEquals(1,(int)bit1[2]); //Bit
        assertEquals(false,(boolean)bit1[3]); //Status

        assertEquals(busName,(String)bit2[0]); //Bus name
        assertEquals(95,(int)bit2[1]); //Addr
        assertEquals(2,(int)bit2[2]); //Bit
        assertEquals(true,(boolean)bit2[3]); //Status

        assertEquals(busName,(String)bit3[0]); //Bus name
        assertEquals(95,(int)bit3[1]); //Addr
        assertEquals(3,(int)bit3[2]); //Bit
        assertEquals(false,(boolean)bit3[3]); //Status

        assertEquals(busName,(String)bit4[0]); //Bus name
        assertEquals(95,(int)bit4[1]); //Addr
        assertEquals(4,(int)bit4[2]); //Bit
        assertEquals(true,(boolean)bit4[3]); //Status

    }

    @DisplayName("Multiple separate Accessory Decoder Information Response in same package")
    @Test
    void MultipleMessage_AccessoryDecoderInformationResponse() {
        //Tests sending multiple accessory decoder information responses as separate messages but received in a single package.
        //Expects a FbBit object with the proper variables to be returned in the feedbackInfo queue.
        String busName = "TestBus1";

        //0xFF + 0xFD = call bytes. 0x42 = header, accessory decoder information response, data length 2 bytes.
        //0x5F = decoder addr: 95, 0x4A / 0x55 = feedback module, lower nibble (lower 4 feedback bits), 1010 in first msg, upper nibble 0101 in second,
        //0x57 / 0x48 = XOR byte
        byte[] msgPack = new byte[]{(byte)0xFF,(byte)0xFD,(byte)0x42,(byte)0x5F,(byte)0x4A,(byte)0x57,(byte)0xFF,(byte)0xFD,(byte)0x42,(byte)0x5F,(byte)0x55,(byte)0x48};

        try {
            incomingMsgQueue.put(new Object[]{busName,msgPack});
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //Give the interpreter time to work
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Expect 4 messages back on feedBackInfo (one for each FbBit) and null on the 5th poll.
        Object[] bit1 = feedbackInfo.poll();
        Object[] bit2 = feedbackInfo.poll();
        Object[] bit3 = feedbackInfo.poll();
        Object[] bit4 = feedbackInfo.poll();
        Object[] bit5 = feedbackInfo.poll();
        Object[] bit6 = feedbackInfo.poll();
        Object[] bit7 = feedbackInfo.poll();
        Object[] bit8 = feedbackInfo.poll();
        Object[] bit9 = feedbackInfo.poll();


        assertNotNull(bit1);
        assertNotNull(bit2);
        assertNotNull(bit3);
        assertNotNull(bit4);
        assertNotNull(bit5);
        assertNotNull(bit6);
        assertNotNull(bit7);
        assertNotNull(bit8);
        assertNull(bit9);

        //Is the data correct? (first msg)
        assertEquals(busName,(String)bit1[0]); //Bus name
        assertEquals(95,(int)bit1[1]); //Addr
        assertEquals(1,(int)bit1[2]); //Bit
        assertEquals(false,(boolean)bit1[3]); //Status

        assertEquals(busName,(String)bit2[0]); //Bus name
        assertEquals(95,(int)bit2[1]); //Addr
        assertEquals(2,(int)bit2[2]); //Bit
        assertEquals(true,(boolean)bit2[3]); //Status

        assertEquals(busName,(String)bit3[0]); //Bus name
        assertEquals(95,(int)bit3[1]); //Addr
        assertEquals(3,(int)bit3[2]); //Bit
        assertEquals(false,(boolean)bit3[3]); //Status

        assertEquals(busName,(String)bit4[0]); //Bus name
        assertEquals(95,(int)bit4[1]); //Addr
        assertEquals(4,(int)bit4[2]); //Bit
        assertEquals(true,(boolean)bit4[3]); //Status

        //Second msg
        assertEquals(busName,(String)bit5[0]); //Bus name
        assertEquals(95,(int)bit5[1]); //Addr
        assertEquals(5,(int)bit5[2]); //Bit
        assertEquals(true,(boolean)bit5[3]); //Status

        assertEquals(busName,(String)bit6[0]); //Bus name
        assertEquals(95,(int)bit6[1]); //Addr
        assertEquals(6,(int)bit6[2]); //Bit
        assertEquals(false,(boolean)bit6[3]); //Status

        assertEquals(busName,(String)bit7[0]); //Bus name
        assertEquals(95,(int)bit7[1]); //Addr
        assertEquals(7,(int)bit7[2]); //Bit
        assertEquals(true,(boolean)bit7[3]); //Status

        assertEquals(busName,(String)bit8[0]); //Bus name
        assertEquals(95,(int)bit8[1]); //Addr
        assertEquals(8,(int)bit8[2]); //Bit
        assertEquals(false,(boolean)bit8[3]); //Status

    }

    @DisplayName("Multiple Accessory Decoder Information Response in same message")
    @Test
    void MultipleDecoder_AccessoryDecoderInformationResponse() {
        //Tests sending multiple accessory decoder information responses in one message.
        //Expects a FbBit object with the proper variables to be returned in the feedbackInfo queue.
        String busName = "TestBus1";

        //0xFF + 0xFD = call bytes. 0x42 = header, accessory decoder information response, data length 2 bytes.
        //0x5F = decoder addr: 95, 0x4A / 0x55 = feedback module, lower nibble (lower 4 feedback bits), 1010 in first msg, upper nibble 0101 in second,
        //0x57 / 0x48 = XOR byte
        byte[] msgPack = new byte[]{(byte)0xFF,(byte)0xFD,(byte)0x44,(byte)0x5F,(byte)0x4A,(byte)0x5F,(byte)0x55,(byte)0x5B};

        try {
            incomingMsgQueue.put(new Object[]{busName,msgPack});
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //Give the interpreter time to work
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Expect 4 messages back on feedBackInfo (one for each FbBit) and null on the 5th poll.
        Object[] bit1 = feedbackInfo.poll();
        Object[] bit2 = feedbackInfo.poll();
        Object[] bit3 = feedbackInfo.poll();
        Object[] bit4 = feedbackInfo.poll();
        Object[] bit5 = feedbackInfo.poll();
        Object[] bit6 = feedbackInfo.poll();
        Object[] bit7 = feedbackInfo.poll();
        Object[] bit8 = feedbackInfo.poll();
        Object[] bit9 = feedbackInfo.poll();


        assertNotNull(bit1);
        assertNotNull(bit2);
        assertNotNull(bit3);
        assertNotNull(bit4);
        assertNotNull(bit5);
        assertNotNull(bit6);
        assertNotNull(bit7);
        assertNotNull(bit8);
        assertNull(bit9);

        //Is the data correct? (first msg)
        assertEquals(busName,(String)bit1[0]); //Bus name
        assertEquals(95,(int)bit1[1]); //Addr
        assertEquals(1,(int)bit1[2]); //Bit
        assertEquals(false,(boolean)bit1[3]); //Status

        assertEquals(busName,(String)bit2[0]); //Bus name
        assertEquals(95,(int)bit2[1]); //Addr
        assertEquals(2,(int)bit2[2]); //Bit
        assertEquals(true,(boolean)bit2[3]); //Status

        assertEquals(busName,(String)bit3[0]); //Bus name
        assertEquals(95,(int)bit3[1]); //Addr
        assertEquals(3,(int)bit3[2]); //Bit
        assertEquals(false,(boolean)bit3[3]); //Status

        assertEquals(busName,(String)bit4[0]); //Bus name
        assertEquals(95,(int)bit4[1]); //Addr
        assertEquals(4,(int)bit4[2]); //Bit
        assertEquals(true,(boolean)bit4[3]); //Status

        //Second msg
        assertEquals(busName,(String)bit5[0]); //Bus name
        assertEquals(95,(int)bit5[1]); //Addr
        assertEquals(5,(int)bit5[2]); //Bit
        assertEquals(true,(boolean)bit5[3]); //Status

        assertEquals(busName,(String)bit6[0]); //Bus name
        assertEquals(95,(int)bit6[1]); //Addr
        assertEquals(6,(int)bit6[2]); //Bit
        assertEquals(false,(boolean)bit6[3]); //Status

        assertEquals(busName,(String)bit7[0]); //Bus name
        assertEquals(95,(int)bit7[1]); //Addr
        assertEquals(7,(int)bit7[2]); //Bit
        assertEquals(true,(boolean)bit7[3]); //Status

        assertEquals(busName,(String)bit8[0]); //Bus name
        assertEquals(95,(int)bit8[1]); //Addr
        assertEquals(8,(int)bit8[2]); //Bit
        assertEquals(false,(boolean)bit8[3]); //Status

    }
}