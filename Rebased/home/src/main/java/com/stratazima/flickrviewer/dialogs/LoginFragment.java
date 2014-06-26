package com.stratazima.flickrviewer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.stratazima.flickrviewer.home.FlickrPhotoListFragment;
import com.stratazima.flickrviewer.home.R;

public class LoginFragment extends DialogFragment {
    EditText username;
    EditText password;

    public static LoginFragment newInstance(int title) {
        LoginFragment frag = new LoginFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    // Creates the dialog
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final View view = View.inflate(getActivity(), R.layout.fragment_shared_dialog, null);
        username = (EditText) view.findViewById(R.id.username_editText);
        password = (EditText) view.findViewById(R.id.password_editText);

        builder.setView(view)
                .setPositiveButton(R.string.action_shared_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {}
                })
                .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LoginFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    // Overrides the dialog on click which dismisses
    @Override
    public void onStart(){
        super.onStart();
        AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor edit = preference.edit();

                    if (!username.getText().toString().equals("") && !password.getText().toString().equals("")) {
                        edit.putString("username", username.getText().toString());
                        edit.putString("password", password.getText().toString());
                        edit.apply();

                        FlickrPhotoListFragment flickrPhotoListFragment = (FlickrPhotoListFragment) getFragmentManager().findFragmentById(R.id.daListFrag);
                        flickrPhotoListFragment.onLogin(preference.getString("username", ""));

                        dismiss();
                    } else {
                        if (username.getText().toString().equals("")) {
                            username.setError("Must Be filled out");
                        }
                        if (password.getText().toString().equals("")) {
                            password.setError("Must Be filled out");
                        }
                    }
                }
            });
        }
    }
}
