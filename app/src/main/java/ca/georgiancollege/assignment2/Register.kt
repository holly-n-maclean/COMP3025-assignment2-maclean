package ca.georgiancollege.assignment2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ca.georgiancollege.assignment2.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class Register : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth;
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.regButton.setOnClickListener {
            registerUser("tas@auth.com", "password")
        }

    }

    private fun registerUser(email: String, password: String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                } else {
                    Log.e("Register", "Registration failed", task.exception)
                    Toast.makeText(this,
                        "Registration failed",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}