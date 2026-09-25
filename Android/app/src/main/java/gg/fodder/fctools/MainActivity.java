package gg.fodder.fctools;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.app.Dialog;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(9, 13, 20);
    private static final int PANEL = Color.rgb(17, 25, 37);
    private static final int MINT = Color.rgb(94, 242, 166);
    private static final String HOME_URL = "https://www.ea.com/ea-sports-fc/ultimate-team/web-app/";

    private LinearLayout root;
    private WebView webView;
    private CredentialStore credentialStore;
    private SharedPreferences uiPreferences;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        credentialStore = new CredentialStore(this);
        uiPreferences = getSharedPreferences("fc_tools_ui", MODE_PRIVATE);
        webView = createWebView();
        showWeb();
    }

    private WebView createWebView() {
        WebView view = new WebView(this);
        WebSettings settings = view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        view.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView page, String url) {
                try {
                    String loader;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("fodder-loader.js")))) {
                        StringBuilder source = new StringBuilder(); String line;
                        while ((line = reader.readLine()) != null) source.append(line).append('\n');
                        loader = source.toString();
                    }
                    page.evaluateJavascript("(()=>{if(window.__fcToolsOpenPatched)return;window.__fcToolsOpenPatched=true;window.open=(url)=>{if(url)location.href=url;return window}})()", null);
                    page.evaluateJavascript(loader, null);
                    fillSavedLogin(false);
                } catch (Exception ignored) {}
            }
        });
        view.loadUrl(HOME_URL);
        return view;
    }

    private void baseLayout() {
        if (webView.getParent() instanceof ViewGroup) ((ViewGroup) webView.getParent()).removeView(webView);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        setContentView(root);
    }

    private void showWeb() {
        baseLayout();
        FrameLayout frame = new FrameLayout(this);
        frame.addView(webView, new FrameLayout.LayoutParams(-1, -1));
        Button settings = button("•••", Color.TRANSPARENT);
        settings.setTextColor(Color.WHITE);
        settings.setAlpha(0.82f);
        settings.setOnClickListener(v -> showSettingsDialog());
        FrameLayout.LayoutParams settingsParams = new FrameLayout.LayoutParams(52, 48, Gravity.TOP | Gravity.END);
        settingsParams.setMargins(0, 94, 12, 0);
        frame.addView(settings, settingsParams);
        settings.setOnTouchListener(new View.OnTouchListener() {
            float downX;
            float downY;
            float startTranslationX;
            float startTranslationY;
            boolean moved;

            @Override public boolean onTouch(View view, android.view.MotionEvent event) {
                switch (event.getActionMasked()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        downY = event.getRawY();
                        startTranslationX = view.getTranslationX();
                        startTranslationY = view.getTranslationY();
                        moved = false;
                        return true;
                    case android.view.MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - downX;
                        float dy = event.getRawY() - downY;
                        if (Math.abs(dx) > 6 || Math.abs(dy) > 6) moved = true;
                        float maxX = Math.max(0, frame.getWidth() - view.getLeft() - view.getWidth() - 12);
                        float maxY = Math.max(0, frame.getHeight() - view.getTop() - view.getHeight() - 12);
                        view.setTranslationX(Math.max(-view.getLeft() + 12, Math.min(startTranslationX + dx, maxX)));
                        view.setTranslationY(Math.max(-view.getTop() + 12, Math.min(startTranslationY + dy, maxY)));
                        return true;
                    case android.view.MotionEvent.ACTION_UP:
                        if (!moved) view.performClick();
                        uiPreferences.edit()
                                .putFloat("settings_offset_x", view.getTranslationX())
                                .putFloat("settings_offset_y", view.getTranslationY())
                                .apply();
                        return true;
                    case android.view.MotionEvent.ACTION_CANCEL:
                        return true;
                    default:
                        return false;
                }
            }
        });
        settings.setTranslationX(uiPreferences.getFloat("settings_offset_x", 0f));
        settings.setTranslationY(uiPreferences.getFloat("settings_offset_y", 0f));
        root.addView(frame, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    private void showSettingsDialog() {
        Dialog dialog = new Dialog(this);
        LinearLayout panel = column(10);
        panel.setPadding(24, 20, 24, 20);
        panel.setBackgroundColor(PANEL);
        TextView title = text("Settings", 20, Color.WHITE, true);
        panel.addView(title);
        EditText email = input("EA email / username");
        EditText password = input("EA password");
        password.setInputType(0x81);
        String[] saved = credentialStore.read();
        if (saved != null) email.setText(saved[0]);
        panel.addView(email);
        panel.addView(password);
        panel.addView(text("Saved fields fill automatically. Sign In is never pressed automatically.", 12, Color.LTGRAY, false));
        Button save = button("Save securely", MINT);
        save.setOnClickListener(v -> {
            try { credentialStore.save(email.getText().toString().trim(), password.getText().toString()); password.setText(""); fillSavedLogin(true); dialog.dismiss(); }
            catch (Exception ignored) {}
        });
        panel.addView(save);
        Button remove = button("Remove saved login", Color.rgb(80, 35, 45));
        remove.setOnClickListener(v -> { credentialStore.clear(); email.setText(""); password.setText(""); });
        panel.addView(remove);
        Button update = button("Check updates now", Color.rgb(35, 55, 70));
        update.setOnClickListener(v -> { webView.reload(); dialog.dismiss(); });
        panel.addView(update);
        dialog.setContentView(panel);
        Window window = dialog.getWindow();
        if (window != null) { window.setBackgroundDrawableResource(android.R.color.transparent); window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); }
        dialog.show();
        if (dialog.getWindow() != null) dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.86), WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void fillSavedLogin(boolean notify) {
        String[] saved = credentialStore.read(); if (saved == null) return;
        try {
            String email = JSONObject.quote(saved[0]); String password = JSONObject.quote(saved[1]);
            String script = "(()=>{const e=" + email + ",p=" + password + ";const set=(x,v)=>{if(!x||x.value)return;const s=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value')?.set;s?s.call(x,v):x.value=v;['input','change','blur'].forEach(n=>x.dispatchEvent(new Event(n,{bubbles:true})))};set(document.querySelector('input[type=email],input[name=email],input[autocomplete=username]'),e);set(document.querySelector('input[type=password],input[name=password],input[autocomplete=current-password]'),p)})()";
            webView.evaluateJavascript(script, null);
        } catch (Exception ignored) {}
    }

    private LinearLayout column(int spacing) { LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL); layout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE); layout.setDividerPadding(spacing / 2); return layout; }
    private TextView text(String value, int size, int color, boolean bold) { TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(color); view.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL); view.setPadding(0, 4, 0, 4); return view; }
    private Button button(String value, int color) { Button button = new Button(this); button.setText(value); button.setTextColor(color == MINT ? Color.rgb(5, 20, 13) : Color.WHITE); button.setTextSize(13); return button; }
    private EditText input(String hint) { EditText input = new EditText(this); input.setHint(hint); input.setHintTextColor(Color.GRAY); input.setTextColor(Color.WHITE); input.setSingleLine(true); return input; }
}
