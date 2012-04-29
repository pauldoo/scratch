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
        method="text"
        media-type="text/csv"
        />

    <xsl:variable name="containsClub" select="count(//MemberList/Member/Club) > 0"/>
    <xsl:variable name="containsSection" select="count(//MemberList/Member/Section) > 0"/>
    <xsl:variable name="newline">
        <xsl:text>
</xsl:text>
    </xsl:variable>

    <xsl:template match="/MembersReport"><xsl:apply-templates select="MemberList"/></xsl:template>

    <xsl:template match="MemberList">
        <xsl:text>Name,</xsl:text>
        <xsl:if test="$containsClub"><xsl:text>Club,</xsl:text></xsl:if>
        <xsl:if test="$containsSection"><xsl:text>Section,</xsl:text></xsl:if>
        <xsl:text>Address,Telephone,SHU Number</xsl:text>
        <xsl:value-of select="$newline"/>
        <xsl:apply-templates select="Member"/>
    </xsl:template>

    <xsl:template match="Member">
        <xsl:value-of select="Name"/>
        <xsl:text>,</xsl:text>
        <xsl:if test="$containsClub"><xsl:value-of select="Club"/><xsl:text>,</xsl:text></xsl:if>
        <xsl:if test="$containsSection"><xsl:value-of select="Section"/><xsl:text>,</xsl:text></xsl:if>
        <xsl:text>"</xsl:text><xsl:apply-templates select="Address/Line"/><xsl:text>",</xsl:text>
        <xsl:value-of select="Telephone"/><xsl:text>,</xsl:text>
        <xsl:value-of select="ShuNumber"/>
        <xsl:value-of select="$newline"/>
    </xsl:template>

    <xsl:template match="Line">
        <xsl:value-of select="."/>
        <xsl:if test="count(following-sibling::Line) > 0"><xsl:value-of select="$newline"/></xsl:if>
    </xsl:template>
</xsl:stylesheet>
