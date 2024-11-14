package com.capstone.gagambrawl.view.Dashboard

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import com.capstone.gagambrawl.R
import com.google.android.material.imageview.ShapeableImageView


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class InventoryFragment : Fragment() {
    // TODO: Rename and change types of parameters
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
         val view = inflater.inflate(R.layout.fragment_inventory, container, false)

        val addBtn: ShapeableImageView = view.findViewById(R.id.addBtn)
        addBtn.setOnClickListener {
            // Create a dialog with a custom layout
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_add_spider)  // Replace with the actual dialog layout name

            // Set fade-in animation when the dialog shows
            dialog.window?.attributes?.windowAnimations = R.style.DialogFadeAnimation

            // Find the close button inside the dialog's layout
            val closeBtn: ImageButton = dialog.findViewById(R.id.i_close_btn)  // Replace with the actual ID of the close button

            // Set an OnClickListener to dismiss the dialog when the close button is clicked
            closeBtn.setOnClickListener {
                dialog.dismiss()  // Close the dialog
            }

            // Show the dialog
            dialog.show()
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
         * @return A new instance of fragment InventoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InventoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}