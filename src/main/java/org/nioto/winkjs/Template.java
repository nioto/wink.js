package org.nioto.winkjs;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author tonio
 * 
 */
public class Template {

	private static Pattern p = Pattern.compile("\\[(.+?)\\]");

	private String template;

	public Template(String tmpl) {
		this.template = tmpl;
	}
	/**
	 *  From the example in {@link Matcher}
	 * @param data conversion map
	 * @return return the 
	 */
	public final StringBuffer substitute(Map<String, String> data) {
		Matcher m = p.matcher(this.template);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String key = m.group(1);
			String value = getReplacement(key, data);
			if (value == null) {
				value = m.group(0);
			}
			m.appendReplacement(sb, value);
		}
		m.appendTail(sb);
		return sb;
	}

	protected String getReplacement(String key, Map<String, String> data) {
		return data.get(key);
	}
}