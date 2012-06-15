/*
    Copyright (c) 2008, 2012 Paul Richards <paul.richards@gmail.com>

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

package pigeon.report;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Collection;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import pigeon.model.Member;
import pigeon.view.Configuration;

final public class MembersReporter implements Reporter
{
    private final Document document;

    public MembersReporter(String organization, Collection<Member> members, Configuration.Mode mode)
    {
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            document.appendChild(document.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"" + Constants.XSL_FOR_XHTML_FILENAME + "\""));

            final Element rootElement = document.createElement("MembersReport");

            final Element organisationElement = document.createElement("Organisation");
            organisationElement.setTextContent(organization);
            rootElement.appendChild(organisationElement);

            final Element memberListElement = document.createElement("MemberList");
            for (Member member: members) {
                final Element memberElement = document.createElement("Member");

                final Element nameElement = document.createElement("Name");
                nameElement.setTextContent(member.getName());
                memberElement.appendChild(nameElement);
                final Element addressElement = document.createElement("Address");
                Utilities.appendLineElements(addressElement, member.getAddress(), document);
                memberElement.appendChild(addressElement);
                final Element telephoneElement = document.createElement("Telephone");
                telephoneElement.setTextContent(member.getTelephone());
                memberElement.appendChild(telephoneElement);
                final Element shuNumberElement = document.createElement("ShuNumber");
                shuNumberElement.setTextContent(member.getSHUNumber());
                memberElement.appendChild(shuNumberElement);

                switch (mode) {
                    case CLUB:
                        break;
                    case FEDERATION:
                        final Element clubElement = document.createElement("Club");
                        clubElement.setTextContent(member.getClub());
                        memberElement.appendChild(clubElement);
                        final Element sectionElement = document.createElement("Section");
                        sectionElement.setTextContent(member.getSection());
                        memberElement.appendChild(sectionElement);
                }

                memberListElement.appendChild(memberElement);
            }
            rootElement.appendChild(memberListElement);
            document.appendChild(rootElement);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(StreamProvider streamProvider) throws IOException
    {
        try {
            final Transformer xmlOutputTransformer = TransformerFactory.newInstance().newTransformer();
            xmlOutputTransformer.setOutputProperty(OutputKeys.INDENT, "yes");

            xmlOutputTransformer.transform(
                    new DOMSource(document),
                    new StreamResult(streamProvider.createNewStream("members.xml", true)));

            final Transformer csvOutputTransformer = TransformerFactory.newInstance().newTransformer(new StreamSource(ClassLoader.getSystemResourceAsStream("resources/" + Constants.XSL_FOR_CSV_FILENAME)));
            csvOutputTransformer.transform(
                    new DOMSource(document),
                    new StreamResult(streamProvider.createNewStream("members.csv", false)));

            pigeon.report.Utilities.copyStream(
                    new BufferedInputStream(ClassLoader.getSystemResourceAsStream("resources/" + Constants.XSL_FOR_XHTML_FILENAME)),
                    streamProvider.createNewStream(Constants.XSL_FOR_XHTML_FILENAME, false));
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }
}
