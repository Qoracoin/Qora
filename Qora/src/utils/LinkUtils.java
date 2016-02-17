package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

public class LinkUtils {
	
	public static List<Pair<String, String>> createHtmlLinks(List<String> links) {
		List<Pair<String, String>> result = new ArrayList<>();
		for (String link : links) {
			String refurbishedlink = StringEscapeUtils.unescapeHtml4(link);
			String youtubeWatchRegex = "https?" 
					+ Pattern.quote("://www.youtube.com/watch?v=")
					+ "([a-zA-Z0-9_-]+).*";
			String youTubeSlashRegex = "https?" 
					+ Pattern.quote("://youtu.be/")
					+ "([a-zA-Z0-9_-]+).*";
			if (refurbishedlink.toLowerCase().matches(youtubeWatchRegex)) {
				String vid = refurbishedlink
						.replaceAll(youtubeWatchRegex, "$1");

				result.add(new Pair<String, String>(link,
						getYoutubeEmbedHtml(vid)));
			} else if (refurbishedlink.toLowerCase().matches(youTubeSlashRegex)) {
				String vid = refurbishedlink
						.replaceAll(youTubeSlashRegex, "$1");

				result.add(new Pair<String, String>(link,
						getYoutubeEmbedHtml(vid)));
			} else {
				refurbishedlink = transformURLIntoLinks(refurbishedlink);
				result.add(new Pair<String, String>(link, refurbishedlink));
			}

		}

		return result;
	}
	
	public static String getYoutubeEmbedHtml(String vid) {
		
	   
		
		return "<div class=\"youtube-container\">\n<div class=\"youtube-player\" data-id="+vid+"></div>\n</div>";
	}
	
	public static String transformURLIntoLinks(String text) {
		String urlValidationRegex = "(https?|ftp)://(www\\d?|[a-zA-Z0-9]+)?.[a-zA-Z0-9-]+(\\:|.)([a-zA-Z0-9.-]+|(\\d+)?)([/?:].*)?";
		Pattern p = Pattern.compile(urlValidationRegex);
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String found = m.group(0);
			m.appendReplacement(sb, "<a target=\"_blank\" href='" + found + "'>" + found + "</a>");
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public static ArrayList<String> getAllLinks(String text) {
		ArrayList<String> links = new ArrayList<>();

		String regex = "\\(?\\b(http(s?)://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		while (m.find()) {
			String urlStr = m.group();
			if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
				urlStr = urlStr.substring(1, urlStr.length() - 1);
			}
			links.add(urlStr);
		}
		return links;
	}
}
