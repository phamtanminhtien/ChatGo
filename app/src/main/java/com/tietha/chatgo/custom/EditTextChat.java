package com.tietha.chatgo.custom;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.BuildCompat;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;

import com.google.android.material.textfield.TextInputEditText;

public class EditTextChat extends TextInputEditText {
    public EditTextChat(@NonNull Context context) {
        super(context);
    }

    public EditTextChat(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextChat(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        final InputConnection ic = super.onCreateInputConnection(editorInfo);
        EditorInfoCompat.setContentMimeTypes(editorInfo,
                new String[]{"image/gif", "image/png"});

        final InputConnectionCompat.OnCommitContentListener callback =
                new InputConnectionCompat.OnCommitContentListener() {
                    @Override
                    public boolean onCommitContent(InputContentInfoCompat inputContentInfo,
                                                   int flags, Bundle opts) {
                        // read and display inputContentInfo asynchronously
                        if (BuildCompat.isAtLeastNMR1() && (flags &
                                InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                            try {
                                inputContentInfo.requestPermission();
                            } catch (Exception e) {
                                return false; // return false if failed
                            }
                        }

                        if(mOnImportImgSupportListener != null){
                            mOnImportImgSupportListener.OnImportImgSupport(inputContentInfo);
                        }

                        return true;  // return true if succeeded
                    }
                };
        return InputConnectionCompat.createWrapper(ic, editorInfo, callback);
    }

    OnImportImgSupportListener mOnImportImgSupportListener;

    public void setOnImportImgSupportListener(OnImportImgSupportListener onImportImgSupportLsitener) {
        mOnImportImgSupportListener = onImportImgSupportLsitener;
    }

    public interface OnImportImgSupportListener{
        void OnImportImgSupport(InputContentInfoCompat inputContentInfo);
    }
}
