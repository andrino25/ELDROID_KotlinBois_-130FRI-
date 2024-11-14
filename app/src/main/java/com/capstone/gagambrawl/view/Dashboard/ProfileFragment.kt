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

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val addBtn: RelativeLayout = view.findViewById(R.id.logoutButton)
        addBtn.setOnClickListener {
            // Create a dialog with a custom layout
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_logout)  // Replace with the actual dialog layout name

            // Set fade-in animation when the dialog shows
            dialog.window?.attributes?.windowAnimations = R.style.DialogFadeAnimation

            // Find the close button inside the dialog's layout
            val closeBtn: ImageButton = dialog.findViewById(R.id.i_close_btn)
            val logoutBtn: Button = dialog.findViewById(R.id.dialog_logoutAcc)

            // Set an OnClickListener to dismiss the dialog when the close button is clicked
            closeBtn.setOnClickListener {
                dialog.dismiss()  // Close the dialog
            }
            logoutBtn.setOnClickListener {
                val intent = Intent(requireContext(), LoginPage::class.java)
                startActivity(intent)

                requireActivity().overridePendingTransition(R.anim.slow_fade_in, R.anim.slow_fade_out)
                // Optionally, finish the current activity if you want to close the Profile activity after logout
                requireActivity().finish()
            }

            // Show the dialog
            dialog.show()
        }

        val helpCenterButton: RelativeLayout = view.findViewById(R.id.helpCenterButton)
        helpCenterButton.setOnClickListener {
            val intent = Intent(requireContext(), HelpCenter::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0) // No transition animation
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
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