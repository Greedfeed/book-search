package com.brentruhle.omgandroid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

//Google Ads
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    TextView mainTextView;
    Button mainButton;
    EditText mainEditText;
    ListView mainListView;
    //ArrayAdapter mArrayAdapter;
    JSONAdapter mJSONAdapter;
    ArrayList mNameList = new ArrayList();
    ShareActionProvider mShareActionProvider;
    private static final String PREFS = "prefs";
    private static final String PREF_NAME = "name";
    SharedPreferences mSharedPreferences;
    private static final String QUERY_URL = "http://openlibrary.org/search.json?q=";
    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //1.  Access the TextView defined in layout XML and then set its text
        mainTextView = (TextView) findViewById(R.id.main_textview);
        //mainTextView.setText("Set in Java!");

        //2. Access the Button defined in layout XML and listen for it here
        mainButton = (Button) findViewById(R.id.main_button);
        mainButton.setOnClickListener(this);

        //3. Access the EditText defined in layout XML
        mainEditText = (EditText) findViewById(R.id.main_edittext);

        mainEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event){

                queryBooks(mainEditText.getText().toString());

                hideKeyboard(MainActivity.this, mainEditText);
                //InputMethodManager mInputMethodManager = (InputMethodManager)MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                //mInputMethodManager.hideSoftInputFromWindow(mainEditText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

                return true;
            }
        });

        //4. Access the ListView
        mainListView = (ListView) findViewById(R.id.main_listview);

        /*COMMENTING OUT FOR THE LAST TUTORIAL
        //Create an ArrayAdapter for the ListView
        mArrayAdapter = new ArrayAdapter(this,
                        android.R.layout.simple_list_item_1,
                        mNameList);

        //Set the ListView to use the ArrayAdapter
        mainListView.setAdapter(mArrayAdapter);          */

        //10. Create a JSONAdapter for the ListView
        mJSONAdapter = new JSONAdapter(this, getLayoutInflater());

        //Set the ListView to use the ArrayAdapter
        mainListView.setAdapter(mJSONAdapter);

        //5. Set this activity to react to list items being pressed
        mainListView.setOnItemClickListener(this);

        //7. Greet the user of ask for their name if new
        displayWelcome();

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Searching for book");
        mDialog.setCancelable(false);

        //Google Ads
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu. Adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //Access the Share Item Defined in menu XML
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);

        //Access the object responsible for putting together the sharing submenu
        if (shareItem != null) {
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        }

        //Create an Intent to share your content
        setShareIntent();

        return true;
    }

    private void setShareIntent() {
        if (mShareActionProvider != null) {

            //Create an Intent with the contents of the TextView
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Android Development");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mainTextView.getText());

            //Make sure the provider knows it should work with that intent
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onClick(View v) {
        /*
        //Take what was typed into the EditText and use in TextView
        mainTextView.setText(mainEditText.getText().toString() + " is the man (or woman)!");

        //Also add that value to the list shown in the ListView
        mNameList.add(mainEditText.getText().toString());
        mArrayAdapter.notifyDataSetChanged();

        // 6. The text you'd like to share has changed, and you need to update
        setShareIntent();
        */
        queryBooks(mainEditText.getText().toString());

        hideKeyboard(MainActivity.this, mainEditText);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //12. Now that the user's chosen a book, grab the cover data
        JSONObject jsonObject = (JSONObject) mJSONAdapter.getItem(position);
        String coverID = jsonObject.optString("cover_i", "");
        String bookTitle = jsonObject.optString("title", "");
        String authorName = jsonObject.optJSONArray("author_name").optString(0);

        //Create an Intent to take you over to a new DetailActivity
        Intent detailIntent = new Intent(this, DetailActivity.class);

        //Pack away the data about the cover into your intent before you head out
        detailIntent.putExtra("coverID", coverID);

        // TODO: add any other data you'd like like as extras
        detailIntent.putExtra("bookTitle", bookTitle);
        detailIntent.putExtra("authorName", authorName);

        //Start the next Activity using your prepared Intent
        startActivity(detailIntent);
    }

    public void displayWelcome() {
        //Access the device's key-value storage
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);

        //Read the user's name, or an empty string if nothing found
        String name = mSharedPreferences.getString(PREF_NAME, "");

        if (name.length() > 0) {
            //If the name is valid, display a Toast welcoming them
            Toast.makeText(this, "Welcome back, " + name + "!", Toast.LENGTH_LONG).show();
        } else {
            //Otherwise, show a dialog to ask for their name
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Hello!");
            alert.setMessage("What is your name?");

            //Create EditText for entry
            final EditText input = new EditText(this);
            alert.setView(input);

            //Make an "OK" button to save the name
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    //Grab the EditText's input
                    String inputName = input.getText().toString();

                    //Put it into memory (don't forget to commit!)
                    SharedPreferences.Editor e = mSharedPreferences.edit();
                    e.putString(PREF_NAME, inputName);
                    e.commit();

                    //Welcome the new user
                    Toast.makeText(getApplicationContext(), "Welcome, " + inputName + "!", Toast.LENGTH_LONG).show();
                }
            });

            //Make a "Cancel" button that simply dismisses the alert
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {}
            });

            alert.show();
        }
    }

    private void queryBooks (String searchString) {

        //Prepare your search string to be put in a url it might have reserved characters
        String urlString = "";
        try {
            urlString = URLEncoder.encode(searchString, "UTF-8");
        } catch (UnsupportedEncodingException e) {

            //if this fails for some reason, let the user know why
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        //Create a client to perform networking
        AsyncHttpClient client = new AsyncHttpClient();

        //Show progressdialog to inform user that a task in the background is occurring
        mDialog.show();

        //Have the client get a JSONArray of data and define how to respond
        client.get(QUERY_URL + urlString,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(JSONObject jsonObject) {

                        //11. Dismiss the progressdialog
                        mDialog.dismiss();

                        //Display a Toast message to announce your success
                        Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();

                        //8. For now, just log results
                        //Log.d("omg android", jsonObject.toString());
                        //Update the data in your custom method
                        mJSONAdapter.updateData(jsonObject.optJSONArray("docs"));
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable throwable, JSONObject error) {

                        //11. Dismiss the progressdialog
                        mDialog.dismiss();

                        //Display a Toast message to announce your failure
                        Toast.makeText(getApplicationContext(), "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();

                        //Log error message to help solve any problems
                        Log.e("omg android", statusCode + " " + throwable.getMessage());
                    }
                });
    }

    //This Hides a keyboard! magically!
    public void hideKeyboard(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
}
