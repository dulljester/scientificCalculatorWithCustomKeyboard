package com.solutions.sj.kazi;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by kazi on 3/15/2017.
 */
/*
 Pattern:
         PositivePattern
         PositivePattern ; NegativePattern
 PositivePattern:
         Prefixopt Number Suffixopt
 NegativePattern:
         Prefixopt Number Suffixopt
 Prefix:
         any Unicode characters except \uFFFE, \uFFFF, and special characters
 Suffix:
         any Unicode characters except \uFFFE, \uFFFF, and special characters
 Number:
         Integer Exponentopt
         Integer . Fraction Exponentopt
 Integer:
         MinimumInteger
         #
         # Integer
         # , Integer
 MinimumInteger:
         0
         0 MinimumInteger
         0 , MinimumInteger
 Fraction:
         MinimumFractionopt OptionalFractionopt
 MinimumFraction:
         0 MinimumFractionopt
 OptionalFraction:
         # OptionalFractionopt
 Exponent:
         E MinimumExponent
 MinimumExponent:
         0 MinimumExponentopt
 */
    /*
     * formats a decimal number with a sign (needed for displaying the polynomial)
     * removes trailing superfluous zeroes
     * otherwise, prints the number with 7 digits after decimal point
     */
public class MyDecimalFormatter {
    private static DecimalFormat df;
    static {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(' ');
        df = new DecimalFormat("+#.#######;-#.#######",otherSymbols);
    }
    public static String format( double x ) {
        if ( Math.abs(x) < 1e-15 )
            return df.format(0.00);
        return df.format(x);
    }
}
