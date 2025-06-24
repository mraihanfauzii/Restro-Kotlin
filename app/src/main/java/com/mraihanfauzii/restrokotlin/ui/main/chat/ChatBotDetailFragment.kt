package com.mraihanfauzii.restrokotlin.ui.main.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.adapter.chat.ChatBotAdapter
import com.mraihanfauzii.restrokotlin.viewmodel.ChatBotViewModel

class ChatBotDetailFragment : Fragment() {

    private lateinit var viewModel: ChatBotViewModel
    private lateinit var chatBotAdapter: ChatBotAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_bot_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))
            .get(ChatBotViewModel::class.java)

        recyclerView = view.findViewById(R.id.recyclerViewChatBot)
        messageEditText = view.findViewById(R.id.editTextMessageBot)
        sendButton = view.findViewById(R.id.buttonSendBot)

        chatBotAdapter = ChatBotAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = chatBotAdapter

        viewModel.chatMessages.observe(viewLifecycleOwner) { messages ->
            chatBotAdapter.submitList(messages) {
                recyclerView.scrollToPosition(chatBotAdapter.itemCount - 1)
            }
        }

        sendButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                messageEditText.text.clear()
            }
        }
    }
}