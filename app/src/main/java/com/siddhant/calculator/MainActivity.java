package com.siddhant.calculator;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.view.ViewPropertyAnimator;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private TextView tvExpression;
    private TextView tvDisplay;
    private GridLayout grid;

    private String currentInput = "";
    private String operator = "";
    private boolean isNewOp = true;
    private boolean hasDecimal = false;
    private boolean justCalculated = false;

    private StringBuilder fullExpression = new StringBuilder();
    private ExpressionEvaluator evaluator = new ExpressionEvaluator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvExpression = findViewById(R.id.tvExpression);
        tvDisplay = findViewById(R.id.tvDisplay);
        grid = findViewById(R.id.gridButtons);

        for (int i = 0; i < grid.getChildCount(); i++) {
            View v = grid.getChildAt(i);
            if (v instanceof Button) {
                v.setOnClickListener(this::onButtonClick);
            }
        }
    }

    private void onButtonClick(View view) {
        Button btn = (Button) view;
        String btnText = btn.getText().toString();

        view.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).withEndAction(() ->
                view.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
        ).start();

        switch (btnText) {
            case "AC":
                resetCalculator();
                break;
            case "±":
                toggleSign();
                break;
            case "%":
                applyPercent();
                break;
            case "+":
            case "−":
            case "×":
            case "÷":
                setOperator(btnText);
                break;
            case "=":
                calculateResult(true);
                break;
            case ".":
                addDecimalPoint();
                break;
            case "00":
                addNumber("00");
                break;
            default:
                addNumber(btnText);
        }
    }

    private void resetCalculator() {
        currentInput = "";
        operator = "";
        isNewOp = true;
        hasDecimal = false;
        fullExpression.setLength(0);
        tvDisplay.setText("0");
        tvExpression.setText("");
        justCalculated = false;
    }

    private void toggleSign() {
        if (!currentInput.isEmpty()) {
            if (currentInput.startsWith("-")) currentInput = currentInput.substring(1);
            else currentInput = "-" + currentInput;
            tvDisplay.setText(currentInput);
            appendOrReplaceLastInExpression(currentInput);
            updateFullExpressionPreview();
        } else if (fullExpression.length() > 0 && !isOperatorLast()) {
            if (fullExpression.charAt(0) == '-') fullExpression.deleteCharAt(0);
            else fullExpression.insert(0, '-');
            tvExpression.setText(fullExpression.toString());
            updateFullExpressionPreview();
        }
    }

    private void applyPercent() {
        if (!currentInput.isEmpty()) {
            double val = Double.parseDouble(currentInput) / 100.0;
            currentInput = formatNumber(val);
            tvDisplay.setText(currentInput);
            appendOrReplaceLastInExpression(currentInput);
            updateFullExpressionPreview();
        }
    }

    private void setOperator(String op) {
        if (!currentInput.isEmpty()) {
            appendOrReplaceLastInExpression(currentInput);
            fullExpression.append(' ').append(op).append(' ');
            tvExpression.setText(fullExpression.toString());
            currentInput = "";
            hasDecimal = false;
            isNewOp = true;
            justCalculated = false;
        } else if (isOperatorLast()) {
            int len = fullExpression.length();
            if (len >= 3) {
                fullExpression.delete(len - 3, len);
                fullExpression.append(op).append(' ');
                tvExpression.setText(fullExpression.toString());
            }
        }
    }

    private void calculateResult(boolean animate) {
        if (!currentInput.isEmpty()) {
            appendOrReplaceLastInExpression(currentInput);
        }
        String expr = fullExpression.toString().trim();
        if (expr.isEmpty()) return;

        try {
            double result = evaluator.eval(expr);
            String out = formatNumber(result);

            if (animate) {
                tvExpression.animate().translationYBy(-8f).alpha(0.6f).setDuration(180).start();
                ViewPropertyAnimator a = tvDisplay.animate();
                a.scaleX(1.06f).scaleY(1.06f).setDuration(180).withEndAction(() ->
                        tvDisplay.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                ).start();
            }

            tvDisplay.setText(out);
            fullExpression.setLength(0);
            fullExpression.append(out);
            tvExpression.setText(fullExpression.toString());
            currentInput = out;
            operator = "";
            isNewOp = true;
            hasDecimal = currentInput.contains(".");
            justCalculated = true;

        } catch (Exception e) {
            tvDisplay.setText("Error");
        }
    }

    private void addDecimalPoint() {
        if (!hasDecimal) {
            if (currentInput.isEmpty()) currentInput = "0.";
            else currentInput += ".";
            hasDecimal = true;
            isNewOp = false;
            tvDisplay.setText(currentInput);
            appendOrReplaceLastInExpression(currentInput);
            updateFullExpressionPreview();
        }
    }

    private void addNumber(String num) {
        if (justCalculated) {
            fullExpression.setLength(0);
            tvExpression.setText("");
            currentInput = "";
            justCalculated = false;
        }
        if (isNewOp) {
            currentInput = "";
            isNewOp = false;
        }
        currentInput += num;
        tvDisplay.setText(currentInput);
        appendOrReplaceLastInExpression(currentInput);
        updateFullExpressionPreview();
    }

    private void appendOrReplaceLastInExpression(String value) {
        String expr = fullExpression.toString();
        if (expr.isEmpty()) {
            fullExpression.append(value);
        } else {
            int len = expr.length();
            while (len > 0 && expr.charAt(len - 1) == ' ') len--;
            if (len > 0) {
                char last = expr.charAt(len - 1);
                if (last == '+' || last == '−' || last == '×' || last == '÷' || last == '*' || last == '/') {
                    fullExpression.append(value);
                } else {
                    int pos = len - 1;
                    while (pos >= 0 && (Character.isDigit(expr.charAt(pos)) || expr.charAt(pos) == '.' || expr.charAt(pos) == '-'))
                        pos--;
                    fullExpression.delete(pos + 1, fullExpression.length());
                    fullExpression.append(value);
                }
            } else {
                fullExpression.append(value);
            }
        }
        tvExpression.setText(fullExpression.toString());
    }

    private void updateFullExpressionPreview() {
        String expr = fullExpression.toString();
        if (expr.isEmpty()) return;
        try {
            double result = evaluator.eval(expr);
            String formatted = formatNumber(result);
            if (!justCalculated) {
                tvDisplay.setText(formatIfEmpty(currentInput, formatted));
            }
        } catch (Exception ignored) {
        }
    }

    private String formatIfEmpty(String current, String preview) {
        if (current == null || current.isEmpty()) return preview;
        return current;
    }

    private boolean isOperatorLast() {
        String expr = fullExpression.toString().trim();
        if (expr.isEmpty()) return false;
        char last = expr.charAt(expr.length() - 1);
        return last == '+' || last == '−' || last == '×' || last == '÷' || last == '*' || last == '/';
    }

    private String formatNumber(double num) {
        if (num == (long) num) {
            return String.valueOf((long) num);
        } else {
            DecimalFormat df = new DecimalFormat("#.##########");
            return df.format(num);
        }
    }
}
