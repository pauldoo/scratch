<?xml version="1.0" encoding="UTF-8"?>
<!--
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
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output
        method="html"
        doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
        indent="yes"
        media-type="application/xhtml+xml"
        />

    <xsl:variable name="containsClub" select="count(//MemberList/Member/Club) > 0"/>
    <xsl:variable name="containsSection" select="count(//MemberList/Member/Section) > 0"/>

    <xsl:template name="CssStylesheet">
        <style type="text/css" media="screen">
            body { font-family: Verdana, sans-serif; white-space: nowrap; }
            .outer { text-align:center; page-break-after: always; }
            .outer.last { page-break-after: auto; }
            h1 { margin-bottom:10px; font-size:18pt; }
            h2 { font-size:16pt; }
            h3 { font-size:14pt; }
            h2, h3 { margin-top:0; margin-bottom:5px; }
            table { width:95%; border:1px solid #000000; border-collapse:collapse; font-size:8pt; margin-top:20px; }
            th { border-bottom:3px solid #000000; text-align: left; }
            td { border-bottom:1px solid #000000; page-break-inside:avoid; padding:3px 0 3px 0; text-align: left; }
            .numeric { text-align: right; }
        </style>
        <style type="text/css" media="print">
            body { font-family: Verdana, sans-serif; white-space: nowrap; }
            .outer { text-align:center; page-break-after: always; }
            .outer.last { page-break-after: auto; }
            h1 { margin-bottom:10px; font-size:18pt; }
            h2 { font-size:16pt; }
            h3 { font-size:14pt; }
            h2, h3 { margin-top:0; margin-bottom:5px; }
            table { width:95%; border:1px solid #000000; border-collapse:collapse; font-size:6pt; margin-top:20px; }
            th { border-bottom:3px solid #000000; text-align: left; }
            td { border-bottom:1px solid #000000; page-break-inside:avoid; padding:3px 0 3px 0; text-align: left; }
            .numeric { text-align: right; }
        </style>
    </xsl:template>

    <xsl:template match="/MembersReport">
        <html>
            <head>
                <title>Members for <xsl:value-of select="Organisation"/></title>
                <xsl:call-template name="CssStylesheet"/>
            </head>
            <body>
                <div class="outer">
                    <xsl:apply-templates select="Organisation"/>
                    <xsl:apply-templates select="MemberList"/>
                </div>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="Organisation">
        <h1><xsl:value-of select="."/></h1>
    </xsl:template>

    <xsl:template match="MemberList">
        <h2>Members</h2>
        <table>
            <tr>
                <th>Name</th>
                <xsl:if test="$containsClub"><th>Club</th></xsl:if>
                <xsl:if test="$containsSection"><th>Section</th></xsl:if>
                <th>Address</th>
                <th>Telephone</th>
                <th>SHU Number</th>
            </tr>
            <xsl:apply-templates select="Member"/>
        </table>
    </xsl:template>

    <xsl:template match="Member">
        <tr>
            <td><xsl:value-of select="Name"/></td>
            <xsl:if test="$containsClub"><td><xsl:value-of select="Club"/></td></xsl:if>
            <xsl:if test="$containsSection"><td><xsl:value-of select="Section"/></td></xsl:if>
            <td><xsl:apply-templates select="Address"/></td>
            <td><xsl:value-of select="Telephone"/></td>
            <td><xsl:value-of select="ShuNumber"/></td>
        </tr>
    </xsl:template>

    <xsl:template match="Line">
        <xsl:value-of select="."/>
        <xsl:if test="count(following-sibling::Line) > 0"><br/></xsl:if>
    </xsl:template>
</xsl:stylesheet>
