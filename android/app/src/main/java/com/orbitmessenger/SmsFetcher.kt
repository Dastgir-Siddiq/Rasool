package com.orbitmessenger

import android.content.Context
import android.provider.Telephony
import com.orbitmessenger.feature.chat.ui.MessageUiModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SmsThread(
    val address: String,
    val snippet: String,
    val timestamp: String
)

object SmsFetcher {

    fun fetchRecentConversations(context: Context): List<SmsThread> {
        val threadsMap = mutableMapOf<String, SmsThread>()
        
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE),
            null,
            null,
            Telephony.Sms.DEFAULT_SORT_ORDER + " LIMIT 500"
        )
        
        cursor?.use {
            val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
            
            val formatter = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())

            while (it.moveToNext()) {
                val address = it.getString(addressIdx) ?: "Unknown"
                if (!threadsMap.containsKey(address)) {
                    val body = it.getString(bodyIdx) ?: ""
                    val dateStr = it.getLong(dateIdx)
                    val timestamp = formatter.format(Date(dateStr))
                    
                    threadsMap[address] = SmsThread(address, body, timestamp)
                }
            }
        }
        
        return threadsMap.values.toList()
    }

    fun fetchMessagesForAddress(context: Context, targetAddress: String): List<MessageUiModel> {
        val messages = mutableListOf<MessageUiModel>()
        
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.TYPE),
            "${Telephony.Sms.ADDRESS} = ?",
            arrayOf(targetAddress),
            Telephony.Sms.DEFAULT_SORT_ORDER + " LIMIT 100"
        )
        
        cursor?.use {
            val idIdx = it.getColumnIndex(Telephony.Sms._ID)
            val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
            val typeIdx = it.getColumnIndex(Telephony.Sms.TYPE)

            val formatter = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())

            while (it.moveToNext()) {
                val id = it.getLong(idIdx).toString()
                val body = it.getString(bodyIdx) ?: ""
                val dateStr = it.getLong(dateIdx)
                val type = it.getInt(typeIdx)
                
                val isFromMe = type == Telephony.Sms.MESSAGE_TYPE_SENT
                val timestamp = formatter.format(Date(dateStr))
                
                messages.add(
                    MessageUiModel(
                        id = id,
                        text = body,
                        isFromMe = isFromMe,
                        timestamp = timestamp,
                        status = if (isFromMe) "SENT" else "RECEIVED"
                    )
                )
            }
        }
        
        return messages.reversed()
    }
    
    fun sendSms(context: Context, address: String, text: String, simIndex: Int = 0) {
        try {
            var smsManager: android.telephony.SmsManager? = null
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as android.telephony.SubscriptionManager
                val activeSubscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
                if (!activeSubscriptionInfoList.isNullOrEmpty() && simIndex < activeSubscriptionInfoList.size) {
                    val subId = activeSubscriptionInfoList[simIndex].subscriptionId
                    smsManager = android.telephony.SmsManager.getSmsManagerForSubscriptionId(subId)
                }
            }
            
            if (smsManager == null) {
                smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    context.getSystemService(android.telephony.SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    android.telephony.SmsManager.getDefault()
                }
            }
            
            // 1. Actually transmit the SMS over the radio
            smsManager?.sendTextMessage(address, null, text, null, null)
            
            // 2. Manually write it to the device's SMS database so it persists 
            // and shows up in other SMS apps (since we aren't the default SMS app).
            val values = android.content.ContentValues().apply {
                put(Telephony.Sms.ADDRESS, address)
                put(Telephony.Sms.BODY, text)
                put(Telephony.Sms.DATE, System.currentTimeMillis())
                put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
                put(Telephony.Sms.READ, 1)
            }
            context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
