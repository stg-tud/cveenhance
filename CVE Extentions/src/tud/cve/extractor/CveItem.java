package tud.cve.extractor;

/*
 * ============ CREATIVE COMMONS LICENSE (CC BY 4.0) ============
 * This work is licensed under the Creative Commons Attribution 4.0 International License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by/4.0/. 
 *  
 * authors: Technische Universit�t Darmstadt - Multimedia Communication Lab (KOM), Technische Universit�t Darmstadt - Software Technology Group (STG)
 * websites: http://www.kom.tu-darmstadt.de/, http://www.stg.tu-darmstadt.de/
 * contact: Leonid Glanz (STG), Sebastian Schmidt (KOM), Sebastian Wollny (KOM), Ben Hermann (STG)
 * name: CVE Version Information Extractor
 *
 */

/**
 * >> This class is an abstract representation of a CVE XML file.<<
 * The extraction algorithm is part of this class and information is generated inside instances of this class.
 * @author TU Darmstadt KOM, TU Darmstadt STG
 * @version 0.1
 */

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import tud.cve.data.representation.Snippet;

/**
 * >> On object of this class represents a CVE Entry <<
 * 
 * @author TU Darmstadt KOM, TU Darmstadt STG
 * @version 0.1
 */

public class CveItem {
	public CveItem(String xmlInput) {
		xmlCode = xmlInput;
		initialise();
	}

	public final String xmlCode; // plain unmodified content of a CVE file

	protected Document xmlDocument; // XPath Document of CVE entry

	protected XPath xpath; // XPath object

	protected Vector<Snippet> tokenList;

	protected String cveSummary;

	/**
	 * initialization of a CVE item
	 */
	protected void initialise() {
		try {

			InputSource source = new InputSource(new StringReader(xmlCode));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			xmlDocument = db.parse(source);
			XPathFactory xpathFactory = XPathFactory.newInstance();
			xpath = xpathFactory.newXPath();
			String summary = xpath.evaluate("//entry/summary/text()", xmlDocument);
			cveSummary = summary;

		} catch (Exception e) {
			e.printStackTrace();
		}
		Vector<Snippet> description = getTokens();
		tokenList = description;
		Iterator<Snippet> tokenIterator = tokenList.iterator();
		while (tokenIterator.hasNext())
			tokenIterator.next().combine();
		rebuildTokenVector();
		searchSnippetcontext();
	}

