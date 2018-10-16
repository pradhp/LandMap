/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package com.pearnode.app.placero.area.res.doc;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class TextDetailDocumentsCell extends FrameLayout {

    private final TextView textView;
    private final TextView valueTextView;
    private final ImageView imageView;
    private float density = 1;
    private final TextView typeTextView;

    public TextDetailDocumentsCell(Context context) {
        super(context);

        this.density = this.getResources().getDisplayMetrics().density;

        this.textView = new TextView(context);
        this.textView.setTextColor(Color.BLACK);
        this.textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        this.textView.setLines(1);
        this.textView.setMaxLines(1);
        this.textView.setSingleLine(true);
        this.textView.setGravity(Gravity.LEFT);
        this.textView.setTypeface(null, Typeface.BOLD);
        this.addView(this.textView);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.textView.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.topMargin = (int) (this.density * 10);
        layoutParams.leftMargin = (int) (this.density * 71);
        layoutParams.rightMargin = (int) (this.density * 16);
        layoutParams.gravity = Gravity.LEFT;
        this.textView.setLayoutParams(layoutParams);

        this.valueTextView = new TextView(context);
        this.valueTextView.setTextColor(0xff8a8a8a);
        this.valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        this.valueTextView.setLines(1);
        this.valueTextView.setMaxLines(1);
        this.valueTextView.setSingleLine(true);
        this.valueTextView.setGravity(Gravity.LEFT);
        this.addView(this.valueTextView);
        layoutParams = (FrameLayout.LayoutParams) this.valueTextView.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.topMargin = (int) (this.density * 35);
        layoutParams.leftMargin = (int) (this.density * 71);
        layoutParams.rightMargin = (int) (this.density * 16);
        layoutParams.gravity = Gravity.LEFT;
        this.valueTextView.setLayoutParams(layoutParams);

        this.typeTextView = new TextView(context);
        this.typeTextView.setBackgroundColor(0xff757575);
        this.typeTextView.setEllipsize(TruncateAt.MARQUEE);
        this.typeTextView.setGravity(Gravity.CENTER);
        this.typeTextView.setSingleLine(true);
        this.typeTextView.setTextColor(0xffd1d1d1);
        this.typeTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        this.typeTextView.setTypeface(Typeface.DEFAULT_BOLD);
        this.addView(this.typeTextView);
        layoutParams = (FrameLayout.LayoutParams) this.typeTextView.getLayoutParams();
        layoutParams.width = (int) (this.density * 40);
        layoutParams.height = (int) (this.density * 40);
        layoutParams.leftMargin = (int) (this.density * 16);
        layoutParams.rightMargin = (int) (this.density * 0);
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        this.typeTextView.setLayoutParams(layoutParams);

        this.imageView = new ImageView(context);
        this.addView(this.imageView);
        layoutParams = (FrameLayout.LayoutParams) this.imageView.getLayoutParams();
        layoutParams.width = (int) (this.density * 40);
        layoutParams.height = (int) (this.density * 40);
        layoutParams.leftMargin = (int) (this.density * 16);
        layoutParams.rightMargin = (int) (this.density * 0);
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        this.imageView.setLayoutParams(layoutParams);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec((int) (this.density * 64), View.MeasureSpec.EXACTLY));
    }

    public void setValues(String text, String value, int resId) {
        this.textView.setText(text);
        this.valueTextView.setText(value);
        this.imageView.setImageDrawable(this.getResources().getDrawable(resId));
    }

}
