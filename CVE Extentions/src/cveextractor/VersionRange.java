package cveextractor;

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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class VersionRange {

	private String firstDetectedVer = "0.0";
	private String lastDetectedVer = "";
	private String softwareName = "";
	private String generalCpeString = "";
	private Snippet fixedSoftware;
	private boolean empty;
	private boolean first;
	private boolean last;
	private boolean fixed;

	private ArrayList<NameVersionRelation> versions;

	public ArrayList<NameVersionRelation> versionList() {
		return versions;
	}

	public void updateSoftwareName(String newSoftwareName) {
		softwareName = newSoftwareName;
	}

	public void setCPE(String newCPE) {
		generalCpeString = newCPE;
	}

	public String cpe() {
		return generalCpeString;
	}

	public VersionRange() {
		empty = true;
		versions = new ArrayList<NameVersionRelation>();
		fixed = false;
		last = false;
		first = false;
	}

	public boolean hasFirst() {
		return first;
	}

	public boolean hasLast() {
		return last;
	}

	public boolean hasFix() {
		return fixed;
	}

	public void setLast(String newLast) {
		lastDetectedVer = newLast;
	}

	public void setFirst(String newFirst) {
		firstDetectedVer = newFirst;
	}

	public String firstDetectedVersion() {
		updateRelevantVersions();
		return firstDetectedVer;
	}

	public String lastDetectedVersion() {
		updateRelevantVersions();
		return lastDetectedVer;
	}

	public String fixedVersion() {
		updateRelevantVersions();
		if (fixed)
			return fixedSoftware().getText();
		else
			return "";
	}

	private void updateRelevantVersions() {
		searchFirst();
		searchLast();
		if (versions.get(versions.size() - 1).version().logicalUnitComment()
				.equals("fixed")) {
			if (versions.size() == 1) {
				firstDetectedVer = "0.0";
				lastDetectedVer = "";
			} else if (versions.size() == 2) {
				firstDetectedVer = shortest().version().getText()
						.replaceFirst("\\.x", ".0");
				lastDetectedVer = "";
			} else {
				firstDetectedVer = shortest().version().getText()
						.replaceFirst("\\.x", ".0");
				lastDetectedVer = versions.get(versions.size() - 2).version()
						.getText().replaceFirst("\\.x", ".0");
			}
		} else {
			if (versions.size() == 1) {
				if (!last && !first) {
					firstDetectedVer = versions.get(0).version().getText()
							.replaceFirst("\\.x", ".0");
					lastDetectedVer = firstDetectedVer;
				}
			} else {
				firstDetectedVer = shortest().version().getText()
						.replaceFirst("\\.x", ".0");
				lastDetectedVer = biggest().version().getText()
						.replaceFirst("\\.x", ".0");
			}

		}
	}

	public NameVersionRelation shortest() {
		return versions.get(0);
	}

	public NameVersionRelation biggest() {
		return versions.get(versions.size() - 1);
	}

	private void isFixed() {
		if (!fixed) {
			Iterator<NameVersionRelation> nvrIt = versions.iterator();
			Snippet version;
			while (nvrIt.hasNext()) {
				version = nvrIt.next().version();
				if (version.logicalUnitComment().equals("fixed")) {
					fixed = true;
					fixedSoftware = version;
					break;
				} else if (!nvrIt.hasNext())
					fixed = false;
			}
		}
	}

	private void searchLast() {
		Iterator<NameVersionRelation> nvrIt = versions.iterator();
		Snippet version;
		while (nvrIt.hasNext()) {
			version = nvrIt.next().version();
			if (version.logicalUnitComment().equals(
					"last detected vulnerability")) {
				last = true;
				lastDetectedVer = version.getText();
			}
		}
	}

	private void searchFirst() {
		Iterator<NameVersionRelation> nvrIt = versions.iterator();
		Snippet version;
		while (nvrIt.hasNext()) {
			version = nvrIt.next().version();
			if (version.logicalUnitComment().equals(
					"first detected vulnerability")) {
				first = true;
				firstDetectedVer = version.getText();
				break;
			}
		}
	}

	public boolean fixed() {
		return fixed;
	}

	public Snippet fixedSoftware() {
		return fixedSoftware;
	}

	public void add(NameVersionRelation nvr) {
		if (empty) {
			softwareName = nvr.name().getText();
			empty = false;
		}
		versions.add(nvr);
		Collections.sort(versions);
		isFixed();
	}

	public void addAll(Collection<NameVersionRelation> c) {
		if (c.size() > 0) {
			if (empty) {
				softwareName = c.iterator().next().name().getText();
				empty = false;
			}
			versions.addAll(c);
			Collections.sort(versions);
			isFixed();
		}
	}

	public String firstXMLTag() {
		StringBuilder sb = new StringBuilder();
		sb.append("\t\t\t<");
		sb.append(Konfig.xmlExtensionTag);
		sb.append(":");
		sb.append("start>");
		sb.append(generalCpeString);
		sb.append(firstDetectedVersion());
		sb.append("</");
		sb.append(Konfig.xmlExtensionTag);
		sb.append(":");
		sb.append("start>");
		return sb.toString();
	}

	public String lastXMLTag() {
		StringBuilder sb = new StringBuilder();
		sb.append("\t\t\t<");
		sb.append(Konfig.xmlExtensionTag);
		sb.append(":");
		sb.append("end>");
		sb.append(generalCpeString);
		sb.append(lastDetectedVersion());
		sb.append("</");
		sb.append(Konfig.xmlExtensionTag);
		sb.append(":");
		sb.append("end>");
		return sb.toString();
	}

	public String fixedXMLTag() {
		StringBuilder sb = new StringBuilder();
		sb.append("\t\t\t<");
		sb.append(Konfig.xmlExtensionTag);
		sb.append(":");
		sb.append("fix>");
		sb.append(generalCpeString);
		sb.append(fixedVersion());
		sb.append("</");
		sb.append(Konfig.xmlExtensionTag);
		sb.append(":");
		sb.append("fix>");
		return sb.toString();
	}

	public String getXMLRange(){
		StringBuilder sb = new StringBuilder();
		sb.append("\t\t<");
		sb.append(Konfig.xmlExtensionTag);
		sb.append(":");
		sb.append("range>\n");
		
		if(!firstDetectedVer.equals("0.0")){
			sb.append(firstXMLTag());
			sb.append("\n");
			}
		
		if(!lastDetectedVer.isEmpty()){
			sb.append(lastXMLTag());
			sb.append("\n");
			}
		
		if(!fixedVersion().isEmpty()){
			sb.append(fixedXMLTag());
			sb.append("\n");
			}
		
		sb.append("\t\t</");
		sb.append(Konfig.xmlExtensionTag);
		sb.append(":");
		sb.append("range>\n");
		return sb.toString();
	}
	
	public String toString() {
		if (softwareName.isEmpty())
			softwareName = generalCpeString;
		String returnStr = softwareName + " vulnerable between "
				+ firstDetectedVersion() + " and " + lastDetectedVersion();
		if (fixed)
			returnStr += " fix:" + fixedVersion();
		else
			returnStr += " no fix found";
		if (!generalCpeString.isEmpty() && Konfig.Testmode)
			returnStr += " |  " + generalCpeString;
		return returnStr;

	}

}