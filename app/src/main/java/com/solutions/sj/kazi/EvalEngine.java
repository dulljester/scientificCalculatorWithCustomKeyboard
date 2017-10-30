package com.solutions.sj.kazi; /**
 * NOTES: the grammar is left-associative with the exception of "^", which is right-associative
 */

/**
 * Created by sj on 13/03/17.
 */

import android.util.Log;
import android.widget.Toast;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.*;

class _Polynomial {
    private final static double tol = 1e-10;
    private double []c;
    int n;
    public double coeff( final int idx ) {
        if ( idx < 0 || idx >= c.length )
            return 0.00;
        return c[idx];
    }
    _Polynomial( double []d ) {
        n = d.length;
        c = new double[n];
        for ( int i = 0; i < n; c[i] = d[i], ++i );
        for ( ;n >= 1 && Math.abs(c[n-1]) < tol; --n );
    }
    public int deg() {
        return n-1;
    }
    public boolean isZero() {
        return deg() == -1;
    }
    _Polynomial add( _Polynomial other ) {
        int m = Math.max(deg(),other.deg())+1;
        double []d = new double[m];
        for ( int i = 0; i <= deg(); d[i] = c[i], ++i ) ;
        for ( int i = 0; i <= other.deg(); d[i] += other.coeff(i), ++i ) ;
        return new _Polynomial(d);
    }
    _Polynomial sub( _Polynomial other ) {
        int m = Math.max(deg(), other.deg()) + 1;
        double[] d = new double[m];
        for (int i = 0; i <= deg(); d[i] = c[i], ++i) ;
        for (int i = 0; i <= other.deg(); d[i] -= other.coeff(i), ++i) ;
        return new _Polynomial(d);
    }
    _Polynomial mult( _Polynomial other ) {
        int m = deg()+other.deg()+1;
        double []d = new double[m];
        for ( int i = 0; i <= deg(); ++i )
            for ( int j = 0; j <= other.deg(); ++j )
                d[i+j] += coeff(i)*other.coeff(j);
        return new _Polynomial(d);
    }
    public _Polynomial div( double t ) {
        double []d = new double[n];
        for ( int i = 0; i < n; d[i] = c[i]/t, ++i ) ;
        return new _Polynomial(d);
    }
    public _Polynomial pow( int n ) {
        _Polynomial ax = new _Polynomial(new double[]{1.00}), x = new _Polynomial(this.c);
        int sign = 1;
        if ( n < 0 ) { n = -n; sign = -1; }
        for ( ;n>0; n >>= 1, x = x.mult(x) )
            if ( 1 == (n&1) )
                ax = ax.mult(x);
        if ( sign < 0 )
            ax.c[0] = 1/ax.c[0];
        return ax;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int written = 0;
        for ( int i = n-1; i >= 0; --i ) {
            if ( Math.abs(c[i]) < 1e-9 )
                continue ;
            if ( Math.abs(c[i]-1.00) < tol ) {
                if ( i > 0 ) sb.append(written>0?"+x":"x");
                else sb.append(written>0?"+1":"1");
            }
            else if ( Math.abs(c[i]+1.00) < tol ) {
                if ( i > 0 ) {
                    if ( written == 0 ) sb.append("-x");
                    else sb.append("-x");
                }
                else {
                    if ( written == 0 ) sb.append("-1");
                    else sb.append("-1");
                }
            }
            else {
                if ( written == 0 )
                    sb.append(String.format("%s%s",MyDecimalFormatter.format(c[i]),i==0?"":"x"));
                else
                    sb.append(String.format("%s%s",MyDecimalFormatter.format(c[i]),i==0?"":"x"));
            }
            if ( i >= 2 )
                sb.append("^{"+i+"}");
            ++written;
        }
        String res = sb.toString();
        return res.charAt(0)=='+'?res.substring(1):res;
    }

    public boolean isConstant() {
        return deg() == 0;
    }
}

enum TokenType {
    LB("("),RB(")"),PLUS("+"),MINUS("-"),STAR("*"),DIV("/"),CARET("^"),VARIABLE(null),NUMBER(null),FUNC(null),NONE("");
    private final String ex;
    private static Map<String,TokenType> m = new HashMap<>();
    TokenType( String s ) {ex=s;}
    public static TokenType which( String s ) {
        if ( m.containsKey(s) )
            return m.get(s);
        for ( TokenType t: values() )
            if ( t.ex != null && t.ex.equals(s) ) {
                m.put(s,t);
                return t;
            }
        return NONE;
    }
}

