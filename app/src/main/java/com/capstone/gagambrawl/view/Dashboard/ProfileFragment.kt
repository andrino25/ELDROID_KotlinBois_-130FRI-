package com.capstone.gagambrawl.view.Dashboard

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RelativeLayout
import com.capstone.gagambrawl.view.Authentication.LoginPage
import com.capstone.gagambrawl.R
import androidx.appcompat.app.AlertDialog
import com.capstone.gagambrawl.databinding.FragmentProfileBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var customDialog: Dialog? = null  // Added to store dialog reference
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val addBtn: RelativeLayout = view.findViewById(R.id.logoutButton)
        addBtn.setOnClickListener {
            // Create and store dialog reference
            customDialog = Dialog(requireContext()).apply {
                setContentView(R.layout.dialog_logout)
                window?.attributes?.windowAnimations = R.style.DialogFadeAnimation

                val closeBtn: ImageButton = findViewById(R.id.i_close_btn)
                val logoutBtn: Button = findViewById(R.id.dialog_logoutAcc)

                closeBtn.setOnClickListener {
                    dismiss()
                }

                logoutBtn.setOnClickListener {
                    val intent = Intent(requireContext(), LoginPage::class.java)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slow_fade_in, R.anim.slow_fade_out)
                    requireActivity().finish()
                }

                show()
            }
        }

        val helpCenterButton: RelativeLayout = view.findViewById(R.id.helpCenterButton)
        helpCenterButton.setOnClickListener {
            val intent = Intent(requireContext(), HelpCenter::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0)
        }

        return view
    }

    override fun onDestroyView() {
        // Dismiss dialog if it's showing
        customDialog?.dismiss()
        customDialog = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}