	/**
	 * Refinement of a snippet entity
	 */
	private void searchSnippetcontext() {
		Iterator<Snippet> tokenIterator = tokenList.iterator();
		Snippet curSnip;
		while (tokenIterator.hasNext()) {
			curSnip = tokenIterator.next();
			try {
				if (curSnip.hasLogicalType()) {
					if (curSnip.hasPrev()) {
						if (curSnip.prev.condition("!cuebefore"))
							curSnip.setLogicalUnitComment("fixed");
						if (curSnip.prev.condition("!cuebegin"))
							curSnip.setLogicalUnitComment("first detected vulnerability");
						if (curSnip.prev.condition("!cueearlier"))
							curSnip.setLogicalUnitComment("last detected vulnerability");
						if (curSnip.prev.condition("!cuebetween")) {
							curSnip.setLogicalUnitComment("first detected vulnerability");
							Snippet scanSnip = curSnip;
							int distance = 0;
							while (scanSnip.hasNext() && !scanSnip.islogicalEnd() && distance < Config.SEARCH_DISTANCE) {
								scanSnip = scanSnip.next;
								distance += scanSnip.value();
								if (scanSnip.isLogicalType("version")) {
									scanSnip.setLogicalUnitComment("last detected vulnerability");
								}
							}
						}
						if (curSnip.prev.condition("!comparingword") && curSnip.prev.hasPrev()) {
							if (curSnip.prev.prev.condition("!cueearlier"))
								curSnip.setLogicalUnitComment("last detected vulnerability");
							if (curSnip.prev.prev.condition("!cuebegin"))
								curSnip.setLogicalUnitComment("first detected vulnerability");
						}

					}
					if (curSnip.hasNext() && curSnip.next.hasNext()) {
						if (curSnip.next.condition("!concatword") && curSnip.next.next.condition("!cueearlier"))
							curSnip.setLogicalUnitComment("last detected vulnerability");
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * XPath interface
	 * 
	 * @param command
	 *            XPath evaluation command
	 * @return XPath result string
	 */
	protected String xpathRequest(String command) {
		try {
			return xpath.evaluate(command, xmlDocument);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * @return XPath instance
	 */
	public XPath xPath() {
		return xpath;
	}

	/**
	 * @return XPath Document
	 */
	public Document XmlDocument() {
		return xmlDocument;
	}

	// protected String xpathFromString(String source, String command) {
	// try {
	// InputSource inputSource = new InputSource(new StringReader(source));
	// DocumentBuilderFactory docbuildfac = DocumentBuilderFactory
	// .newInstance();
	// DocumentBuilder docbuild;
	// docbuild = docbuildfac.newDocumentBuilder();
	// Document XmlDoc = docbuild.parse(inputSource);
	// XPathFactory xpathFactory = XPathFactory.newInstance();
	// XPath xpa = xpathFactory.newXPath();
	// return xpa.evaluate(command, XmlDoc);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return "";
	// }

	/**
	 * @return CVE-ID string
	 */
	public String getCVEID() {
		return tagSubstr("vuln:cve-id").firstElement()[1].replaceAll("\\<.*?\\>", "");
	}

	/**
	 * @return token list of CVE summary
	 */
	public Vector<Snippet> getTokens() {
		StringTokenizer st = new StringTokenizer(cveSummary);
		Vector<Snippet> tokens = new Vector<Snippet>();
		Snippet lastSnip = null;
		Snippet curSnip = null;

		while (st.hasMoreTokens()) {
			lastSnip = curSnip;
			curSnip = new Snippet(st.nextToken());
			if (lastSnip != null) {
				lastSnip.next = curSnip;
				curSnip.prev = lastSnip;
			}
			curSnip.initialize();
			tokens.add(curSnip);
		}
		return tokens;
	}

	/**
	 * Searches a Software before a Snippet
	 * 
	 * @param versionSnippet
	 *            version entity snippet
	 * @return corresponding name snippet
	 */
	public Snippet searchSoftwareNameBefore(Snippet versionSnippet) {
		Snippet curSnip = versionSnippet;
		Snippet Softwarename = null;
		int distance = 0;
		while (distance < Config.SEARCH_DISTANCE && curSnip.hasPrev()) {
			curSnip = curSnip.prev;
			if (curSnip.logicalType() != null && curSnip.logicalType().equals("softwarename")) {
				Softwarename = curSnip;
				break;
			} else {
				distance += curSnip.getTokenValue();
				if (curSnip.logicalType() != null && curSnip.logicalType().equals("version"))
					distance = 0;
			}
		}
		if (Softwarename == null)
			Softwarename = new Snippet("");
		return Softwarename;
	}

	/**
	 * Returns the content surrounded by a xml tag with tagname type.
	 * 
	 * @param tagname
	 *            type of an xml tag
	 * @return result of content surrounded by tags of tagname type
	 */

	public Vector<String[]> tagSubstr(String tagname) {
		// german: Liefert den Inhalt zwischen einem �ffnenden und einem
		// schlie�enden Tag des Typs tagname
		Vector<String[]> vec = new Vector<String[]>();
		String[] partresult = null;
		String innerNoTagText;
		Matcher ma, mo;
		ma = Pattern.compile("<" + tagname + ">").matcher(xmlCode.toLowerCase());
		mo = Pattern.compile("</" + tagname + ">").matcher(xmlCode.toLowerCase());
		while (ma.find()) {
			if (mo.find(ma.end())) {
				partresult = new String[2];
				partresult[0] = tagname;
				innerNoTagText = StringEscapeUtils.unescapeXml(xmlCode.substring(ma.start(), mo.end())); // .replaceAll("\\<.*?\\>",
																											// "")
				innerNoTagText = innerNoTagText.replaceAll("[\\t\\n\\f\\r]", "");
				partresult[1] = innerNoTagText;
				vec.add(partresult);
			} else {
				System.out.println("FEHLER: Der Tag </" + tagname + "> konnte nicht gefunden werden!");
			}
		}
		return vec;
	}

	public void rebuildTokenVector() {
		Vector<Snippet> newtokenList = new Vector<Snippet>();
		Snippet curSnip = tokenList.firstElement();
		newtokenList.add(curSnip);
		while (curSnip.hasNext()) {
			curSnip = curSnip.next;
			newtokenList.add(curSnip);
		}
	}

	Vector<Snippet> getSnippetsWithLogicalUnits(String logicalUnitType) {
		Vector<Snippet> returnVec = new Vector<Snippet>();
		Snippet curSnip = tokenList.firstElement();
		if (curSnip.isLogicalType(logicalUnitType))
			returnVec.add(curSnip);
		while (curSnip.hasNext()) {
			curSnip = curSnip.next;
			if (curSnip.isLogicalType(logicalUnitType)) {
				returnVec.add(curSnip);
			}
		}

		return returnVec;
	}

}