package com.orbitmessenger

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

object ContactResolver {
    fun getContactName(context: Context, phoneNumber: String): String {
        var contactName = phoneNumber
        
        try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI, 
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        contactName = cursor.getString(nameIndex)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return contactName
    }
}
