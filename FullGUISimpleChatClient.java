package HeadFirstJava.Chapter15.AdviceServerAndClient.SimpleChatClient.Version3;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public class FullGUISimpleChatClient {

    JPanel mainPanel;
    ArrayList<JCheckBox> checkboxList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame theFrame;
    JTextArea incoming;
    JTextField outgoing;
    BufferedReader inputWriter;
    PrintWriter outputWriter;
    Socket socket;

    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat",
            "Open Hi-Hat","Acoustic Snare", "Crash Cymbal", "Hand Clap",
            "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
            "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo",
            "Open Hi Conga"};

    int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};


    public static void main (String[] args) {
        FullGUISimpleChatClient BeatBox = new FullGUISimpleChatClient();
        BeatBox.buildGUI();
    }

    public void buildGUI() {
        theFrame = new JFrame("Cyber BeatBox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel greaterPanel = new JPanel(layout);
        greaterPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        greaterPanel.setBackground(Color.gray);
        JPanel leftInsidePanel = new JPanel();
        leftInsidePanel.setBackground(Color.gray);
        JPanel rightInsidePanel = new JPanel();
        rightInsidePanel.setBackground(Color.gray);

        Box chatBox = new Box(BoxLayout.Y_AXIS);

        checkboxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton serializeIt = new JButton("serializeIt");
        serializeIt.addActionListener(new SerializeListener());
        buttonBox.add(serializeIt);

        JButton restoreIt = new JButton("restore");
        restoreIt.addActionListener(new MyReadListener());
        buttonBox.add(restoreIt);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }

        // CHAT CLIENT GUI SECTION.
        incoming = new JTextArea(10, 20);
        incoming.setLineWrap(true);
        incoming.setWrapStyleWord(true);
        incoming.setEditable(false);
//        incoming.addFocusListener(new Focuus);

        JScrollPane qScroller = new JScrollPane(incoming);
        qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        chatBox.add(qScroller);
        outgoing = new JTextField("Please type here: ", 10);
        chatBox.add(outgoing);
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());
        chatBox.add(sendButton);

        setUpNetworking();

        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();

        JPanel topRightPanel = new JPanel();
        topRightPanel.setBackground(Color.gray);
        topRightPanel.add(buttonBox);

        JPanel bottomRightPanel = new JPanel();
        bottomRightPanel.setBackground(Color.gray);
        bottomRightPanel.add(BorderLayout.WEST, chatBox);

        Box panelBox = new Box(BoxLayout.Y_AXIS);
        panelBox.add(topRightPanel);
        panelBox.add(bottomRightPanel);

        rightInsidePanel.add(panelBox);

        leftInsidePanel.add(BorderLayout.WEST, nameBox);
        greaterPanel.add(BorderLayout.WEST, leftInsidePanel);
        greaterPanel.add(BorderLayout.EAST, rightInsidePanel);
        theFrame.getContentPane().add(greaterPanel);

        GridLayout grid = new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        greaterPanel.add(BorderLayout.CENTER, mainPanel);
        mainPanel.setBackground(Color.gray);
        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setBackground(Color.gray);
            c.setSelected(false);
            checkboxList.add(c);
            mainPanel.add(c);
        } // end loop

        setUpMidi();



        theFrame.setBounds(50,50,300,300);
        theFrame.pack();
        theFrame.setVisible(true);
    } // close method


    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ,4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);

        } catch(Exception e) {e.printStackTrace();}
    } // close method

    public void buildTrackAndStart() {
        int[] trackList = null;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            trackList = new int[16];

            int key = instruments[i];

            for (int j = 0; j < 16; j++ ) {
                JCheckBox jc = (JCheckBox) checkboxList.get(j + (16*i));
                if ( jc.isSelected()) {
                    trackList[j] = key;
                } else {
                    trackList[j] = 0;
                }
            } // close inner loop

            makeTracks(trackList);
            track.add(makeEvent(176,1,127,0,16));
        } // close outer

        track.add(makeEvent(192,9,1,0,15));
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch(Exception e) {e.printStackTrace();}
    } // close buildTrackAndStart method

    public class MyStartListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            buildTrackAndStart();
        }
    } // close inner class

    public class MyStopListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            sequencer.stop();
        }
    } // close inner class

    public class MyUpTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 1.03));
        }
    } // close inner class

    public class MyDownTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * .97));
        }
    } // close inner class

    public class SendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                outputWriter.println(outgoing.getText()); // outgoing = JTextField.
                outputWriter.flush();
                System.out.println("sent: " + outgoing.getText());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            outgoing.setText("");
            outgoing.requestFocus();
        }
    }

    public class IncomingReader implements Runnable {
        public void run() {
            String message;
            System.out.println("Inside IncomingReader");
            try {
                while ((message = inputWriter.readLine()) != null) {
                    System.out.println("read: " + message);
                    incoming.append(message + "\n");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void setUpNetworking() {
        try {
            try {
                socket = new Socket("127.0.0.1", 4695);
                System.out.println("Network Connection Established");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Could not connect. Is the Server online?");
            }
            outputWriter = new PrintWriter(socket.getOutputStream());
            inputWriter = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void makeTracks(int[] list) {

        for (int i = 0; i < 16; i++) {
            int key = list[i];

            if (key != 0) {
                track.add(makeEvent(144,9,key, 100, i));
                track.add(makeEvent(128,9,key, 100, i+1));
            }
        }
    }

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);

        } catch(Exception e) {e.printStackTrace(); }
        return event;
    }

// ===================================SERIALIZATION LISTENERS ========================================================

    public class SerializeListener implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent e) {
            boolean[] checkboxState = new boolean[256];

            for (int i = 0; i < 256; i++) {
                JCheckBox check = (JCheckBox) checkboxList.get(i);
                if (check.isSelected()) {
                    checkboxState[i] = true;
                }
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream("Checkbox.ser");
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(checkboxState);
                objectOutputStream.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            } // close try-catch
        } // close action-performed.
    } // close MySendListener.

    public class MyReadListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            boolean[] checkboxState = null;
            try {
                FileInputStream fileIn = new FileInputStream("Checkbox.ser"); // ser file.
                ObjectInputStream ois = new ObjectInputStream(fileIn);
                checkboxState = (boolean[]) ois.readObject();
                System.out.println(Arrays.toString(checkboxState));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            for (int i = 0; i < 256; i++) {
                JCheckBox check = (JCheckBox) checkboxList.get(i);
                if (checkboxState[i]) {
                    check.setSelected(true);
                } else {
                    check.setSelected(false);
                }
            }
            sequencer.stop();
            buildTrackAndStart();
        } // end action performed
    }

} // close class


