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

	private static Pattern substitution = Pattern.compile("\\[(.+?)\\]");

	private static Pattern tests  = Pattern.compile("(\\[!?empty\\((.+?)\\)\\])(.*)(\\[\\/empty\\])");

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
		Matcher matcher;
		//parse tests
		matcher = tests.matcher(this.template);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String test = matcher.group(1);
			String key = matcher.group(2);
			String value = getReplacement(key, data);
			boolean show;
			if( test.charAt(1)=='!') {
				show = ( value !=null && value.length()>0);
			} else {
				show = ( value ==null ||  value.length()==0);
			}
			if ( show ) {
				matcher.appendReplacement(sb, matcher.group(3));
			}
		}
		matcher.appendTail(sb);
		
		// variable substitution
		matcher = substitution.matcher(sb);
		sb = new StringBuffer();
		while (matcher.find()) {
			String key = matcher.group(1);
			String value = getReplacement(key, data);
			if (value == null) {
				value = matcher.group(0);
			}
			matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
		}
		matcher.appendTail(sb);
		return sb;
	}

	protected String getReplacement(String key, Map<String, String> data) {
		return data.get(key);
	}
}