class _Tokenizer {
    //private final static Pattern num0 = Pattern.compile("([\\-+]?[0-9]{1,100}\\.[0-9]{1,100})",Pattern.UNICODE_CHARACTER_CLASS); // 12.35
    private final static Pattern num0 = Pattern.compile("([\\-+]?[0-9]{1,100}\\.[0-9]{1,100})"); // 12.35
    private final static Pattern num1 = Pattern.compile("([\\-+]?[0-9]{0,100}\\.[0-9]{1,100})"); // .35
    private final static Pattern num2 = Pattern.compile("([\\-+]?[0-9]{1,100}\\.[0-9]{0,100})"); // 123.
    private final static Pattern num3 = Pattern.compile("([\\-+]?[0-9]{1,100})"); // 123
    private final static Pattern vari = Pattern.compile("[_A-Za-z][_0-9A-Za-z]*");
    private final static Pattern func = Pattern.compile("(log|exp|sin|cos|sqrt)");
    private final static Pattern []nums = {num0,num1,num2,num3};
    private final String []c;
    private int cur;
    private static final String TAG = "[CustomKeyboard]: ";
    public _Tokenizer( final String s ) {
        String t;
        t = s.replaceAll("([^=]+)\\s*=\\s*([^=]+)","$1-($2)"); // replace "a = b" with "a-(b)"
        t = t.replaceAll("\\s+",""); // remove all whitespace
        t = t.replaceAll("[.]([^0-9])",".0$1"); // 2.(1+1) --> 2.0(1+1)
        t = t.replaceAll("([^0-9])[.]","$1 0."); // (1+1).2 --> (1+1) 0.2
        t = t.replaceAll("\\s+",""); // remove all whitespace
        t = t.replaceAll("\\^([0-9]+)\\(","^($1)("); // x^2(5-3) --> x^(2)(5-3)
        t = t.replaceAll("\\^([0-9]+)([a-z])","^($1)$2"); // x^2sqrt(2) --> x^(2)sqrt(2)
        t = t.replaceAll("([0-9])\\s*([a-z(])","$1*$2"); // 9sqrt(2) --> 9*sqrt(2)
        t = t.replaceAll("([)x])\\s*([0-9])","$1*$2");// (1+2)9 --> (1+2)*9, ...x9 --> x*9
        t = t.replaceAll("([)])\\s*([a-z])","$1*$2"); // resolve cases: (1+2)log(5) --> (1+2)*log(5)
        t = t.replaceAll("([x])\\s*([(])","$1*$2"); // resolve: x(1+2) --> x*(1+2)
        t = t.replace(")(",")*("); // resolve cases such as (1-2)(3-4) --> (1-2)*(3-4)
        c = (t.replaceAll("\\s+","").replaceAll("([*+/\\^\\-()])"," $1 ")).replaceFirst("^\\s+","").replaceAll("\\s+$","").split("\\s+");
        cur = 0;
    }
    public boolean hasNext() {
        return cur < c.length && peek() != null;
    }
    private boolean isFunc( String s ) {
        Matcher m = func.matcher(s);
        return m.find() && m.start(1) == 0 && m.end(1) == s.length();
    }
    private boolean isVariable( String s ) {
        Matcher m = vari.matcher(s);
        return m.find() && m.start() == 0 && m.end() == s.length();
    }
    private boolean isNumber( String s ) {
        for ( int i = 0; i < nums.length; ++i ) {
            Matcher m = nums[i].matcher(s);
            if ( m.find() && m.start(1) == 0 && m.end(1) == s.length() )
                return true ;
        }
        return false ;
    }
    public TokenType peek() {
        if ( cur == c.length )
            return null;
        if ( isFunc(c[cur]) )
            return TokenType.FUNC;
        if ( isVariable(c[cur]) )
            return TokenType.VARIABLE;
        if ( isNumber(c[cur]) )
            return TokenType.NUMBER;
        return TokenType.which(c[cur]);
    }
    public String next() {
        return cur==c.length?null:c[cur++];
    }

    public char peekChar() { return cur<c.length?c[cur].charAt(0):'\0'; }

    public TokenType poll() {
        TokenType ret = peek();
        if ( cur < c.length ) ++cur;
        return ret;
    }
}

public class EvalEngine {

    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    private _Tokenizer tokenizer;
    private boolean isEquation;

    private class Cell {
        private Cell left, right;
        Object val; // Variable? Double? Function? Binary Operation?
        Cell( Object v ) { val = v; }
    }

