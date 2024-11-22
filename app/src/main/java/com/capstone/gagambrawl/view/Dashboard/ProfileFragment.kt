package com.capstone.gagambrawl.view.Dashboard

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.capstone.gagambrawl.view.Authentication.LoginPage
import com.capstone.gagambrawl.R
import com.capstone.gagambrawl.api.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileFragment : Fragment() {
    private var token: String = ""
    private var firstName: String? = null
    private var middleName: String? = null
    private var lastName: String? = null
    private var email: String? = null
    private var address: String? = null
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profileImage: ImageView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val EDIT_PROFILE_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            token = it.getString("token", "")
            firstName = it.getString("firstName")
            middleName = it.getString("middleName")
            lastName = it.getString("lastName")
            email = it.getString("email")
            address = it.getString("address")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize views
        profileName = view.findViewById(R.id.profileName)
        profileEmail = view.findViewById(R.id.profileEmail)
        profileImage = view.findViewById(R.id.profileImage)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            refreshUserData()
        }

        // Set color scheme for the refresh animation
        swipeRefreshLayout.setColorSchemeResources(
            R.color.maroon,
            R.color.black
        )

        // Set user information from arguments
        setUserInformation()

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

        // Modify the Edit Profile button click listener
        val editProfileButton: RelativeLayout = view.findViewById(R.id.editProfileButton)
        editProfileButton.setOnClickListener {
            val intent = Intent(requireContext(), EditProfilePage::class.java)
            intent.apply {
                putExtra("token", token)
                putExtra("firstName", firstName)
                putExtra("middleName", middleName)
                putExtra("lastName", lastName)
                putExtra("email", email)
                putExtra("address", address)
            }
            startActivityForResult(intent, EDIT_PROFILE_REQUEST)
            requireActivity().overridePendingTransition(R.anim.slow_fade_in, R.anim.slow_fade_out)
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == Activity.RESULT_OK) {
            // Refresh user data when returning from edit profile
            refreshUserData()
        }
    }

    private fun refreshUserData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://gagambrawl-api.vercel.app/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val apiService = retrofit.create(ApiService::class.java)
                val user = apiService.getUserProfile("Bearer $token")

                // Update the stored data
                firstName = user.userFirstName
                middleName = user.userMiddleName
                lastName = user.userLastName
                email = user.email
                address = user.userAddress

                // Update the UI
                setUserInformation()

                // Stop the refreshing animation
                swipeRefreshLayout.isRefreshing = false

            } catch (e: Exception) {
                // Handle error
                activity?.runOnUiThread {
                    Toast.makeText(context, "Failed to refresh: ${e.message}", Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun setUserInformation() {
        // Set the name, handling null values properly
        val fullName = buildString {
            firstName?.let { append(it) }
            if (!middleName.isNullOrEmpty()) append(" $middleName")
            lastName?.let { append(" $it") }
        }.trim()
        
        profileName.text = if (fullName.isNotEmpty()) fullName else "Loading..."
        profileEmail.text = email ?: "Loading..."
        profileImage.setImageResource(R.drawable.img_pfp)
    }
}