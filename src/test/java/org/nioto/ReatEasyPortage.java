package org.nioto;

public class ReatEasyPortage {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String path = "/{id/{detail}}/full";
		System.out.println( path );
		String path2 = replaceEnclosedCurlyBraces(path );
		System.out.println( path2);
		System.out.println( path.equals(path2));
	}
	

  public static final char openCurlyReplacement = 6;
  public static final char closeCurlyReplacement = 7;

  public static String replaceEnclosedCurlyBraces(String str)
  {
     char[] chars = str.toCharArray();
     int open = 0;
     for (int i = 0; i < chars.length; i++)
     {
        if (chars[i] == '{')
        {
        	System.out.println(open);
           if (open != 0) chars[i] = openCurlyReplacement;
           open++;
        }
        else if (chars[i] == '}')
        {
           open--;
           if (open != 0)
           {
              chars[i] = closeCurlyReplacement;
           }
        }
     }
     return new String(chars);
  }

}