    private Cell handleFunc() {
        Cell x,px = null;
        String f;
        assert tokenizer.peek() == TokenType.FUNC;
        x = new Cell(TokenType.FUNC);
        x.left = new Cell(f = tokenizer.next());
        if ( tokenizer.peek() != TokenType.LB )
            throw new RuntimeException("ERR: "+f+": missing brackets; found "+tokenizer.next());
        tokenizer.poll(); // consume "("
        x.right = expr();
        if ( tokenizer.peek() != TokenType.RB )
            throw new RuntimeException("ERR: "+f+": no closing bracket");
        tokenizer.poll(); // consume ")"
        if ( tokenizer.peek() == TokenType.CARET )
            return Power(x);
        return x;
    }

    private Cell expr() {
        if ( tokenizer.peek() == TokenType.MINUS || tokenizer.peek() == TokenType.PLUS ) {
            Cell r = new Cell(tokenizer.poll());
            r.left = new Cell(0.00);
            r.right = f01(f00());
            return f02(r);
        }
        return f02(f01(f00()));
    }

    private Cell f01( Cell left ) {
        Cell x;
        if ( tokenizer.peek() == TokenType.STAR || tokenizer.peek() == TokenType.DIV ) {
            x = new Cell(tokenizer.poll());
            x.left = left;
            x.right = f00();
            return f01(x);
        }
        return left;
    }

    private Cell f02( Cell left )  {
        Cell x;
        if ( tokenizer.peek() == TokenType.PLUS || tokenizer.peek() == TokenType.MINUS ) {
            x = new Cell(tokenizer.poll());
            x.left = left;
            x.right = f01(f00());
            return f02(x);
        }
        return left;
    }

    private Cell g00() {
        Cell x;
        if ( tokenizer.peek() == TokenType.LB ) {
            tokenizer.poll();
            x = expr();
            if ( tokenizer.peek() != TokenType.RB )
                throw new IllegalStateException("Unmatched parenthesis");
            tokenizer.poll(); // consume ')'
            return x;
        }
        if ( tokenizer.peek() == TokenType.NUMBER ) {
            x = new Cell(Double.parseDouble(tokenizer.next()));
            return x;
        }
        if ( tokenizer.peek() == TokenType.FUNC ) {
            x = new Cell(TokenType.FUNC);
            String f;
            x.left = new Cell(f = tokenizer.next());
            if ( tokenizer.peek() != TokenType.LB )
                throw new RuntimeException("ERR: "+f+": missing brackets; found "+tokenizer.next());
            tokenizer.poll(); // consume "("
            x.right = expr();
            if ( tokenizer.peek() != TokenType.RB )
                throw new RuntimeException("ERR: "+f+": no closing bracket");
            tokenizer.poll(); // consume ")"
            return x;
        }
        throw new IllegalStateException("Syntax Error");
    }

    private Cell g01() {
        Cell x;
        if ( tokenizer.peek() == null )
            throw new IllegalStateException("Syntax Error");
        if ( tokenizer.peek() == TokenType.PLUS || tokenizer.peek() == TokenType.MINUS ) {
            x = new Cell(tokenizer.poll());
            x.left = new Cell(0.00);
            x.right = Power(g00());
            return x;
        }
        return g00();
    }

    private Cell Power( Cell left ) {
        Cell px;
        if ( tokenizer.peek() == TokenType.CARET ) {
            px = new Cell(tokenizer.poll());
            px.left = left; // very nasty and subtle moment: right-associativity of "^"
            px.right = Power(g01()); //!!! compare with f01() or f02() -- they are left associative
            return px;
        }
        return left;
    }

    private Cell f00() {
        Cell x;
        if ( tokenizer.peek() == TokenType.FUNC )
            return handleFunc();
        if ( tokenizer.peek() == TokenType.VARIABLE ) {
            x = new Cell(tokenizer.next());
            if ( tokenizer.peek() == TokenType.CARET )
                return Power(x);
            return x;
        }
        if ( tokenizer.peek() == TokenType.NUMBER ) {
            x = new Cell(Double.parseDouble(tokenizer.next()));
            if ( tokenizer.peek() == TokenType.CARET )
                return Power(x);
            return x;
        }
        if ( tokenizer.peek() == TokenType.LB ) {
            tokenizer.poll(); // consume "("
            x = expr();
            if ( tokenizer.peek() != TokenType.RB )
                throw new RuntimeException("ERR: Unclosed brackets");
            tokenizer.poll(); // consume ")"
            //for ( ;tokenizer.peek() == TokenType.CARET; x = handlePower(x) ) ;
            if ( tokenizer.peek() == TokenType.CARET )
                return Power(x);
            return x;
        }
        //if ( tokenizer.peekChar() == '+' || tokenizer.peekChar() == '-' ) {
            //for ( x = f00(); tokenizer.peek() == TokenType.CARET; x = handlePower(x) ) ;
        //    return x;
        //}
        //throw new RuntimeException("ERR: Unexpected symbol: "+tokenizer.next());
        throw new RuntimeException("ERR: Syntax error");
    }

