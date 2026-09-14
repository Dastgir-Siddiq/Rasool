package com.orbitmessenger.core.database.dao

import androidx.room.*
import com.orbitmessenger.core.database.entity.MessageEntity
import com.orbitmessenger.core.database.entity.MessageStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)
    
    @Query("SELECT * FROM messages WHERE status = 'SENDING' OR status = 'FAILED'")
    suspend fun getPendingMessages(): List<MessageEntity>
}
