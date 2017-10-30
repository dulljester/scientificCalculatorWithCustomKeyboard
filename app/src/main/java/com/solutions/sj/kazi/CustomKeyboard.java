package com.solutions.sj.kazi;

/**
 * Created by kazi on 3/9/2017.
 */

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import io.github.kexanie.library.MathView;

/**
 * When an activity hosts a keyboardView, this class allows several EditText's to register for it.
 *
 * @author Maarten Pennings
 * @date   2012 December 23
 */
public class CustomKeyboard {
    private static final String TAG = "[CustomKeyboard]: ";

    /** A link to the KeyboardView that is used to render this CustomKeyboard. */
    private KeyboardView mKeyboardView;
    /** A link to the activity that hosts the {@link #mKeyboardView}. */
    private Activity     mHostActivity;
    final EvalEngine engine = new EvalEngine();

    /** The key (code) handler. */
    private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() {

        public final static int CodeDelete   = -5; // Keyboard.KEYCODE_DELETE
        public final static int CodeCancel   = -3; // Keyboard.KEYCODE_CANCEL
        public final static int CodePrev     = 55000;
        public final static int CodeAllLeft  = 55001;
        public final static int CodeLeft     = 55002;
        public final static int CodeRight    = 55003;
        public final static int CodeAllRight = 55004;
        public final static int CodeNext     = 55005;
        public final static int CodeClear    = 55006;
        public final static int Go           = 55021;
        public final static int CodeCos      = 55020;
        public final static int CodeSin      = 55007;
        public final static int CodeLog      = 55008;
        public final static int CodeExp      = 55009;
        public final static int CodeSqrt     = 55010;
        public final static int CodeUnaryMinus = 55022;

        @Override public void onKey(int primaryCode, int[] keyCodes) {
            // NOTE We can say '<Key android:codes="49,50" ... >' in the xml file; all codes come in keyCodes, the first in this list in primaryCode
            // Get the EditText and its Editable
            View focusCurrent = mHostActivity.getWindow().getCurrentFocus();
            if( focusCurrent == null || focusCurrent.getClass() != EditText.class ) {
                Log.d(TAG,"Returning...");
                if ( focusCurrent == null )
                    Log.d(TAG,"currentFocus is null");
                else Log.d(TAG,focusCurrent.getClass().toString());
                return;
            }

            EditText edittext = (EditText) focusCurrent;
            Editable editable = edittext.getText();
            MathView resultMathView = (MathView)mHostActivity.findViewById(R.id.resultMathView);
            int start = edittext.getSelectionStart();
            // Apply the key to the edittext
            if( primaryCode==CodeCancel ) {
                hideCustomKeyboard();
            } else if( primaryCode==CodeDelete ) {
                if( editable!=null && start>0 ) editable.delete(start - 1, start);
            } else if( primaryCode==CodeClear ) {
                if( editable!=null ) editable.clear();
            } else if( primaryCode==CodeLeft ) {
                if( start>0 ) edittext.setSelection(start - 1);
            } else if( primaryCode==CodeRight ) {
                if (start < edittext.length()) edittext.setSelection(start + 1);
            } else if( primaryCode==CodeAllLeft ) {
                edittext.setSelection(0);
            } else if( primaryCode==CodeAllRight ) {
                edittext.setSelection(edittext.length());
            } else if( primaryCode==CodePrev ) {
                View focusNew= edittext.focusSearch(View.FOCUS_LEFT);
                if( focusNew!=null ) focusNew.requestFocus();
            } else if( primaryCode==CodeNext ) {
                View focusNew= edittext.focusSearch(View.FOCUS_RIGHT);
                if( focusNew!=null ) focusNew.requestFocus();
            } else if ( primaryCode == Go ) {
                //edittext.setText("("+editable.toString()+")");
                //edittext.setSelection(edittext.length());
                String s = edittext.getText().toString();
                try {
                    StringBuilder ret = new StringBuilder();
                    String res = engine.eval(s,ret);
                    if ( !ret.toString().equals("") ) {
                        String poly = ret.toString();
                        //PrettyPrint.print(resultMathView,poly,res);
                        resultMathView.setText("$$"+poly+"$$\n"+"$$"+res+"$$");
                    }
                    else
                        resultMathView.setText("$$"+res+"$$");
                } catch ( Exception e ) {
                    //Toast.makeText(mHostActivity,e.getMessage(),Toast.LENGTH_LONG).show();
                    resultMathView.setText(e.getMessage());
                    //throw new RuntimeException(e);
                }
            }
            else if ( primaryCode == CodeCos ) {
                edittext.setText("cos("+editable.toString()+")");
                edittext.setSelection(edittext.length());
            }
            else if ( primaryCode == CodeSin ) {
                edittext.setText("sin("+editable.toString()+")");
                edittext.setSelection(edittext.length());
            }
            else if ( primaryCode == CodeLog ) {
                //edittext.setText(editable.toString()+"log()");
                editable.insert(start,"log()");
                //edittext.setSelection(edittext.length()-1);
            }
            else if ( primaryCode == CodeExp ) {
                //edittext.setText(editable.toString()+"exp()");
                editable.insert(start,"exp()");
                //edittext.setSelection(edittext.length()-1);
            }
            else if ( primaryCode == CodeSqrt ) {
                editable.insert(start,"sqrt()");
                //edittext.setText(editable.toString()+"sqrt()");
                //edittext.setSelection(edittext.length()-1);
            }
            else if ( primaryCode == CodeUnaryMinus ) {
                editable.insert(start,Character.toString('\uFE63'));
            }
            else { // insert character
                editable.insert(start, Character.toString((char) primaryCode));
            }
        }

        @Override public void onPress(int primaryCode) {
        }

        @Override public void onRelease(int primaryCode) {
        }

        @Override public void onText(CharSequence text) {
        }

        @Override public void swipeDown() {
        }

        @Override public void swipeLeft() {
        }

        @Override public void swipeRight() {
        }

        @Override public void swipeUp() {
        }
    };

