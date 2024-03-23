package com.example.justin_yan_myruns5

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class MyDialog: DialogFragment(), DialogInterface.OnClickListener {

    // Referred to and based on Camera Implementation to XD's In Class Notes
    // To handle multiple Dialogs in 1 file
    companion object{
        const val DIALOG_KEY = "KEY"
        const val DIALOG_TITLE = "TITLE"
        const val MANUAL_DIALOG = 1
        const val PHOTO_DIALOG = 2

        const val TAG = "myDialog"
        private const val ARG_TITLE = "argTitle"

        fun newInstance(title: String) = MyDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
            }
        }
    }

    private var title: String? = null

    private lateinit var editText : EditText
    private lateinit var optionList : ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE)
        }
    }

    interface DialogListener{
        fun onInputSet(type: String, input: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog

        // Retrieve value from argument
        val bundle = arguments
        val dialogID = bundle?.getInt(DIALOG_KEY)
        // If dialog is for the manual entry input
        if (dialogID == MANUAL_DIALOG){

            // Sets up Dialog window
            var builder = AlertDialog.Builder(requireActivity())

            var view = requireActivity().layoutInflater.inflate(R.layout.manual_input_dialog, null)

            var title = bundle?.getString(DIALOG_TITLE)

            builder.setView(view)

            builder.setTitle(title)

            editText = view.findViewById<EditText>(R.id.dialog_input)

            // Sets up what type of input field based on the title of the field
            if (title == "Comment") {
                editText.inputType = InputType.TYPE_CLASS_TEXT
                editText.setText("How did it go? Notes here.")
            } else {
                editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            }

            builder.setPositiveButton("OK") { _, _ ->
                // Safety guard in case the user does not input anything
                val input = editText.text
                if (input.isNotEmpty()) {
                    (activity as? MyDialog.DialogListener)?.onInputSet(title.toString(), input.toString())
                }
            }

            builder.setNegativeButton("CANCEL") { dialog, _ ->
                dialog.cancel()
            }
            // Builds the dialog
            ret = builder.create()

        // If dialog is for choosing photo option
        } else if (dialogID == PHOTO_DIALOG) {

            var builder = AlertDialog.Builder(requireActivity())

            var options = arrayOf("Open Camera", "Select from Gallery")

            var view = requireActivity().layoutInflater.inflate(R.layout.photo_dialog, null)

            builder.setView(view)

            builder.setTitle("Pick Profile Picture")

            optionList = view.findViewById<ListView>(R.id.picture_options)
            var arrayAdapter = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_list_item_1, options)
            optionList.adapter = arrayAdapter

            // Builds the dialog
            ret = builder.create()

            // Get what option picked, and sends it back to activity
            optionList.setOnItemClickListener { _, _, position, _ ->
                val element = arrayAdapter.getItem(position)
                sendDataToActivity(element.toString())
                this.dismiss()
            }

        }

        return ret
    }

    // Interface to send string back to activity
    interface OnStringPass {
        fun onStringPassed(data: String)
    }

    private fun sendDataToActivity(data: String) {
        if (context is OnStringPass) {
            (context as OnStringPass).onStringPassed(data)
        }
    }

    override fun onClick(dialogInterface: DialogInterface, id: Int){
    }
}