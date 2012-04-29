/*
    Copyright (c) 2007, 2012 Paul Richards <paul.richards@gmail.com>

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

package tuner;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public final class MainWindow extends JFrame
{
    private static final long serialVersionUID = 1062351301126993589L;

    private final Configuration configuration;
    private final SpectralView spectralView;
    private Thread updateThread;
    private JComboBox instrumentPicker;
    private JComboBox stringPicker;

    private final class LocalItemListener implements ItemListener
    {
        public void itemStateChanged(ItemEvent e)
        {
            if (e.getSource() == instrumentPicker) {
                updateSelectedInstrument();
            } else if (e.getSource() == stringPicker) {
                int index = stringPicker.getSelectedIndex();
                if (index != -1) {
                    updateSelectedString();
                }
            } else {
                throw new IllegalArgumentException("Unexpected event source");
            }
        }
    }

    private MainWindow(Configuration configuration) throws LineUnavailableException
    {
        this.configuration = configuration;

        BorderLayout layout = new BorderLayout();
        this.getContentPane().setLayout(layout);

        FrequencyCurve frequencyCurve = new FrequencyCurve();
        this.spectralView = frequencyCurve;

        this.setTitle("Tuner v0.3");
        this.getContentPane().add(frequencyCurve, BorderLayout.CENTER);
        this.getContentPane().add(createButtonPanel(), BorderLayout.NORTH);
        this.getContentPane().add(createStatusPanel(), BorderLayout.SOUTH);

        pack();
    }

    @Override
    public void dispose()
    {
        super.dispose();
        updateThread.interrupt();
        updateThread = null;
    }

    public void startUpdateThread() throws LineUnavailableException
    {
        AudioSource audioSource = new MicrophoneAudioSource();
        updateThread = new Thread(new UpdateRunner(audioSource, spectralView));
        updateThread.setDaemon(true);
        updateThread.start();
    }

    private JPanel createButtonPanel()
    {
        LayoutManager layout = new GridLayout(1, 2);
        JPanel buttonPanel = new JPanel(layout);

        instrumentPicker = new JComboBox(configuration.instrumentNames().toArray());
        instrumentPicker.addItemListener(new LocalItemListener());
        buttonPanel.add(instrumentPicker);

        stringPicker = new JComboBox();
        stringPicker.addItemListener(new LocalItemListener());
        buttonPanel.add(stringPicker);

        instrumentPicker.setSelectedIndex(0);
        updateSelectedInstrument();
        stringPicker.setSelectedIndex(0);
        updateSelectedString();
        return buttonPanel;
    }

    private JPanel createStatusPanel()
    {
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusPanel.add(new JLabel("\u00A9 2007-2008 Paul Richards <paul.richards@gmail.com>."));
        return statusPanel;
    }

    private String getSelectedInstrument()
    {
        return (String)instrumentPicker.getSelectedItem();
    }

    private String getSelectedString()
    {
        return (String)stringPicker.getSelectedItem();
    }

    private void updateSelectedInstrument()
    {
        stringPicker.removeAllItems();
        for (String stringName: configuration.stringNames(getSelectedInstrument())) {
            stringPicker.addItem(stringName);
        }
    }

    private void updateSelectedString()
    {
        double semitone = configuration.stringSemitone(getSelectedInstrument(), getSelectedString());
        double targetFrequency = configuration.frequencyInHzOfMiddleC() * Math.pow(2.0, semitone / 12);
        spectralView.setTargetFrequency(targetFrequency);
    }

    public static void main(String[] args) throws LineUnavailableException, IOException
    {
        InputStream stream = new BufferedInputStream(new FileInputStream("instruments.xml"));
        Configuration configuration;
        try {
            configuration = Configuration.loadFromStream(stream);
        } finally {
            stream.close();
            stream = null;
        }

        MainWindow window = new MainWindow(configuration);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
        window.startUpdateThread();
    }
}
