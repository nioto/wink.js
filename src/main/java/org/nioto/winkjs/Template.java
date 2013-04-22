package org.nioto.winkjs;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Simple templating class. 
 * Convert all  values  like "[key]" with the value of key in a map.
 * If no value is found for a key, the segment is not modified.
 * 
 * Support for simple tests on empty values.
 * 
 *  ex : 
 *   String tmpl =" [key] is [!empty(key)] not[/empty] null";
 *   Map<String,String> map = new HashMap<String,String>();
 *   == > result :  " [key] is null"
 *     
 *   map.put( key, "content' ); 
 *   == > result :  " content is not null"  
 * 
 * @author nioto
 */
public class Template {

	private static final Logger log = LoggerFactory.getLogger(Template.class);

	/**
	 * Pattern to find all the substituable srings
	 */
	private static Pattern SUBSTITUTION_PATTERN = Pattern.compile("\\[(.+?)\\]");
	/**
	 * PAttern to find all tests segments
	 */
	private static Pattern TESTS_PATTERN  = Pattern.compile("(\\[!?empty\\(([^(]+)\\)\\])(.*?)(\\[\\/empty\\])",  Pattern.DOTALL);

	private String template;

	public Template(String tmpl) {
		this.template = tmpl;
	}
	/**
	 *  From the example in {@link Matcher}
	 *  Substitute all occurences of the keys from the map, with their map's value	 *  
	 * @param data conversion map
	 * @return  a string
	 */
	public final StringBuffer substitute(Map<String, String> data) {
		Matcher matcher;
		//parse TESTS_PATTERN
		matcher = TESTS_PATTERN.matcher(this.template);
		StringBuffer sb = new StringBuffer();
		log.debug("begin TESTS_PATTERN matcher");
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
			if( log.isDebugEnabled()) {
				log.debug( "show : {}  ;  test : {} ; key : {} " , new Object[]{ show , test, key});
			}
			matcher.appendReplacement(sb, show ? Matcher.quoteReplacement( matcher.group(3) ) : "");
		}
		matcher.appendTail(sb);
		log.debug( "end TESTS_PATTERN matcher");
		// variable SUBSTITUTION_PATTERN
		matcher = SUBSTITUTION_PATTERN.matcher(sb);
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
	/**
	 *  Retrieve the data replacement for a key
	 * @param key 
	 * @param data map containing association between key and values 
	 * @return 
	 */
	protected String getReplacement(String key, Map<String, String> data) {
		return data.get(key);
	}
}