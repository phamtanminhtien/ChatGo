package com.tietha.chatgo.custom;


import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tietha.chatgo.R;


public class ActionDialog {

    private AlertDialog mActionDialog;
    private Button btnAction;
    private TextView textAction;

    public ActionDialog(Context context) {
        LayoutInflater factory = LayoutInflater.from(context);
        View mActionDialogView = factory.inflate(R.layout.custom_dialog_action, null);

        textAction = mActionDialogView.findViewById(R.id.textAction);
        btnAction = mActionDialogView.findViewById(R.id.btnAction);
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickListener.onClick();
                dismiss();
            }
        });
        mActionDialog = new AlertDialog.Builder(context).create();
        mActionDialog.setCancelable(false);
        mActionDialog.setView(mActionDialogView);
    }
    public void show(){
        mActionDialog.show();
    }
    public void dismiss(){
        mActionDialog.dismiss();
    }
    public ActionDialog setText(String text){
        textAction.setText(text);
        return this;
    }
    public ActionDialog setButton(String text, OnClickListener onLickListioner ){
        btnAction.setText(text);
        mOnClickListener = onLickListioner;
        return this;
    }
    private OnClickListener mOnClickListener;

    public interface OnClickListener{
        public void onClick();
    }

}
