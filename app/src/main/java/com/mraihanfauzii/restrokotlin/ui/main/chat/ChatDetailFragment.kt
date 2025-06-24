package com.mraihanfauzii.restrokotlin.ui.main.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mraihanfauzii.restrokotlin.adapter.chat.ChatTerapisAdapter
import com.mraihanfauzii.restrokotlin.databinding.FragmentChatDetailBinding
import com.mraihanfauzii.restrokotlin.viewmodel.ChatTerapisViewModel
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager

class ChatDetailFragment : Fragment() {

    private val TAG = "ChatDetailFragment" // Ubah TAG
    private var _binding: FragmentChatDetailBinding? = null
    private val binding get() = _binding!!

    private val chatViewModel: ChatTerapisViewModel by viewModels()
    private lateinit var chatAdapter: ChatTerapisAdapter

    private var targetTherapistFirebaseUid: String? = null
    private var patientFirebaseUid: String? = null
    private var patientNameFromAzure: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authManager = AuthenticationManager(requireContext())

        patientFirebaseUid = authManager.getAccess(AuthenticationManager.FIREBASE_UID)
        patientNameFromAzure = authManager.getAccess(AuthenticationManager.NAMALENGKAP)

        if (patientFirebaseUid.isNullOrEmpty() || patientNameFromAzure.isNullOrEmpty()) {
            Log.e(TAG, "Patient Firebase UID or name not found in AuthenticationManager. Cannot initialize chat fully.")
            Toast.makeText(requireContext(), "Error: Gagal mendapatkan data pengguna. Pastikan Anda login Firebase.", Toast.LENGTH_LONG).show()
            activity?.finish()
            return
        }

        arguments?.let {
            targetTherapistFirebaseUid = it.getString(THERAPIST_ID_KEY)
        }

        if (targetTherapistFirebaseUid.isNullOrEmpty()) {
            Log.e(TAG, "Therapist Firebase UID is null. Cannot initialize chat.")
            Toast.makeText(requireContext(), "Error: ID terapis tidak ditemukan.", Toast.LENGTH_LONG).show()
            activity?.supportFragmentManager?.popBackStack()
            return
        }

        chatViewModel.setChatParticipants(
            patientFirebaseUid!!,
            targetTherapistFirebaseUid!!,
            patientNameFromAzure!!
        )
        Log.d(TAG, "Chat initiated with Patient Firebase UID: $patientFirebaseUid, Therapist Firebase UID: $targetTherapistFirebaseUid, Patient Name: $patientNameFromAzure")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserIdForAdapter = chatViewModel.getFirebaseCurrentUserUid()
        if (currentUserIdForAdapter == null) {
            Log.e(TAG, "Firebase UID is null at onViewCreated. Chat adapter might not display messages correctly.")
            Toast.makeText(requireContext(), "Pengguna belum terautentikasi Firebase. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
            binding.buttonSendMessage.isEnabled = false
            return
        }

        chatAdapter = ChatTerapisAdapter(currentUserIdForAdapter)
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }

        observeViewModel()

        binding.buttonSendMessage.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                if (chatViewModel.currentUserId != null && chatViewModel.therapistId != null) {
                    chatViewModel.sendMessage(messageText)
                    binding.editTextMessage.text.clear()
                } else {
                    Toast.makeText(requireContext(), "Informasi chat belum lengkap. Harap tunggu atau coba lagi.", Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "Attempted to send message before ViewModel was fully initialized with UIDs.")
                }
            } else {
                Toast.makeText(context, "Pesan tidak boleh kosong.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            chatAdapter.submitList(messages) {
                if (messages.isNotEmpty()) {
                    binding.recyclerViewChat.smoothScrollToPosition(messages.size - 1)
                }
            }
            Log.d(TAG, "Messages updated in UI: ${messages.size}")
            binding.buttonSendMessage.isEnabled = true
        }

        chatViewModel.sendMessageStatus.observe(viewLifecycleOwner) { success ->
            if (!success) {
                Toast.makeText(context, "Gagal mengirim pesan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val THERAPIST_ID_KEY = "THERAPIST_ID_KEY"

        fun newInstance(therapistFirebaseUid: String): ChatDetailFragment { // Ubah di sini juga
            val fragment = ChatDetailFragment()
            val args = Bundle()
            args.putString(THERAPIST_ID_KEY, therapistFirebaseUid)
            fragment.arguments = args
            return fragment
        }
    }
}