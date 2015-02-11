package com.brentruhle.omgandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by U1C101 on 2/10/2015.
 */
public class DetailActivity extends ActionBarActivity {
    private static final String IMAGE_URL_BASE = "http://covers.openlibrary.org/b/id/"; //13
    String mImageURL; //13
    ShareActionProvider mShareActionProvider; //14
    TextView bookTitleTextView;
    TextView authorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Tell the activity which XML layout is right
        setContentView(R.layout.activity_detail);

        bookTitleTextView = (TextView) findViewById(R.id.detail_title);
        String bookTitle = this.getIntent().getExtras().getString("bookTitle");

        if (bookTitle.length() > 0) {
            bookTitleTextView.setText(bookTitle);
        }

        authorTextView = (TextView) findViewById(R.id.detail_authorname);
        String authorName = this.getIntent().getExtras().getString("authorName");

        if (authorName.length() > 0) {
            authorTextView.setText(authorName);
        }

        //Enabled the "up" button for more navigation options
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Access the imageview from XML
        ImageView imageView = (ImageView) findViewById(R.id.img_cover);

        //13. unpack the coverID from its trip inside your intent
        String coverID = this.getIntent().getExtras().getString("coverID");

        //See if there is a valid coverID
        if (coverID.length() > 0) {

            //Use the ID to construct an image URL
            mImageURL = IMAGE_URL_BASE + coverID + "-L.jpg";

            //use Picasson to load the image
            Picasso.with(this).load(mImageURL).placeholder(R.drawable.img_books_loading).into(imageView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Inflate the menu this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //Access the share item defines in menu XML
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);

        //access the object responsible for putting together the sharing submenu
        if (shareItem != null) {
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        }

        setShareIntent();

        return true;
    }

    private void setShareIntent() {

        //Create an intent with the contents of the TextView
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Book Recommendation!");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mImageURL);

        //Make sure the provider knows it should work with that intent
        mShareActionProvider.setShareIntent(shareIntent);
    }
}
