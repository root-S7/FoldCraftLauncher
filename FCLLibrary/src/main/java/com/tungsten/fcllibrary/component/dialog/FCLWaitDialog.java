package com.tungsten.fcllibrary.component.dialog;

import android.content.Context;
import android.graphics.Point;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ScrollView;

import androidx.annotation.NonNull;

import com.tungsten.fcllibrary.R;
import com.tungsten.fcllibrary.component.view.FCLTextView;
import com.tungsten.fcllibrary.util.ConvertUtils;

public class FCLWaitDialog extends FCLDialog {
    private final View parent;
    private final FCLTextView message;
    private final ScrollView scrollView;

    public FCLWaitDialog(@NonNull Context context) {
        super(context);

        setContentView(R.layout.dialog_wait);

        parent = findViewById(R.id.wait_view);
        message = findViewById(R.id.wait_dialog_text);
        scrollView = findViewById(R.id.wait_text_scroll);
    }

    private void checkHeight() {
        parent.post(() -> message.post(() -> {
            WindowManager wm = getWindow().getWindowManager();
            Point point = new Point();
            wm.getDefaultDisplay().getSize(point);
            int maxHeight = point.y - ConvertUtils.dip2px(getContext(), 30);
            if (parent.getMeasuredHeight() < maxHeight) {
                ViewGroup.LayoutParams layoutParams = scrollView.getLayoutParams();
                layoutParams.height = message.getMeasuredHeight();
                scrollView.setLayoutParams(layoutParams);
                getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            } else {
                getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, maxHeight);
            }
        }));
    }

    public FCLWaitDialog setMessage(String message) {
        this.message.setText(message);
        checkHeight();

        return this;
    }

    public FCLWaitDialog setMessage(CharSequence message) {
        this.message.setText(message);
        checkHeight();

        return this;
    }

    public FCLWaitDialog setMessage(Spanned message) {
        this.message.setText(message);
        checkHeight();

        return this;
    }

    public void dismiss() {
        super.dismiss();
    }

    public FCLWaitDialog showDialog() {
        super.show();

        return this;
    }

    public static class Builder {
        private final FCLWaitDialog dialog;

        public Builder(Context context) {
            dialog = new FCLWaitDialog(context);
        }

        public Builder setCancelable(boolean cancelable) {
            dialog.setCancelable(cancelable);
            return this;
        }

        public FCLWaitDialog create() {
            return dialog;
        }

        public Builder setMessage(String message) {
            dialog.setMessage(message);
            return this;
        }

        public Builder setMessage(CharSequence message) {
            dialog.setMessage(message);
            return this;
        }

        public Builder show() {
            dialog.show();
            return this;
        }

        public Builder dismiss() {
            dialog.dismiss();
            return this;
        }
    }
}
