/*

Tuner, a simple application to help you tune your musical instrument.
Copyright (c) 2003, 2004, 2005, 2012 Paul Richards <paul.richards@gmail.com>

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

*/


import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.*;

public class Tuner extends JFrame implements ActionListener, Runnable {

    // Version number of application
    private static final String VERSION = "0.2";
    // Copyright message
    private static final String COPYRIGHT =
        "Tuner version " + VERSION + ", Copyright (C) 2003-2005 Paul Richards\n" +
        "Tuner comes with ABSOLUTELY NO WARRANTY; for details\n" +
        "see the file COPYRIGHT.TXT.  This is free software, and you are welcome\n" +
        "to redistribute it under certain conditions; see the file COPYRIGHT.TXT\n" +
        "for details.";
    // Window title
    private static final String TITLE = "Tuner v" + VERSION;
    // Credits message
    private static final String CREDITS = "Made by Paul Richards, pauldoo@users.sf.net";

    // Instrument names
    private final String[] instruments;

    // For each instrument, the list of note semitones and names
    private final int[][] semitones;
    private final String[][] names;

    // AudioInput object connected to microphone
    private AudioInput audioInput;

    // GUI components
    private JPanel chooserPanel;
    private JComboBox instrumentBox;
    private JComboBox noteBox;
    private TuningBars bars;

    // Main
    public static void main(String[] args) throws Exception {
        JOptionPane.showMessageDialog( null, COPYRIGHT );

	InputStream in = new BufferedInputStream( new FileInputStream( "tuner.properties" ) );
	Properties props = new Properties();
	props.load( in );

	int instrumentCount = Integer.parseInt( props.getProperty("InstrumentCount") );
	String[] instruments = new String[ instrumentCount ];
	int[][] semitones = new int[instrumentCount][];
	String[][] names = new String[instrumentCount][];

	for (int i = 0; i < instrumentCount; i++) {
	    String iPrefix = "Instrument_" + i;
	    String name = props.getProperty(iPrefix + ".Name");
	    instruments[i] = name;
	    int noteCount = Integer.parseInt(props.getProperty(iPrefix + ".NoteCount"));
	    semitones[i] = new int[noteCount];
	    names[i] = new String[noteCount];
	    for (int j = 0; j < noteCount; j++) {
		String nPrefix = iPrefix + ".Note_" + j;
		int semitone = Integer.parseInt(props.getProperty(nPrefix + ".Semitone" ));
		semitones[i][j] = semitone;
		String noteName = props.getProperty(nPrefix + ".Name" );
		names[i][j] = noteName;
	    }
	}

        Tuner t = new Tuner( TITLE, instruments, semitones, names );
        t.launch();

    }

    public Tuner( String title, String[] instruments, int[][] semitones, String[][] names ) {
        super( title );
        this.setDefaultCloseOperation( EXIT_ON_CLOSE );
        this.instruments = instruments;
        this.semitones = semitones;
        this.names = names;

	chooserPanel = new JPanel(new GridLayout( 1, 2 ));
	instrumentBox = new JComboBox( instruments );
	instrumentBox.addActionListener( this );
	noteBox = new JComboBox();
	noteBox.addActionListener( this );
	chooserPanel.add( instrumentBox );

        this.getContentPane().add( chooserPanel, BorderLayout.NORTH );

        JLabel credits = new JLabel( CREDITS );
        this.getContentPane().add( credits, BorderLayout.SOUTH );
    }


    public void launch() throws Exception {
	this.audioInput = TuningBars.createAudioInput();

        updateInstrument();
        this.setVisible(true);

        new Thread(this).start();
    }

    private int getNoteNumber() {
	return noteBox.getSelectedIndex();
    }

    private void updateNote() {
	int noteNumber = getNoteNumber();
	if (bars != null) {
	    this.getContentPane().remove( bars );
	}
	int[] iSemitones = semitones[getInstrumentNumber()];
	bars = new TuningBars( audioInput, iSemitones[noteNumber] );
	this.getContentPane().add( bars, BorderLayout.CENTER );
        this.pack();
    }

    private int getInstrumentNumber() {
	return instrumentBox.getSelectedIndex();
    }

    private void updateInstrument() {
	int instrumentNumber = getInstrumentNumber();
	if (noteBox != null) {
	    chooserPanel.remove( noteBox );
	}
	String[] noteNames = names[instrumentNumber];
	noteBox = new JComboBox( noteNames );
	noteBox.addActionListener( this );
	chooserPanel.add( noteBox );
	updateNote();
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == instrumentBox) {
	    updateInstrument();
	} else if (e.getSource() == noteBox) {
	    updateNote();
	}

    }

    public void run() {
        try {
            while (true) {
		bars.update();
	    }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

