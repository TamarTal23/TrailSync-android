package com.idz.trailsync.features.chatbot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.idz.trailsync.databinding.FragmentChatbotBinding

class ChatbotFragment : Fragment() {
    private lateinit var chatAdapter: ChatAdapter
    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatbotViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupMessageInput()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()

        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }
    }

    private fun setupMessageInput() {
        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()

            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                binding.messageInput.text?.clear()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            val visibleMessages = messages.filter {
                it.role != "system"
            }
            chatAdapter.setMessages(visibleMessages)
            binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.shimmerContainer.visibility = View.VISIBLE
                binding.shimmerContainer.startShimmer()
                binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
            } else {
                binding.shimmerContainer.stopShimmer()
                binding.shimmerContainer.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
