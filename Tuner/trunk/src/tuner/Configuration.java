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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
    Parses the XML configuration file, and provides accessors to get the list
    of instruments and their string frequencies.
*/
public final class Configuration
{
    private final Document document;

    public Configuration(Document document)
    {
        this.document = document;
    }

    public static Configuration loadFromStream(InputStream stream) throws IOException
    {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
            return new Configuration(document);
        } catch (ParserConfigurationException ex) {
            throw new IOException(ex.toString());
        } catch (SAXException ex) {
            throw new IOException(ex.toString());
        }
    }

    private Node findTunerInstrumentsNode()
    {
        for (int i = 0; i < document.getChildNodes().getLength(); i++) {
            Node child = document.getChildNodes().item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("TunerInstruments")) {
                return child;
            }
        }
        throw new RuntimeException("Could not find 'TunerInstruments' node.");
    }

    public double frequencyInHzOfMiddleC()
    {
        for (int i = 0; i < findTunerInstrumentsNode().getChildNodes().getLength(); i++) {
            Node child = findTunerInstrumentsNode().getChildNodes().item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("MiddleC")) {
                return Double.parseDouble(child.getTextContent());
            }
        }
        throw new RuntimeException("Couldn't find MiddleC from configuration.");
    }

    /**
        Returns the list of instruments configured.
    */
    public List<String> instrumentNames()
    {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < findTunerInstrumentsNode().getChildNodes().getLength(); i++) {
            Node child = findTunerInstrumentsNode().getChildNodes().item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("Instrument")) {
                String instrumentName = child.getAttributes().getNamedItem("name").getTextContent();
                result.add(instrumentName);
            }
        }
        return result;
    }

    private Node findInstrumentNode(String instrumentName)
    {
        for (int i = 0; i < findTunerInstrumentsNode().getChildNodes().getLength(); i++) {
            Node child = findTunerInstrumentsNode().getChildNodes().item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("Instrument")) {
                String name = child.getAttributes().getNamedItem("name").getTextContent();
                if (name.equals(instrumentName)) {
                    return child;
                }
            }
        }
        throw new RuntimeException("No instrument called '" + instrumentName + "'.");
    }

    /**
        Returns the names of the strings on a given instrument.
    */
    public List<String> stringNames(String instrumentName)
    {
        Node instrumentNode = findInstrumentNode(instrumentName);
        List<String> result = new ArrayList<String>();

        for (int i = 0; i < instrumentNode.getChildNodes().getLength(); i++) {
            Node stringNode = instrumentNode.getChildNodes().item(i);
            if (stringNode.getNodeType() == Node.ELEMENT_NODE && stringNode.getNodeName().equals("String")) {
                String name = stringNode.getAttributes().getNamedItem("name").getTextContent();
                result.add(name);
            }
        }
        return result;
    }

    private Node findStringNode(String instrumentName, String stringName)
    {
        Node instrumentNode = findInstrumentNode(instrumentName);
        List<String> result = new ArrayList<String>();

        for (int i = 0; i < instrumentNode.getChildNodes().getLength(); i++) {
            Node stringNode = instrumentNode.getChildNodes().item(i);
            if (stringNode.getNodeType() == Node.ELEMENT_NODE && stringNode.getNodeName().equals("String")) {
                String name = stringNode.getAttributes().getNamedItem("name").getTextContent();
                if (name.equals(stringName)) {
                    return stringNode;
                }
            }
        }
        throw new RuntimeException("No string called '" + stringName + "' on instrument '" + instrumentName + "'.");
    }

    /**
        Returns the semitone (relative to middle C) of a string on a given instrument.
    */
    public double stringSemitone(String instrumentName, String stringName)
    {
        Node stringNode = findStringNode(instrumentName, stringName);

        for (int i = 0; i < stringNode.getChildNodes().getLength(); i++) {
            Node semitoneNode = stringNode.getChildNodes().item(i);
            if (semitoneNode.getNodeType() == Node.ELEMENT_NODE && semitoneNode.getNodeName().equals("Semitone")) {
                return Double.parseDouble(semitoneNode.getTextContent());
            }
        }
        throw new RuntimeException("Missing Semitone node on '" + instrumentName + "' / '" + stringName + "'.");
    }
}