    _Polynomial eval( Cell x ) {
        if ( x == null )
            throw new IllegalStateException("eval() called with null argument");
        if ( x.val instanceof String ) {// Variable; all variables are "x"
            String s = (String)x.val;
            if ( !s.equals("x") )
                throw new UnsupportedOperationException("ERR: " + "unknown variable "+s);
            return new _Polynomial(new double[]{0.00,1.00});
        }
        if ( x.val instanceof Double )
            return new _Polynomial(new double[]{(Double)x.val});
        _Polynomial l,r;
        if ( x.val instanceof TokenType ) {
            switch ( (TokenType)x.val ) {
                case CARET: r = eval(x.right);
                            if ( r.deg() >= 1 )
                                throw new UnsupportedOperationException("ERR: Raising to non-constant power");
                            l = eval(x.left);
                            if ( l.isZero() )
                                throw new UnsupportedOperationException("ERR: $$f(x) = a^x$$ should have \\(a > 0\\)");
                            if ( r.isZero() )
                                return new _Polynomial(new double[]{1.00});
                            if ( l.isConstant() && r.isConstant() && Math.abs(r.coeff(0)-(int)r.coeff(0)) < 1e-9 )
                                return new _Polynomial(new double[]{Math.pow(l.coeff(0),r.coeff(0))});
                            if ( !r.isConstant() )
                                throw new UnsupportedOperationException("ERR: Raising to polynomial power");
                            if ( l.isConstant() && l.coeff(0) < 0 )
                                throw new UnsupportedOperationException("ERR: $$f(x) = a^x$$ should have \\(a > 0\\)");
                            if ( l.isConstant() )
                                return new _Polynomial(new double[]{Math.pow(l.coeff(0),r.coeff(0))});
                            if ( !l.isConstant() && r.coeff(0) < 0 )
                                throw new UnsupportedOperationException("ERR: Raising a polynomial to power < 0");
                            if ( !l.isConstant() && Math.abs(r.coeff(0)-(int)r.coeff(0)) > 1e-6 )
                                throw new UnsupportedOperationException("ERR: Raising a polynomial to non-integer power");
                            return l.pow((int)r.coeff(0));
                case FUNC: return evalFunc((String)x.left.val,eval(x.right));
                case PLUS: return eval(x.left).add(eval(x.right));
                case MINUS: return eval(x.left).sub(eval(x.right));
                case STAR: return eval(x.left).mult(eval(x.right));
                case DIV: r = eval(x.right);
                          if ( r.deg() >= 1 )
                              throw new UnsupportedOperationException("ERR: Division by polynomial \\(\\new\\) const");
                          if ( Math.abs(r.coeff(0)) < 1e-15 )
                              throw new RuntimeException("ERR: Division by zero");
                          return eval(x.left).div(r.coeff(0));
            }
        }
        throw new IllegalStateException("x.val is of unknown type: "+x.val);
    }

    private _Polynomial evalFunc( String funcName, _Polynomial x ) {
        if ( x.deg() >= 1 )
            throw new UnsupportedOperationException("ERR: "+funcName+" applied to polynomial \\(\\neq\\) const");
        switch ( funcName ) {
            case "cos": return new _Polynomial(new double[]{Math.cos(x.coeff(0))});
            case "sin": return new _Polynomial(new double[]{Math.sin(x.coeff(0))});
            case "log": if ( Math.abs(x.coeff(0)) < 1e-15 || x.coeff(0) <= 0 )
                            throw new IllegalArgumentException("ERR: $$f(x) = \\log(x)$$ should have \\(x > 0\\)");
                        return new _Polynomial(new double[]{Math.log(x.coeff(0))});
            case "exp": return new _Polynomial(new double[]{Math.exp(x.coeff(0))});
            case "sqrt": if ( x.coeff(0) < 0 )
                            throw new IllegalArgumentException("ERR: $$f(x) = \\sqrt{x}$$ should have \\(x \\geq 0\\)");
                        return new _Polynomial(new double[]{Math.sqrt(x.coeff(0))});
            default: throw new IllegalArgumentException("unknown function name: "+funcName);
        }
    }

