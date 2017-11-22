import se.andelain.dcc.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class MyGui {
    private JTable fbitTable;
    private JPanel panel1;
    private JScrollPane jscrl;
    private JButton startButton;
    private JButton stopButton;
    private JLabel msgSecLabel;
    private JButton saveButton;
    private JButton openButton;
    private JTable listenerTable;
    private JButton addListenerButton;
    private JButton removeBusButton;
    private JTextField busNameField;
    private JTextField busIPField;
    private JLabel busNameFieldLabel;
    private JLabel busIPFieldLabel;
    private Timer timer1;
    protected Thread[] listeners;
    static LinkedBlockingQueue<byte[]> incomingMsgQueue = new LinkedBlockingQueue<>();
    static LinkedBlockingQueue<Object[]> feedbackInfo = new LinkedBlockingQueue<>();
    private final JFileChooser fc = new JFileChooser();
    private XpressNetListenerTableModel busListenerModel;
    private FbBitTableModel fbBitTableModel;


    public MyGui() {
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                // Start the listeners
                listeners = new Thread[busListenerModel.getRowCount()];
                for(int i = 0; i < listeners.length; i++){
                    listeners[i] = new Thread(new XpressNetListener((String)busListenerModel.getValueAt(i,1), 5550, 512, incomingMsgQueue));
                    listeners[i].start();
                }

                startButton.setEnabled(false);
                saveButton.setEnabled(false);
                openButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // Stop the listeners
                for(int i = 0; i < listeners.length; i++){
                    listeners[i].interrupt();
                }
                startButton.setEnabled(true);
                saveButton.setEnabled(true);
                openButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //Get file to open
                int returnVal = fc.showOpenDialog(panel1);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    List<FbBit> fbList = null;
                    try {
                        FileInputStream fcIn = new FileInputStream(file);
                        ObjectInputStream fcInObj = new ObjectInputStream(fcIn);
                        fbList = (List<FbBit>) fcInObj.readObject();

                    } catch (Exception e){
                        //TODO: Implement error msg!
                        System.out.println("Failed to open file! "+ e.getMessage());
                    }
                    if(fbList != null) {
                        ((FbBitTableModel) fbitTable.getModel()).setFbBits(fbList);
                    } else {
                        System.out.println("fbList was null");
                    }

                } else {
                    //User cancelled. Do nothing. :)
                }
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int returnVal = fc.showSaveDialog(panel1);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        FileOutputStream fcOut = new FileOutputStream(file);
                        ObjectOutputStream fcOutObj = new ObjectOutputStream(fcOut);
                        fcOutObj.writeObject(((FbBitTableModel) fbitTable.getModel()).getFbBits());

                    } catch (Exception e){
                        //TODO: Implement error msg!
                        System.out.println("Failed to save file! "+ e.getMessage());
                    }

                } else {
                    //User cancelled. Do nothing. :)
                }
            }
        });
        removeBusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //TODO: Implement this :) Should be trivial, just add remove method in table model.
            }
        });
        addListenerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //busListenerModel.addBusListener("FB1","192.168.202.11",true);
                //busListenerModel.addBusListener("FB2","192.168.202.12",true);

                    busListenerModel.addBusListener(busNameField.getText(),busIPField.getText(),true);

            }
        });
    }

    public static void main(String[] args) {
        /*
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }
        */

        setUIFont(new javax.swing.plaf.FontUIResource("SansSerif", Font.PLAIN,15));

        JFrame frame = new JFrame("DCC FB Reader");
        frame.setContentPane(new MyGui().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        //Start the interpreter
        Thread interpreter = new Thread(new XpressNetDataInterpreter(incomingMsgQueue, feedbackInfo));
        interpreter.start();



    }

    public static void setUIFont (javax.swing.plaf.FontUIResource f){
        java.util.Enumeration keys = UIManager.getLookAndFeelDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get (key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put (key, f);
        }
    }

    private void createUIComponents() {

        busListenerModel = new XpressNetListenerTableModel();
        fbBitTableModel = new FbBitTableModel();

        fbitTable = new JTable(fbBitTableModel);
        listenerTable = new JTable(busListenerModel);
        /*
        for (int count = 1; count <= 10; count++){
            FbBit fbBit = new FbBit(count,1,false);
            fbBitTableModel.addFbBit(fbBit);
        }

        fbBitTableModel.updateFbBit(100,5,true);
        fbBitTableModel.updateFbBit(2,1,true);
        */
        timer1 = new Timer(1000, new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e){

               /*
               //Not pretty, but might be useful sometimes
               ((FbBitTableModel)fbitTable.getModel()).addFbBit(fbBit);
               i++;

               boolean stat;
               if(i %2 != 0){
                   stat = true;
               } else {
                   stat = false;
               }
               i++;

               SwingUtilities.invokeLater(new Runnable() {
                   public void run() {

                   }
               });
               */
                int msgSec = 0;
               while(true){
                   Object[] data = feedbackInfo.poll();
                   if (data == null){
                       msgSecLabel.setText("Msgs received/sec: "+msgSec);
                       break;
                   } else {
                       msgSec++;
                       fbBitTableModel.updateFbBit((int)data[0], (int)data[1], (boolean)data[2]);
                   }
               }

           }
        });

        timer1.start();
    }
}