    /**
     * Create a custom keyboard, that uses the KeyboardView (with resource id <var>viewid</var>) of the <var>host</var> activity,
     * and load the keyboard layout from xml file <var>layoutid</var> (see {@link Keyboard} for description).
     * Note that the <var>host</var> activity must have a <var>KeyboardView</var> in its layout (typically aligned with the bottom of the activity).
     * Note that the keyboard layout xml file may include key codes for navigation; see the constants in this class for their values.
     * Note that to enable EditText's to use this custom keyboard, call the {@link #registerEditText(int)}.
     *
     * @param host The hosting activity.
     * @param viewid The id of the KeyboardView.
     * @param layoutid The id of the xml file containing the keyboard layout.
     */
    public CustomKeyboard(Activity host, int viewid, int layoutid) {
        mHostActivity= host;
        mKeyboardView= (KeyboardView)mHostActivity.findViewById(viewid);
        mKeyboardView.setKeyboard(new Keyboard(mHostActivity, layoutid));
        mKeyboardView.setPreviewEnabled(false); // NOTE Do not show the preview balloons
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
        // Hide the standard keyboard initially
        mHostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /** Returns whether the CustomKeyboard is visible. */
    public boolean isCustomKeyboardVisible() {
        return mKeyboardView.getVisibility() == View.VISIBLE;
    }

    /** Make the CustomKeyboard visible, and hide the system keyboard for view v. */
    public void showCustomKeyboard( View v ) {
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);
        if( v!=null ) ((InputMethodManager)mHostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    /** Make the CustomKeyboard invisible. */
    public void hideCustomKeyboard() {
        mKeyboardView.setVisibility(View.GONE);
        mKeyboardView.setEnabled(false);
    }

    /**
     * Register <var>EditText<var> with resource id <var>resid</var> (on the hosting activity) for using this custom keyboard.
     *
     * @param resid The resource id of the EditText that registers to the custom keyboard.
     */
    public void registerEditText(int resid) {
        // Find the EditText 'resid'
        Log.d(TAG,"inside RegisterEditText with ID = "+resid);
        EditText edittext= (EditText)mHostActivity.findViewById(resid);
        // Make the custom keyboard appear
        edittext.setOnFocusChangeListener(new OnFocusChangeListener() {
            // NOTE By setting the on focus listener, we can show the custom keyboard when the edit box gets focus, but also hide it when the edit box loses focus
            @Override public void onFocusChange(View v, boolean hasFocus) {
                if( hasFocus ) showCustomKeyboard(v); else hideCustomKeyboard();
            }
        });
        edittext.setOnClickListener(new OnClickListener() {
            // NOTE By setting the on click listener, we can show the custom keyboard again, by tapping on an edit box that already had focus (but that had the keyboard hidden).
            @Override public void onClick(View v) {
                showCustomKeyboard(v);
            }
        });
        // Disable standard keyboard hard way
        // NOTE There is also an easy way: 'edittext.setInputType(InputType.TYPE_NULL)' (but you will not have a cursor, and no 'edittext.setCursorVisible(true)' doesn't work )
        edittext.setOnTouchListener(new OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();       // Backup the input type
                edittext.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
                edittext.onTouchEvent(event);               // Call native handler
                edittext.setInputType(inType);              // Restore input type
                return true; // Consume touch event
            }
        });
        // Disable spell check (hex strings look like words to Android)
        edittext.setInputType(edittext.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

}


// NOTE How can we change the background color of some keys (like the shift/ctrl/alt)?
// NOTE What does android:keyEdgeFlags do/mean
