package com.client.hardware.project.first.UI

import android.R.attr.name
import android.R.id
import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.client.hardware.project.first.R
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.pixplicity.easyprefs.library.Prefs
import java.io.File


class admin_add_data : AppCompatActivity() {
    private lateinit var etProductName: EditText
    private lateinit var etProductId: EditText
    private lateinit var imgProduct: ImageView
    private lateinit var etPricePerUnit: EditText
    private lateinit var etQuality: EditText
    private lateinit var etDimensions: EditText
    private lateinit var btnSelectImage: Button
    private lateinit var btnSubmit: Button
    private lateinit var selectedImageUri: Uri
    private lateinit var continue_to_user: TextView
    private lateinit var firebase_analytics: FirebaseAnalytics
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseDatabase: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_add_data)
        Prefs.Builder()
            .setContext(this)
            .setMode(MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()
        Prefs.putBoolean("is_admin", false)
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebase_analytics = Firebase.analytics
        firebase_analytics.setUserProperty("skill_level", "admin")
        val bundle = Bundle()
        bundle.putString("skill_level", "admin")
        firebase_analytics.logEvent("event_good", bundle)
        etProductName = findViewById(R.id.etProductName)
        etProductId = findViewById(R.id.etProductId)
        imgProduct = findViewById(R.id.imgProduct)
        etPricePerUnit = findViewById(R.id.etPricePerUnit)
        etQuality = findViewById(R.id.etQuality)
        etDimensions = findViewById(R.id.etDimensions)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSubmit = findViewById(R.id.btnSubmit)
        continue_to_user = findViewById(R.id.tv_to_the_user)
        continue_to_user.setOnClickListener {
            startActivity(Intent(this@admin_add_data, MainActivity::class.java))
        }
        setimagedefault()
        btnSelectImage.setOnClickListener {
            openImagePicker()
        }
        btnSubmit.setOnClickListener {
            Toast.makeText(this, "Submitting...", Toast.LENGTH_LONG).show()
            submitDataToFirebase()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    handleSelectedImage(data.data)
                }
            }
        }

    private fun handleSelectedImage(imageUri: Uri?) {
        if (imageUri != null) {
            val imageFile = File(imageUri.path)
            val fileSizeInBytes = imageFile.length()
            val fileSizeInKB = fileSizeInBytes / 1024
            val fileSizeInMB = fileSizeInKB / 1024
            if (fileSizeInMB <= 2) {
                selectedImageUri = imageUri

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(contentResolver, selectedImageUri)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    imgProduct.setImageBitmap(bitmap)
                } else {
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                    imgProduct.setImageBitmap(bitmap)
                }
            } else {
                Toast.makeText(this@admin_add_data, "Size greater than 1 mb", Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            setimagedefault()
        }
    }

    private fun submitDataToFirebase() {
        val package_name: String = this.packageName.toString().substring(
            this.packageName.toString().lastIndexOf('.')+1,
            this.packageName.toString().length
        )
        val productName = etProductName.text.toString()
        val productId = etProductId.text.toString()
        val pricePerUnit = etPricePerUnit.text.toString()
        val quality = etQuality.text.toString()
        val dimensions = etDimensions.text.toString()
        val imageUri: Uri = selectedImageUri

        if (imageUri != null && productName.isNotEmpty() && productId.isNotEmpty() && pricePerUnit.isNotEmpty()
            && quality.isNotEmpty() && dimensions.isNotEmpty()
        ) {
            val imageRef = firebaseStorage.reference.child(package_name).child("product_images").child(productId)
            val databaseRef =
                firebaseDatabase.reference.child(package_name).child("products").child(productId)
            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(
                            this@admin_add_data, "Product with the same ID already exists.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val uploadTask = imageRef.putFile(imageUri)
                        uploadTask.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Image upload is successful, get the download URL
                                imageRef.downloadUrl.addOnSuccessListener { uri ->
                                    val imageUrl = uri.toString()

                                    val productData = HashMap<String, Any>()
                                    productData["productName"] = productName
                                    productData["pricePerUnit"] = pricePerUnit
                                    productData["quality"] = quality
                                    productData["dimensions"] = dimensions
                                    productData["imageUrl"] = imageUrl

                                    databaseRef.setValue(productData)
                                        .addOnCompleteListener { databaseTask ->
                                            if (databaseTask.isSuccessful) {
                                                Toast.makeText(
                                                    this@admin_add_data,
                                                    "Success",
                                                    Toast.LENGTH_LONG
                                                )
                                                    .show()
                                                clearFormFields()
                                            } else {
                                                Toast.makeText(
                                                    this@admin_add_data,
                                                    "Data Not submitted",
                                                    Toast.LENGTH_LONG
                                                )
                                                    .show()

                                            }
                                        }
                                }
                            } else {
                                Toast.makeText(
                                    this@admin_add_data,
                                    task.exception.toString(),
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        } else {
            Toast.makeText(this, "Some details are missing", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearFormFields() {
        etProductName.text.clear()
        etProductId.text.clear()
        etPricePerUnit.text.clear()
        etQuality.text.clear()
        etDimensions.text.clear()
        setimagedefault()
    }

    private fun setimagedefault() {
        val defaultImageResId = R.drawable.icn_default_iv
        imgProduct.setImageResource(defaultImageResId)
        selectedImageUri = Uri.parse("android.resource://$packageName/$defaultImageResId")
    }
}