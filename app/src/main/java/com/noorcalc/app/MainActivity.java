package com.noorcalc.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(16, 23, 32);
    private static final int PANEL = Color.rgb(24, 34, 46);
    private static final int KEY = Color.rgb(31, 43, 57);
    private static final int MINT = Color.rgb(99, 213, 197);
    private static final int WHITE = Color.rgb(242, 246, 250);
    private static final int MUTED = Color.rgb(133, 151, 168);
    private final StringBuilder input = new StringBuilder();
    private TextView expressionView, resultView;
    private boolean justEvaluated = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildScreen();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(14), dp(22), dp(18));
        root.setBackgroundColor(BG);
        setContentView(root);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView mark = new TextView(this);
        mark.setText("ن"); mark.setTextColor(BG); mark.setTextSize(20); mark.setTypeface(null, Typeface.BOLD);
        mark.setGravity(Gravity.CENTER); mark.setBackground(round(MINT, 16));
        header.addView(mark, new LinearLayout.LayoutParams(dp(42), dp(42)));
        LinearLayout names = new LinearLayout(this); names.setOrientation(LinearLayout.VERTICAL);
        names.setPadding(dp(12), 0, 0, 0);
        TextView title = label("نور", 18, WHITE, true);
        TextView subtitle = label("آلة حاسبة", 12, MUTED, false);
        names.addView(title); names.addView(subtitle);
        header.addView(names, new LinearLayout.LayoutParams(0, -2, 1));
        TextView badge = label("CALC  •  01", 10, MUTED, true);
        header.addView(badge);
        root.addView(header);

        LinearLayout display = new LinearLayout(this);
        display.setOrientation(LinearLayout.VERTICAL);
        display.setGravity(Gravity.BOTTOM | Gravity.END);
        display.setPadding(dp(20), dp(18), dp(20), dp(20));
        display.setBackground(round(PANEL, 26));
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(-1, 0, 1.15f);
        dlp.setMargins(0, dp(24), 0, dp(18));
        root.addView(display, dlp);
        TextView hint = label("الحساب", 12, MUTED, false);
        hint.setGravity(Gravity.RIGHT);
        display.addView(hint, new LinearLayout.LayoutParams(-1, -2));
        expressionView = label("", 24, MUTED, false);
        expressionView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        expressionView.setSingleLine(true);
        display.addView(expressionView, new LinearLayout.LayoutParams(-1, 0, 1));
        View line = new View(this); line.setBackgroundColor(Color.rgb(44, 60, 75));
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(-1, dp(1)); lineLp.setMargins(0, 0, 0, dp(12)); display.addView(line, lineLp);
        resultView = label("0", 42, WHITE, true);
        resultView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        resultView.setSingleLine(true);
        display.addView(resultView, new LinearLayout.LayoutParams(-1, dp(56)));

        String[][] keys = {
            {"AC", "⌫", "%", "÷"},
            {"7", "8", "9", "×"},
            {"4", "5", "6", "−"},
            {"1", "2", "3", "+"},
            {"±", "0", ".", "="}
        };
        GridLayout grid = new GridLayout(this); grid.setColumnCount(4); grid.setRowCount(5);
        LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(-1, 0, 1.7f);
        root.addView(grid, glp);
        for (int r = 0; r < keys.length; r++) for (int c = 0; c < 4; c++) {
            String key = keys[r][c];
            Button b = new Button(this); b.setText(key); b.setAllCaps(false); b.setStateListAnimator(null);
            b.setTextSize(22); b.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL)); b.setPadding(0,0,0,0);
            b.setMinWidth(0); b.setMinimumWidth(0); b.setMinHeight(0); b.setMinimumHeight(0);
            int color = key.equals("=") ? MINT : (isOperator(key) || key.equals("AC") || key.equals("⌫") || key.equals("%") || key.equals("±") ? Color.rgb(39, 55, 69) : KEY);
            b.setTextColor(key.equals("=") ? BG : (isOperator(key) || key.equals("AC") || key.equals("⌫") || key.equals("%") || key.equals("±") ? MINT : WHITE));
            b.setBackground(round(color, 19));
            GridLayout.LayoutParams p = new GridLayout.LayoutParams(GridLayout.spec(r, 1, 1f), GridLayout.spec(c, 1, 1f));
            p.width = 0; p.height = 0; p.setMargins(dp(5), dp(5), dp(5), dp(5));
            grid.addView(b, p);
            b.setOnClickListener(v -> press(key));
        }
        TextView footer = label("بسيطة. واضحة. على مزاجك.", 11, MUTED, false);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(-1, dp(30)); flp.topMargin = dp(8); root.addView(footer, flp);
    }

    private void press(String key) {
        if (key.equals("AC")) { input.setLength(0); justEvaluated = false; resultView.setText("0"); expressionView.setText(""); return; }
        if (key.equals("⌫")) { if (input.length() > 0) input.deleteCharAt(input.length()-1); justEvaluated = false; refresh(); return; }
        if (key.equals("=")) { calculate(); return; }
        if (key.equals("±")) {
            if (input.length() > 0) { if (input.charAt(0) == '-') input.deleteCharAt(0); else input.insert(0, '-'); }
            refresh(); return;
        }
        if (isOperator(key)) {
            if (input.length() == 0 && key.equals("−")) input.append('-');
            else if (input.length() > 0) {
                char last = input.charAt(input.length()-1);
                if (isOpChar(last)) input.setCharAt(input.length()-1, toChar(key)); else input.append(toChar(key));
            }
            justEvaluated = false; refresh(); return;
        }
        if (justEvaluated && (Character.isDigit(key.charAt(0)) || key.equals("."))) input.setLength(0);
        justEvaluated = false;
        if (key.equals("%")) { if (input.length() > 0 && Character.isDigit(input.charAt(input.length()-1))) input.append("/100"); }
        else if (key.equals(".")) {
            int i = input.length()-1; while (i >= 0 && (Character.isDigit(input.charAt(i)) || input.charAt(i)=='.')) i--;
            if (input.indexOf(".", i+1) < 0) input.append(input.length()==0 || isOpChar(input.charAt(input.length()-1)) ? "0." : ".");
        } else input.append(key);
        refresh();
    }

    private void refresh() {
        expressionView.setText(input.toString().replace("/", "÷").replace("*", "×").replace("-", "−"));
        if (input.length() == 0) resultView.setText("0");
        else try { resultView.setText(format(new Parser(input.toString()).parse())); } catch (Exception ignored) { resultView.setText(" "); }
    }

    private void calculate() {
        if (input.length() == 0) return;
        try {
            double value = new Parser(input.toString()).parse();
            String result = format(value);
            expressionView.setText(input.toString().replace("/", "÷").replace("*", "×").replace("-", "−") + " =");
            resultView.setText(result);
            input.setLength(0); input.append(result); justEvaluated = true;
        } catch (Exception e) { resultView.setText("خطأ"); }
    }

    private String format(double n) {
        if (!Double.isFinite(n)) return "خطأ";
        if (Math.abs(n) < 1e12 && n == Math.rint(n)) return String.format(Locale.US, "%.0f", n);
        DecimalFormat f = new DecimalFormat("#,##0.########", DecimalFormatSymbols.getInstance(Locale.US));
        return f.format(n);
    }
    private boolean isOperator(String s) { return s.equals("÷") || s.equals("×") || s.equals("−") || s.equals("+"); }
    private boolean isOpChar(char c) { return c=='+' || c=='-' || c=='*' || c=='/'; }
    private char toChar(String s) { return s.equals("÷") ? '/' : s.equals("×") ? '*' : s.equals("−") ? '-' : '+'; }
    private TextView label(String text, int size, int color, boolean medium) {
        TextView t = new TextView(this); t.setText(text); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif" + (medium ? "-medium" : ""), Typeface.NORMAL)); return t;
    }
    private GradientDrawable round(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private static class Parser {
        final String s; int p = 0;
        Parser(String s) { this.s = s; }
        double parse() { double x = expression(); if (p != s.length()) throw new RuntimeException(); return x; }
        double expression() { double x = term(); while (p < s.length()) { char c=s.charAt(p); if(c=='+'){p++;x+=term();} else if(c=='-'){p++;x-=term();} else break; } return x; }
        double term() { double x = factor(); while (p < s.length()) { char c=s.charAt(p); if(c=='*'){p++;x*=factor();} else if(c=='/'){p++;x/=factor();} else break; } return x; }
        double factor() {
            if (p < s.length() && (s.charAt(p)=='+' || s.charAt(p)=='-')) { char c=s.charAt(p++); return (c=='-'?-1:1)*factor(); }
            int start=p; while(p<s.length() && (Character.isDigit(s.charAt(p)) || s.charAt(p)=='.')) p++;
            if(start==p) throw new RuntimeException(); return Double.parseDouble(s.substring(start,p));
        }
    }
}