    /*public static void main(String... args) throws Exception {
        System.setIn(new FileInputStream(new File("/home/sj/IdeaProjects/Toptal000/src/input.txt")));
        System.setOut(new PrintStream(new File("/home/sj/IdeaProjects/Toptal000/src/out02.txt")));
        EvalEngine e = new EvalEngine();
        e.go();
    }*/

    public String eval( String s, StringBuilder ret ) throws Exception {
            try {
                String tmp = s.replace("exp","");
                isEquation = tmp.indexOf("=") != -1 || tmp.indexOf("x") != -1;
                tokenizer = new _Tokenizer(s);
                Cell root = expr();
                if ( tokenizer.hasNext() ) {
                    //throw new RuntimeException("ERR: Unexpected symbol: "+tokenizer.next());
                    throw new RuntimeException("ERR: Syntax error");
                }
                _Polynomial p = eval(root);
                if (p.deg() >= 3) {
                    ret.append(p.toString()+" = 0");
                    return "\\text{unsupported for degrees}\\geq{3}";
                }
                if ( p.deg() == -1 ) {
                    if ( isEquation )
                        return "-\\infty \\leq x \\leq +\\infty";
                    return MyDecimalFormatter.format(0.00);
                }
                if ( p.deg() == 0 ) {
                    if ( isEquation )
                        return "x \\in \\emptyset";
                    //return String.format("%.6f", p.coeff(0));
                    return MyDecimalFormatter.format(p.coeff(0));
                }
                if ( p.deg() == 1 )
                    return "x = "+MyDecimalFormatter.format(-p.coeff(0)/p.coeff(1));
                    //return String.format("x = %.6f", -p.coeff(0) / p.coeff(1));
                return solveQuadratic(p,ret);
            } catch (Exception e) {
                throw e;
            }
    }

    /*
    public void go() throws Exception {
        for ( String s; (s = br.readLine()) != null; ) {
            try {
                isEquation = s.indexOf("=") != -1 || s.indexOf("x") != -1;
                tokenizer = new _Tokenizer(s);
                _Polynomial p = eval(expr());
                if (p.deg() >= 3) {
                    //throw new UnsupportedOperationException("Finding roots of polynomials of degree >= 3 is not supported");
                    bw.write(p.toString() + " = 0,\\,\\,\\text{unsupported for degrees}\\geq{3}"+"\n");
                    continue ;
                }
                if ( p.deg() == -1 ) {
                    if ( isEquation )
                        bw.write("-\\infty \\leq x \\leq +\\infty"+"\n");
                    else bw.write("0.00"+"\n");
                    continue ;
                }
                if ( p.deg() == 0 ) {
                    if ( isEquation )
                        bw.write("x \\in \\emptyset"+"\n");
                    else bw.write(String.format("%.2f", p.coeff(0))+"\n");
                    continue ;
                }
                if (p.deg() == 1)
                    bw.write(String.format("x = %.2f", -p.coeff(0) / p.coeff(1))+"\n");
                else bw.write(solveQuadratic(p,null)+"\n");
            } catch (Exception e) {
                throw e;
            }
        }
        bw.flush();
    }
    */

    private String solveQuadratic(_Polynomial p, StringBuilder ret ) {
        StringBuilder sb = new StringBuilder();
        assert p.deg() == 2;
        double a = p.coeff(2), b = p.coeff(1), c = p.coeff(0), D = -4*a*c+b*b;
        ret.append(new _Polynomial(new double[]{c,b,a}).toString()+" = 0,\\,\\,");
        if ( D < 0 ) {
            sb.append("\\Leftrightarrow\\,\\,x \\in \\emptyset");
            return sb.toString();
        }
        D = Math.sqrt(D);
        if ( Math.abs(D) < 1e-10 )
            sb.append(String.format("\\Leftrightarrow\\,\\,x_{1,2} = %s",MyDecimalFormatter.format((-b)/2/a)));
        else if ( a > 0 ) sb.append(String.format("\\Leftrightarrow\\,\\\\\\begin{align*}x_1 &= %s\\\\x_2 &= %s\\end{align*}",MyDecimalFormatter.format((-b-D)/2/a),MyDecimalFormatter.format((-b+D)/2/a)));
        else sb.append(String.format("\\Leftrightarrow\\,\\\\\\begin{align*}x_1 &= %s\\\\x_2 &= %s\\end{align*}",MyDecimalFormatter.format((-b+D)/2/a),MyDecimalFormatter.format((-b-D)/2/a)));
        return sb.toString();
    }
}

