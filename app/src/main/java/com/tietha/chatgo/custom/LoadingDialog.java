package com.tietha.chatgo.custom;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.tietha.chatgo.R;

public class LoadingDialog {

    private Dialog mLoadingDialog;
    
    public LoadingDialog(Context context) {
        LayoutInflater factory = LayoutInflater.from(context);
        View LoadingDialogView = factory.inflate(R.layout.custom_dialog_loading, null);
        mLoadingDialog = new Dialog(context);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setContentView(LoadingDialogView);
    }
    
    public void show(){
        mLoadingDialog.show();
    }
    
    public void dismiss(){
        mLoadingDialog.dismiss();
    }

}
