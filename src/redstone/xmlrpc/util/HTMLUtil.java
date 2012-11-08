package redstone.xmlrpc.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML工具
 * 
 */
public class HTMLUtil {

	// >
	public static final String GT = "&gt;";
	// <
	public static final String LT = "&lt;";
	// "
	public static final String QUOT = "&quot;";
	// &
	public static final String AMP = "&amp;";
	// 空格
	public static final String SPACE = "&nbsp;";
	// ©
	public static final String COPYRIGHT = "&copy;";
	// ®
	public static final String REG = "&reg;";
	// ™
	public static final String TM = "&trade;";
	// ¥
	public static final String RMB = "&yen;";

	private static Pattern scriptPattern;
	private static Pattern noscriptPattern;
	private static Pattern stylePattern;
	private static Pattern titlePattern;
	private static Pattern tagPattern;

	static {
		// script
		String scriptRegex = "<script[^>]*?>[\\s\\S]*?<\\/script>";
		scriptPattern = Pattern.compile(scriptRegex, Pattern.CASE_INSENSITIVE);
		// noscript
		String noscriptRegex = "<noscript[^>]*?>[\\s\\S]*?<\\/noscript>";
		noscriptPattern = Pattern.compile(noscriptRegex,
				Pattern.CASE_INSENSITIVE);
		// style
		String styleRegex = "<style[^>]*?>[\\s\\S]*?<\\/style>";
		stylePattern = Pattern.compile(styleRegex, Pattern.CASE_INSENSITIVE);
		// title
		String titleRegex = "<title[^>]*?>[\\s\\S]*?<\\/title>";
		titlePattern = Pattern.compile(titleRegex, Pattern.CASE_INSENSITIVE);
		// html tag
		String tagRegex = "<[^>]+>";
		tagPattern = Pattern.compile(tagRegex, Pattern.CASE_INSENSITIVE);

	}

	/**
	 * 删除noscript标签
	 * 
	 * @param str
	 * @return
	 */
	public static String delNoscriptTag(String str) {
		Matcher scriptMatcher = noscriptPattern.matcher(str);
		str = scriptMatcher.replaceAll("");
		return str.trim();
	}

	/**
	 * 删除script标签
	 * 
	 * @param str
	 * @return
	 */
	public static String delScriptTag(String str) {
		Matcher scriptMatcher = scriptPattern.matcher(str);
		str = scriptMatcher.replaceAll("");
		return str.trim();
	}

	/**
	 * 删除style标签
	 * 
	 * @param str
	 * @return
	 */
	public static String delStyleTag(String str) {
		Matcher styleMatcher = stylePattern.matcher(str);
		str = styleMatcher.replaceAll("");
		return str;
	}

	/**
	 * 删除style标签
	 * 
	 * @param str
	 * @return
	 */
	public static String delTitleTag(String str) {

		Matcher titleMatcher = titlePattern.matcher(str);
		str = titleMatcher.replaceAll("");
		return str;
	}

	/**
	 * 删除HTML标签
	 * 
	 * @param str
	 * @return
	 */
	public static String delHTMLTag(String str) {
		Matcher tagMatcher = tagPattern.matcher(str);
		str = tagMatcher.replaceAll("");
		return str;
	}

	/**
	 * 删除HTML标签
	 * 
	 * @param str
	 * @return
	 */
	public static String delNullTag(String str) {
		str = str.replaceAll(" ", "");
		str = str.replaceAll("	", "");
		return str;
	}

	/**
	 * 删除所有标签
	 * 
	 * @param str
	 * @return
	 */
	public static String delAllTag(String str) {
		// 删noscript
		str = delNoscriptTag(str);
		// 删script
		str = delScriptTag(str);
		// 删style
		str = delStyleTag(str);
		// 删title
		str = delTitleTag(str);
		// 删HTML
		str = delHTMLTag(str);
		//
		str = delNullTag(str);
		return str;
	}

	/**
	 * 清除标签,恢复HTML转义字符
	 * 
	 * @param str
	 * @return
	 */
	public static String clean(String str) {
		str = delAllTag(str);
		str = str.replaceAll(SPACE, " ");
		str = str.replaceAll(GT, ">");
		str = str.replaceAll(LT, "<");
		str = str.replaceAll(QUOT, "\"");
		str = str.replaceAll(AMP, "&");
		str = str.replaceAll(COPYRIGHT, "©");
		str = str.replaceAll(REG, "®");
		str = str.replaceAll(TM, "™");
		str = str.replaceAll(RMB, "¥");
		return str;
	}

	/**
	 * 恢复HTML转义字符
	 * 
	 * @param str
	 * @return
	 */
	public static String recoverHtml(String str) {
		str = str.replaceAll(SPACE, " ");
		str = str.replaceAll(GT, ">");
		str = str.replaceAll(LT, "<");
		str = str.replaceAll(QUOT, "\"");
		str = str.replaceAll(AMP, "&");
		str = str.replaceAll(COPYRIGHT, "©");
		str = str.replaceAll(REG, "®");
		str = str.replaceAll(TM, "™");
		str = str.replaceAll(RMB, "¥");
		return str;
	}

	/**
	 * 过滤指定标签
	 * 
	 * @param str
	 * @param tag
	 *            指定标签
	 * @return String
	 */
	public static String fiterHtmlTag(String str, String tag) {
		String regxp = "<\\s*" + tag + "\\s+([^>]*)\\s*>";
		Pattern pattern = Pattern.compile(regxp);
		Matcher matcher = pattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		boolean result1 = matcher.find();
		while (result1) {
			matcher.appendReplacement(sb, "");
			result1 = matcher.find();
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 替换指定的标签
	 * 
	 * @param str
	 * @param beforeTag
	 *            要替换的标签
	 * @param tagAttrib
	 *            要替换的标签属性值
	 * @param startTag
	 *            新标签开始标记
	 * @param endTag
	 *            新标签结束标记
	 * @return String example: 替换img标签的src属性值为[img]属性值[/img]
	 */
	public static String replaceHtmlTag(String str, String beforeTag,
			String tagAttrib, String startTag, String endTag) {
		String regxpForTag = "<\\s*" + beforeTag + "\\s+([^>]*)\\s*>";
		String regxpForTagAttrib = tagAttrib + "=\"([^\"]+)\"";
		Pattern patternForTag = Pattern.compile(regxpForTag);
		Pattern patternForAttrib = Pattern.compile(regxpForTagAttrib);
		Matcher matcherForTag = patternForTag.matcher(str);
		StringBuffer sb = new StringBuffer();
		boolean result = matcherForTag.find();
		while (result) {
			StringBuffer sbreplace = new StringBuffer();
			Matcher matcherForAttrib = patternForAttrib.matcher(matcherForTag
					.group(1));
			if (matcherForAttrib.find()) {
				matcherForAttrib.appendReplacement(sbreplace, startTag
						+ matcherForAttrib.group(1) + endTag);
			}
			matcherForTag.appendReplacement(sb, sbreplace.toString());
			result = matcherForTag.find();
		}
		matcherForTag.appendTail(sb);
		return sb.toString();
	}

}