package com.label.practice.files.trans.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import com.google.common.collect.Sets;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {
  public static Pattern MD5_PATTERN = Pattern.compile("([a-f\\d]{32}|[A-F\\d]{32})");

  public static final String NORM_DATE = "[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])";
  public static final String PURE_DATE = "[1-9]\\d{3}(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])";
  public static final String NORM_TIME = "(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d";
  public static final String PURE_TIME = "(20|21|22|23|[0-1]\\d)[0-5]\\d[0-5]\\d";
  public static Pattern NORM_DATE_PATTERN = Pattern.compile(NORM_DATE);
  public static Pattern NORM_DATETIME_PATTERN = Pattern.compile(NORM_DATE + "\\s+" + NORM_TIME);
  public static Pattern PURE_DATE_PATTERN = Pattern.compile(PURE_DATE);
  public static Pattern PURE_DATETIME_PATTERN = Pattern.compile(PURE_DATE + PURE_TIME);
  public static Pattern PURE_DATE_TIME_PATTERN = Pattern.compile(PURE_DATE + "_" + PURE_TIME);

  public static Set<String> listRegexes(String originalString, Pattern pattern) {
    String searchString = originalString;
    Matcher dateMatcher = pattern.matcher(searchString);
    Set<String> regexes = Sets.newHashSet();
    //    int beEndIndex = 0;
    while (dateMatcher.find()) {
      String subString = dateMatcher.group();
      regexes.add(subString);
      //      System.out.print("子串:"+subString+"  ");
      //      int subIndex = searchString.indexOf(subString);
      //      System.out.print("位置:"+(subIndex + beEndIndex)+"  ");
      //      int subLength = subString.length();
      //      System.out.println("长度:"+subLength);
      //      beEndIndex = subIndex + subLength + beEndIndex;
      //      searchString = originalString.substring(beEndIndex);
      //      dateMatcher = pattern.matcher(searchString);
    }
    return regexes;
  }

  public static DateTime getDateTime(String str) {
    Set<String> dateTimes = listRegexes(str, NORM_DATETIME_PATTERN);
    if (!dateTimes.isEmpty()) {
      return parseDateTime(dateTimes.stream().findFirst().get());
    }

    dateTimes = listRegexes(str, PURE_DATE_TIME_PATTERN);
    if (!dateTimes.isEmpty()) {
      return parseDateTime(dateTimes.stream().findFirst().get());
    }

    dateTimes = listRegexes(str, PURE_DATETIME_PATTERN);
    if (!dateTimes.isEmpty()) {
      return parseDateTime(dateTimes.stream().findFirst().get());
    }

    dateTimes = listRegexes(str, NORM_DATE_PATTERN);
    if (!dateTimes.isEmpty()) {
      return parseDateTime(dateTimes.stream().findFirst().get());
    }

    dateTimes = listRegexes(str, PURE_DATE_PATTERN);
    if (!dateTimes.isEmpty()) {
      return parseDateTime(dateTimes.stream().findFirst().get());
    }

    return null;
  }

  public static DateTime parseDateTime(String str) {
    DateTime dateTime = of(str, DatePattern.NORM_DATETIME_PATTERN);
    if (Objects.nonNull(dateTime)) {
      return dateTime;
    }
    dateTime = of(str, DatePattern.NORM_DATE_PATTERN);
    if (Objects.nonNull(dateTime)) {
      return dateTime;
    }
    dateTime = of(str, DatePattern.PURE_DATETIME_PATTERN);
    if (Objects.nonNull(dateTime)) {
      return dateTime;
    }
    dateTime = of(str, "yyyyMMdd_HHmmss");
    if (Objects.nonNull(dateTime)) {
      return dateTime;
    }
    dateTime = of(str, DatePattern.PURE_DATE_PATTERN);
    if (Objects.nonNull(dateTime)) {
      return dateTime;
    }

    return dateTime;
  }

  private static DateTime of(String str, String normDatetimePattern) {
    try {
      return DateTime.of(str, normDatetimePattern);
    } catch (Exception e) {
      return null;
    }
  }
}
