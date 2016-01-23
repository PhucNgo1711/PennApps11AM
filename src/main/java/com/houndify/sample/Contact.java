package com.houndify.sample;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PhucNgo on 1/23/16.
 */
public class Contact {
    public MainActivity mainActivity;

    public Contact(MainActivity activity){
        mainActivity = (MainActivity) activity;
    }

    public List<Person> getContactList(){
        ArrayList<Person> contactList = new ArrayList<Person>();

        Uri contactUri = ContactsContract.Contacts.CONTENT_URI;
        String[] PROJECTION = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
        };
        String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
        Cursor contacts = mainActivity.getApplicationContext().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, SELECTION, null, null);


        if (contacts.getCount() > 0)
        {
            while(contacts.moveToNext()) {
                Person aContact = new Person();
                int idFieldColumnIndex = 0;
                int nameFieldColumnIndex = 0;
                int numberFieldColumnIndex = 0;

                String contactId = contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts._ID));

                nameFieldColumnIndex = contacts.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                if (nameFieldColumnIndex > -1)
                {
                    aContact.setName(contacts.getString(nameFieldColumnIndex));
                }

                PROJECTION = new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER};
                final Cursor phone = mainActivity.getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, ContactsContract.Data.CONTACT_ID + "=?", new String[]{String.valueOf(contactId)}, null);
                if(phone.moveToFirst()) {
                    while(!phone.isAfterLast())
                    {
                        numberFieldColumnIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        if (numberFieldColumnIndex > -1)
                        {
                            aContact.setPhoneNum(phone.getString(numberFieldColumnIndex));
                            phone.moveToNext();
                            TelephonyManager mTelephonyMgr;
                            mTelephonyMgr = (TelephonyManager) mainActivity.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                            if (!mTelephonyMgr.getLine1Number().contains(aContact.getPhoneNum()))
                            {
                                contactList.add(aContact);
                            }
                        }
                    }
                }
                phone.close();
            }

            contacts.close();
        }

        return contactList;
    }
}
