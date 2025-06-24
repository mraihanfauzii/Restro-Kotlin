package com.mraihanfauzii.restrokotlin.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mraihanfauzii.restrokotlin.model.MessageChatBot
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatBotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageChatBot)

    @Query("SELECT * FROM chatbot_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageChatBot>>

    @Query("DELETE FROM chatbot_messages")
    suspend fun deleteAllMessages()

    @Update
    suspend fun updateMessage(message: MessageChatBot)
}