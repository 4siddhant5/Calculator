package com.siddhant.calculator;

public class ExpressionEvaluator {
    // accepts expression like "45×65+12" or "45 * 65 + 12"
    public double eval(String expr) {
        if (expr == null || expr.trim().isEmpty()) return 0;
        expr = expr.replace('×', '*').replace('÷', '/').replace('−', '-');
        return parse(expr);
    }

    // Recursive descent parser (supports parentheses if needed in future)
    private double parse(final String s) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < s.length()) ? s.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parseExpression() {
                nextChar();
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(s.substring(startPos, this.pos));
                } else {
                    // unexpected char: throw to be handled by caller
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                return x;
            }
        }.parseExpression();
    }
}
