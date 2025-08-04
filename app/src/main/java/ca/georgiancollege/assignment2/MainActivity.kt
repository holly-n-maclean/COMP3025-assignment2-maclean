package ca.georgiancollege.assignment2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ca.georgiancollege.assignment2.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: MovieAdapter
    private val db = Firebase.firestore
    private val movieList = mutableListOf<MovieModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        adapter = MovieAdapter(
            movieList,
            onEdit = { movie ->
                val intent = Intent(this, MovieEditsActivity::class.java)
                intent.putExtra("movieId", movie.id)
                startActivity(intent)
            },
            onDelete = { movie ->
                db.collection("users").document(userId)
                    .collection("movies").document(movie.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Movie deleted", Toast.LENGTH_SHORT).show()
                        loadMovies(userId)
                    }
            }
        )

        binding.logoutButton.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.addButton.setOnClickListener {
            startActivity(Intent(this, MovieEditsActivity::class.java))
        }

        loadMovies(userId)
    }

    private fun loadMovies(userId: String) {
        db.collection("users").document(userId)
            .collection("movies")
            .limit(20)
            .get()
            .addOnSuccessListener { result ->
                movieList.clear()
                for (doc in result) {
                    val movie = doc.toObject(MovieModel::class.java)
                    movieList.add(movie)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading movies", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        auth.currentUser?.uid?.let { loadMovies(it) }
    }
}