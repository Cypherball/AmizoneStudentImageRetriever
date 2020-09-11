package com.experiment.app7;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.experiment.app7.databinding.ActivityMainBinding;
import com.experiment.app7.databinding.CustomDialogBinding;
import com.experiment.app7.databinding.ListViewItemBinding;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final String TAG = "MainActivity";
    private ProfileListAdapter mProfileListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
    }

    private void initViews() {
        setSupportActionBar(binding.topAppBar);

        mProfileListAdapter = new ProfileListAdapter(this);
        binding.profileListView.setAdapter(mProfileListAdapter);

        setListeners();
    }

    private void setListeners() {
        binding.addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.addFab.setEnabled(false);
                buildAlertDialog();
            }
        });
    }

    private void buildAlertDialog() {
        final String[] str = {""};
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        CustomDialogBinding customDialogBinding = CustomDialogBinding.inflate(this.getLayoutInflater());
        dialogBuilder.setView(customDialogBinding.getRoot())
                .setTitle("Add Profile")
                .setMessage("Please enter profile details")
                .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        binding.addFab.setEnabled(true);
                        dialogBuilder.setCancelable(true);
                    }
                })
                .setCancelable(false);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String name = customDialogBinding.nameET.getText().toString();
                String formNo = customDialogBinding.formET.getText().toString();
                if(name.isEmpty() || formNo.isEmpty()){
                    Snackbar.make(customDialogBinding.getRoot(),"Please fill all fields", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(formNo.length() > 9 || formNo.length() < 6){
                    Snackbar.make(binding.parentLayout,"Please enter valid form number!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                CheckUserAndAdd checkUser = new CheckUserAndAdd(formNo, name);
                checkUser.execute();
                dialog.dismiss();
                Snackbar.make(binding.parentLayout,"Processing...", Snackbar.LENGTH_INDEFINITE).show();
            }
        });
    }


    private class CheckUserAndAdd extends AsyncTask<Void, Void, Void>{
    //Check if user exists by checking for their image on amizone server
        private int code;
        private String username;
        private String name;

        //username is form number. Name is just the name of the user, can be anything.
        CheckUserAndAdd(String username, String name){
            this.username = username;
            this.name = name;
            this.code = 0;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (code == 404){   //Image does not exist
                Snackbar.make(binding.parentLayout, "'" + username + "' does not exist in amizone server!", Snackbar.LENGTH_SHORT).show();
            } else if (code == 200){    //Image exists
                mProfileListAdapter.add(new AbstractMap.SimpleEntry<>(username, name));  //Add profile to list view
                Snackbar.make(binding.parentLayout, "User added successfully", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(binding.parentLayout, "Error! Response Returned: " + code, Snackbar.LENGTH_SHORT).show();
            }
            binding.addFab.setEnabled(true);    //re-enable add button
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                //Check if image is present in url by getting response code
                URL u = new URL("https://amizone.net/amizone/Images/Signatures/" + username + "_P.png");
                HttpURLConnection huc = (HttpURLConnection) u.openConnection();
                huc.setRequestMethod("GET");
                huc.connect();
                code = huc.getResponseCode();
                System.out.println(code);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    //Custom Adapter to display and update Profiles List View
    public class ProfileListAdapter extends ArrayAdapter<Map.Entry<String, String>> {
        public ProfileListAdapter(Context context) {
            super (context, R.layout.list_view_item);
        }

        @NonNull
        @Override
        public View getView (int position, View convertView, ViewGroup parent)  {
            ListViewItemBinding binding = ListViewItemBinding.inflate(getLayoutInflater());

            Map.Entry<String, String> currentProfile = this.getItem(position);

            String formNo = currentProfile.getKey();
            String name = currentProfile.getValue();
            binding.formTV.setText(formNo);
            binding.nameTV.setText(name);
            Picasso.get().load("https://amizone.net/amizone/Images/Signatures/" + formNo + "_P.png")
                    .into(binding.dpIV);

            return binding.getRoot();
        }
    }
}