package com.client.hardware.project.first.UI

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.client.hardware.project.first.UI.ui.theme.Freelance_p1Theme
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class LoginAdmin : ComponentActivity() {
    private lateinit var firebase_analytics: FirebaseAnalytics
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseDatabase: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Freelance_p1Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        // Text fields for email and password
        var mob_no by remember { mutableStateOf("") }

        OutlinedTextField(
            value = mob_no,
            onValueChange = { mob_no = it },
            label = { Text("Mobile Number") },
            leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) }
        )


        // Button for login
        Button(
            onClick = { PerformLogin(mob_no, context) },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Log in")
        }
    }
}

fun PerformLogin(mob_no: String, context: Context) {
    val package_name: String = context.packageName.toString().substring(
        context.packageName.toString().lastIndexOf('.')+1,
        context.packageName.toString().length
    )
    val data_base_ref: FirebaseDatabase = FirebaseDatabase.getInstance()
    val packageReference: DatabaseReference =
        data_base_ref.getReference().child(package_name)
    packageReference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var admin_mob_no: String? = null
            for (productSnapshot in snapshot.children) {
                admin_mob_no = productSnapshot.value.toString()
                Log.d("Firebase Data", "Admin mobile number: $admin_mob_no")
                if (mob_no == admin_mob_no) {
                    context.startActivity(Intent(context, admin_add_data::class.java))
                } else {
                    context.startActivity(Intent(context, MainActivity::class.java))
                }
            }

        }

        override fun onCancelled(error: DatabaseError) {}
    })
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Freelance_p1Theme {
        LoginScreen()
    }
}