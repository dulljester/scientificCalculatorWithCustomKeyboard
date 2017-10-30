package com.solutions.sj.kazi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sj on 08/03/17.
 */

public class MyUtils {
    public static final Pattern argPattern = Pattern.compile("([_A-Za-z][0-9]*)\\s*:\\s*([-+0-9.]+)");
    public static final double tol = 1e-9;
    public static final int oo = (1<<29);
    private static final Pattern funcs = Pattern.compile("(sin|cos|tan|log|asin|acos|atan|exp|sqrt)(\\s*\\(\\s*[^)\\s]+\\s*\\)\\s*)");
    public static final int EOF = -1;
    public static boolean isVariableStart( int ch ) {
        return ch == '_' || 'A' <= ch && ch <= 'Z' || 'a' <= ch && ch <= 'z';
    }
    public static boolean insideVar( int ch ) {
        return ch == '_' || 'A' <= ch && ch <= 'Z' || 'a' <= ch && ch <= 'z' || '0' <= ch && ch <= '9';
    }
}
