package com.orange.signsatwork.biz.security;

import java.util.regex.Pattern;


public class ClearXss {


    public static String cleanFormString(String value) {
      String cleanedValue = removeUnwantedUnicodes(value);
      cleanedValue = replaceDangerousUnicodes(cleanedValue);
      return cleanedValue;
    }

    // See unicode characters table: https://fr.wikipedia.org/wiki/Table_des_caract%C3%A8res_Unicode_(0000-0FFF)
    // u000A = includes \n, u0152 = includes Œ (and œ)
    private static final Pattern unicodeOutliers = Pattern.compile("[^\\u0020-\\u007E^\\u00A0-\\u00FF^\\u000A^\\u0152]",
      Pattern.UNICODE_CASE | Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE);

    // needed to pass tests with emoji in java 11: emoji are now coded on 2 characters...
    // see https://www.baeldung.com/java-string-remove-emojis + reference https://unicode.org/Public/emoji/14.0/emoji-sequences.txt
    private static final Pattern emojiOutliers = Pattern.compile("[\\x{1000}-\\x{1FFFF}]");

    private static String removeUnwantedUnicodes(String input) {
      if (input == null) {
        return null;
      }

      // ; is unwanted in csv exports...
      input = input.replaceAll(";", ",");

      // keep some punctuations seen in administrative levels files
      input = input.replaceAll("’", "'");

      // avoiding database errors on persist
      // (in this order...)
      input = emojiOutliers.matcher(input).replaceAll("");
      return unicodeOutliers.matcher(input).replaceAll("");
    }

    // see OssSecurityFilter
    private static final Pattern javascriptInjectionPattern = Pattern.compile("(?i)(&|\\<|\\>|javascript|redirect)");

    private static String replaceDangerousUnicodes(String input) {
      if (input == null) {
        return null;
      }

      // \ is dangerous for SQL injection
      input = input.replaceAll("\\\\", "/");

      // for XSS and javascript injection, see OssSecurityFilter.javascriptInjectionPattern
      return javascriptInjectionPattern.matcher(input).replaceAll("¿");
    }


}
