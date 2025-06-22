// com/mraihanfauzii/restrokotlin/ui/main/exercise/detect/ChatFragment.kt
package com.mraihanfauzii.restrokotlin.ui.main.exercise.detect // Perhatikan package name yang Anda gunakan

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mraihanfauzii.restrokotlin.adapter.chat.MessageAdapter
import com.mraihanfauzii.restrokotlin.databinding.FragmentChatBinding
import com.mraihanfauzii.restrokotlin.viewmodel.ChatBotViewModel

class ChatFragment : Fragment() {

    private val TAG = "ChatFragment"
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatBotViewModel: ChatBotViewModel
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatBotViewModel = ViewModelProvider(this).get(ChatBotViewModel::class.java)

        setupRecyclerView()
        setupMessageInput()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(mutableListOf())
        binding.rvChatMessages.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true // Pesan baru akan muncul di bagian bawah
            }
            adapter = messageAdapter
        }
    }

    private fun setupMessageInput() {
        binding.btnSendMessage.setOnClickListener {
            val messageText = binding.etMessageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                chatBotViewModel.sendMessage(messageText)
                binding.etMessageInput.text.clear() // Bersihkan input setelah mengirim
            } else {
                Toast.makeText(context, "Pesan tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        chatBotViewModel.messages.observe(viewLifecycleOwner) { messages ->
            Log.d(TAG, "Messages updated: ${messages.size} messages")
            messageAdapter.updateMessages(messages)
            binding.rvChatMessages.scrollToPosition(messages.size - 1) // Gulir ke pesan terbaru
        }

        chatBotViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarChat.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        chatBotViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                Log.e(TAG, "Error: $it")
                chatBotViewModel.clearErrorMessage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}