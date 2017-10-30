package com.solutions.sj.kazi;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Locale;

import io.github.kexanie.library.MathView;

import static android.R.attr.typeface;

public class MainActivity extends Activity {
    private final static String TAG = "[MainActivity]:";
    EditText expressionEditText;
    MathView resultMathView;
    CustomKeyboard mCustomKeyboard;
    Button submitButton;
    //final EvalEngine engine = new EvalEngine();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mCustomKeyboard = new CustomKeyboard(this, R.id.keyboardview, R.xml.calculatorkbd);
        /* setting DejaVuSans font for the EditText */
        expressionEditText = (EditText)findViewById(R.id.expressionEditText);
        AssetManager am = getApplicationContext().getAssets();
        Typeface typeface = Typeface.createFromAsset(am,String.format(Locale.US, "fonts/%s", "DejaVuSans.ttf"));
        expressionEditText.setTypeface(typeface);
        //expressionEditText.setTypeface(Typeface.MONOSPACE);

        expressionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if( count != 1 || start+count-1 < 0 || start+count-1 >= s.length() || s.charAt(start+count-1) != ')' ) return ;
                Deque<Integer> deque = new LinkedList<Integer>();
                for ( int i = 0; i != start+count-1; ++i )
                    if ( s.charAt(i) == '(' )
                        deque.addLast(i);
                    else if ( s.charAt(i) == ')' ) {
                        if ( deque.isEmpty() ) return ;
                        deque.pollLast();
                    }
                if ( deque.isEmpty() ) {
                    expressionEditText.setSelection(start,start+count);
                    Toast.makeText(MainActivity.this,"Unmatched parenthesis",Toast.LENGTH_LONG).show();
                    return ;
                }
                int k = deque.pollLast();
                expressionEditText.setSelection(k,start+count);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mCustomKeyboard.registerEditText(R.id.expressionEditText);
        resultMathView = (MathView)findViewById(R.id.resultMathView);
        /*
        resultMathView.config(
                "MathJax.Hub.Config({\n"+
                        "  CommonHTML: { linebreaks: { automatic: true } },\n"+
                        "  \"HTML-CSS\": { linebreaks: { automatic: true } },\n"+
                        "         SVG: { linebreaks: { automatic: true } }\n"+
                        "});");
        resultMathView.config(
                "MathJax.Hub.Config({\n"+
                        "  { TeX: { extensions: [\"color.js\"] } }\n"+
                        "});"
        );*/
        /* submit button is already on the keyboard, so no need for that */
        //submitButton = (Button)findViewById(R.id.submitButton);
        /*
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = expressionEditText.getText().toString();
                try {
                    StringBuilder ret = new StringBuilder();
                    String res = engine.eval(s,ret);
                    if ( !ret.toString().equals("") )
                        resultMathView.setText("$$"+ret.toString()+"$$\n"+"$$"+res+"$$");
                    else
                        resultMathView.setText("$$"+res+"$$");
                } catch ( Exception e ) {
                    Toast.makeText(v.getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
        */
    }

    @Override
    public void onBackPressed() {
        if( mCustomKeyboard.isCustomKeyboardVisible() )
            mCustomKeyboard.hideCustomKeyboard();
        else this.finish();
    }
}
