package com.example.justin_yan_myruns5

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProfileActivity : AppCompatActivity(), MyDialog.OnStringPass {
    private lateinit var imageView: ImageView
    private lateinit var nameView: EditText
    private lateinit var emailView: EditText
    private lateinit var phoneView: EditText
    private lateinit var radioGroup: RadioGroup
    private lateinit var classView: EditText
    private lateinit var majorView: EditText

    private lateinit var sharedPref: SharedPreferences
    private lateinit var photoOption: String

    private lateinit var textView: TextView
    private lateinit var photoButton: Button
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var tempImgUri: Uri
    private lateinit var myViewModel: MyViewModel
    private lateinit var cameraResult: ActivityResultLauncher<Intent>

    //  Referred to and based on Camera Implementation to XD's In Class Notes
    private val tempImgFileName = "myrun4_temp_img.jpg"
    private val oldImgFileName = "old_img.jpg"

//  -----

    // This is listener for when dialog passes back which photo option the user picks, and launches it
    override fun onStringPassed(data: String) {
        if (data == "Open Camera") {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
            cameraResult.launch(intent)
        } else if (data == "Select from Gallery") {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            cameraResult.launch(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)
        imageView = findViewById(R.id.imageProfile)
        photoButton = findViewById(R.id.btnChangePhoto)
        saveButton = findViewById(R.id.btnSave)
        cancelButton = findViewById(R.id.btnCancel)

        nameView = findViewById(R.id.nameInput)
        emailView = findViewById(R.id.emailInput)
        phoneView = findViewById(R.id.phoneInput)
        radioGroup = findViewById(R.id.radioGender)
        classView = findViewById(R.id.classInput)
        majorView = findViewById(R.id.majorInput)
        photoOption = ""

        val tempImgFile = File(getExternalFilesDir(null), tempImgFileName)
        val prevImgFile = File(getExternalFilesDir(null), oldImgFileName)
        tempImgUri = FileProvider.getUriForFile(this, "com.example.justin_yan_myruns5", tempImgFile)
        //  ----

        //  Displaying Saved Profile Information
        //  - Based on code from: GeeksforGeeks and Android Geek on Youtube (https://www.youtube.com/watch?v=p0nhM5irW7Y&t=942s)
        sharedPref = getSharedPreferences("profilePref", MODE_PRIVATE)

        val userName = sharedPref.getString("name", null)
        val email = sharedPref.getString("email", null)
        val phone = sharedPref.getInt("phone", -1)
        val buttonID = sharedPref.getInt("gender", -1)
        val radioButton : RadioButton? = findViewById<RadioButton>(buttonID)
        val yearClass = sharedPref.getInt("class", -1)
        val major = sharedPref.getString("major", null)

        nameView.setText(userName)
        emailView.setText(email)
        if (phone == -1)
            phoneView.setText("")
        else
            phoneView.setText(phone.toString())
        if (radioButton != null)
            radioButton.isChecked = true
        if (yearClass == -1)
            classView.setText("")
        else
            classView.setText(yearClass.toString())
        majorView.setText(major)

        // When user clicks the save button, saves all information placed
        saveButton.setOnClickListener {
            val editor = sharedPref.edit()
            editor.clear()

            editor.putString("name", nameView.text.toString())

            editor.putString("email", emailView.text.toString())

            if (phoneView.text.toString().isEmpty()) {
                editor.putInt("phone", -1)
            } else {
                editor.putInt("phone", phoneView.text.toString().toInt())
            }

            val buttonID = radioGroup.checkedRadioButtonId
            editor.putInt("gender", buttonID)

            if (classView.text.toString().isEmpty()) {
                editor.putInt("class", -1)
            } else {
                editor.putInt("class", classView.text.toString().toInt())
            }

            editor.putString("major", majorView.text.toString())

            editor.commit()

            finish()
        }

        // Discards all changes when cancel button clicked
        cancelButton.setOnClickListener(){
            //  Goes back to old saved picture
            tempImgFile.delete()
            if (prevImgFile.exists())
                prevImgFile.copyTo(tempImgFile)

            finish()
        }
        //  ----

        //  Implicit Intent (Detects Photo Button and opens Camera App)
        //  Referred to and based on Camera Implementation to XD's In Class Notes
        photoButton.setOnClickListener(){
            if (tempImgFile.exists()) {
                prevImgFile.delete()
                tempImgFile.copyTo(prevImgFile)
            }

            // Dialog setup has been based on Prof's (XD's) lecture/demo code
            // Open dialog window
            val dialog = MyDialog()

            val bundle = Bundle()
            bundle.putInt(MyDialog.DIALOG_KEY, MyDialog.PHOTO_DIALOG)
            dialog.arguments = bundle
            dialog.show(supportFragmentManager, "tag")

        }

        cameraResult = registerForActivityResult(StartActivityForResult()){ result: ActivityResult ->
            // If they picked the camera option
            if (result.data == null) {
                if(result.resultCode == Activity.RESULT_OK){
                    val bitmap = Util.getBitmap(this, tempImgUri)
                    myViewModel.userImage.value = bitmap
                }

            // If they picked the gallery option
            } else {
                if(result.resultCode == Activity.RESULT_OK){
                    // Gets uri from result to get the bitmap
                    val uri : Uri = Uri.parse(result.data!!.data.toString())
                    val bitmap = Util.getBitmap(this, uri)
                    myViewModel.userImage.value = bitmap

                    // Saves the file in the app directory
                    var inputStream : InputStream = contentResolver.openInputStream(uri)!!
                    var outputStream = FileOutputStream(tempImgFile)
                    inputStream.copyTo(outputStream)
                }
            }
        }

        myViewModel = ViewModelProvider(this)[MyViewModel::class.java]
        myViewModel.userImage.observe(this) { it ->
            imageView.setImageBitmap(it)
        }

        if(tempImgFile.exists()) {
            val bitmap = Util.getBitmap(this, tempImgUri)
            imageView.setImageBitmap(bitmap)
        } else {
            //  Add a default picture when theres none taken
            Picasso.get().load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTACuCOdy8vswKsoGkBqUPiE8kI0DMWXdPC_OHjY5nBzLDuoIaE").into(imageView)
        }
        // ------
    }


    //  Display Menu Bar
    //  Referred to Android Developers Page (https://developer.android.com/develop/ui/views/components/menus)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }
    //  ----

}