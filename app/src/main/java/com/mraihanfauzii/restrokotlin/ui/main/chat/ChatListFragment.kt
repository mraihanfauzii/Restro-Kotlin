package com.mraihanfauzii.restrokotlin.ui.main.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.adapter.chat.ChatContactAdapter
import com.mraihanfauzii.restrokotlin.databinding.FragmentChatListBinding
import com.mraihanfauzii.restrokotlin.model.ChatContact
import com.mraihanfauzii.restrokotlin.viewmodel.ChatListViewModel
import java.util.Date

class ChatListFragment : Fragment() {

    private val TAG = "ChatListFragment"
    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private val chatListViewModel: ChatListViewModel by viewModels()
    private lateinit var chatContactAdapter: ChatContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        // Panggil loadChatContacts untuk memuat terapis
        chatListViewModel.loadChatContacts()
    }

    private fun setupRecyclerView() {
        chatContactAdapter = ChatContactAdapter { chatContact ->
            // Cek apakah ini kontak Gemini atau terapis
            if (chatContact.therapistFirebaseUid == "GEMINI_CHATBOT_UID") {
                // Navigasi ke ChatBotDetailFragment
                findNavController().navigate(R.id.action_chatListFragment_to_chatBotDetailFragment)
                Log.d(TAG, "Navigating to ChatBotDetailFragment")
            } else {
                // Navigasi ke ChatDetailFragment untuk terapis
                val bundle = Bundle().apply {
                    putString(ChatDetailFragment.THERAPIST_ID_KEY, chatContact.therapistFirebaseUid)
                    // Jika Anda perlu nama terapis di ChatDetailFragment
                    putString("therapistName", chatContact.therapistName)
                }
                findNavController().navigate(R.id.action_chatListFragment_to_chatDetailFragment, bundle)
                Log.d(TAG, "Navigating to ChatDetailFragment for therapist UID: ${chatContact.therapistFirebaseUid} using NavController")
            }
        }
        binding.rvChatContacts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatContactAdapter
        }
    }

    private fun observeViewModel() {
        chatListViewModel.chatContacts.observe(viewLifecycleOwner) { therapistChatContacts ->
            // Buat ChatContact dummy untuk Gemini
            val geminiChatContact = ChatContact(
                therapistFirebaseUid = "GEMINI_CHATBOT_UID", // ID unik untuk mengidentifikasi Gemini
                therapistName = "Gemini AI Chatbot",
                lastMessage = "Bertanyalah apa saja kepada saya!", // Atau ambil pesan terakhir dari Room DB Gemini
                lastMessageTimestamp = Date() // Bisa timestamp saat ini, atau tanggal tetap jika tidak ada pesan terakhir dari Gemini
            )

            // Urutkan chat terapis berdasarkan lastMessageTimestamp terbaru (descending)
            val sortedTherapistChats = therapistChatContacts.sortedByDescending { it.lastMessageTimestamp }

            // Gabungkan list: Gemini di paling atas, diikuti oleh terapis yang sudah diurutkan
            val combinedChatList = mutableListOf<ChatContact>()
            combinedChatList.add(geminiChatContact)
            combinedChatList.addAll(sortedTherapistChats)

            chatContactAdapter.submitList(combinedChatList)
            Log.d(TAG, "Chat contacts updated: ${combinedChatList.size} contacts (including Gemini)")
            binding.progressBarChatList.visibility = View.GONE
        }

        chatListViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarChatList.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        chatListViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                Log.e(TAG, "Error loading chat contacts: $it")
                chatListViewModel.clearErrorMessage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}