package com.twormobile.itrackmygps;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import static com.twormobile.itrackmygps.IntentCodes.PICK_CONTACT;

public class PickPhoneContactActivity extends Activity {
    private static final String TAG = "MainActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
    }

    public void buttonContactsPressed(View view){
        Intent pickContactIntent = new Intent( Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI );
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(pickContactIntent, PICK_CONTACT);
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent intent ) {

        super.onActivityResult( requestCode, resultCode, intent );
        if ( requestCode == PICK_CONTACT ) {

            if ( resultCode == RESULT_OK ) {
                Uri contactData = intent.getData();
                Cursor c = getContentResolver().query(contactData, null, null, null, null);
                c.moveToNext();

                String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                Log.i(TAG, "name:" + name + " number:" + number);
            }
        }
    }
}
