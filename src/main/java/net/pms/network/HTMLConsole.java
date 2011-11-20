/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.network;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaAudio;
import net.pms.dlna.DLNAMediaDatabase;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.RootFolder;

public class HTMLConsole {
	public static String servePage(String resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>PS3 Media Server HTML Console</title></head><body>");

		DLNAMediaDatabase database = PMS.get().getDatabase();
		PmsConfiguration configuration = PMS.getConfiguration();
		if (resource.equals("compact") && configuration.getUseCache()) {
			database.compact();
			sb.append("<p align=center><b>Database compacted!</b></p><br>");
		}

		if (resource.equals("scan") && configuration.getUseCache()) {
			if (!database.isScanLibraryRunning()) {
				database.scanLibrary();
			}
			if (database.isScanLibraryRunning()) {
				sb.append("<p align=center><b>Scan in progress! you can also <a href=\"stop\">stop it</a></b></p><br>");
			}
		}

		if (resource.equals("stop") && configuration.getUseCache() && database.isScanLibraryRunning()) {
			database.stopScanLibrary();
			sb.append("<p align=center><b>Scan stopped!</b></p><br>");
		}
		

		sb.append("<p align=center><img src='/images/thumbnail-256.png'><br>PS3 Media Server HTML console<br><br>Menu:<br>");
		sb.append("<a href=\"home\">Home</a><br>");
		sb.append("<a href=\"scan\">Scan folders</a><br>");
		sb.append("<a href=\"compact\">Shrink cache database (not recommended)</a>");

		if (resource.startsWith("browse/")) {
			String objId = resource.substring("browse/".length());
			final RendererConfiguration defConf = RendererConfiguration.getDefaultConf();
			final RootFolder rootFolder = PMS.get().getRootFolder(defConf);
			try {
				boolean hasObjId = (objId.trim().length() > 0);
				final DLNAResource current;
				final List<DLNAResource> list;
				if (hasObjId) {
					list =  rootFolder.getDLNAResources(objId, true, 0, 0, defConf);
					current = rootFolder.search(objId, 0, defConf);
				} else {
					rootFolder.discoverChildren();
					list = rootFolder.getChildren();
					current = rootFolder;
				}
				sb.append("<div id='filelist'>");
				if (current != null) {
					DLNAMediaAudio mediaInfo = current.getMediaAudio();
					DLNAMediaSubtitle subtitle = current.getMediaSubtitle();
					sb.append("<table><tr><td colspan='2'>File info</td></tr>")
						.append(tableRow("Name", current.getDisplayName(defConf)))
						.append(tableRow("Content Features", current.getDlnaContentFeatures()))
						.append(tableRow("Audio", mediaInfo))
						.append(tableRow("Subtitle", subtitle))
						.append("</table>");
				}
				sb.append("<table>");
				if (current != null) {
					sb.append("<tr><td><a href='/console/browse/").append(current.getParentId()).append("'>Parent</a></td></tr>");
				}
				for (DLNAResource d : list) {
					sb.append("<tr><td><a href='/console/browse/").append(d.getResourceId()).append("'>").append(d.getDisplayName(defConf)).append("</td><td>").append(d.getSystemName()).append("</td><td>").append(d.getInternalId()).append("</a></td></tr>");
				}
				sb.append("</table></div>");
				
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				e.printStackTrace();
				sb.append(sw.toString());
			} 
			
			
		}

		sb.append("</p></body></html>");
		return sb.toString();
	}
	
	private static String tableRow(Object... cells) {
		StringBuilder s = new StringBuilder();
		s.append("<tr>");
		for (Object c : cells) {
			s.append("<td>").append(c != null ? c.toString() : "<i>none</i>").append("</td>");
		}
		return s.append("</tr>").toString();
	}
}
