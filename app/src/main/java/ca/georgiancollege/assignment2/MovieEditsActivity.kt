package ca.georgiancollege.assignment2


import android.os.Bundle
import android.util.Log
import java.net.URLEncoder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ca.georgiancollege.assignment2.databinding.ActivityMovieEditsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MovieEditsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMovieEditsBinding
    private val db = Firebase.firestore
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieEditsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fetchButton.setOnClickListener {
            val title = binding.titleInput.text.toString().trim()
            if (title.isNotEmpty()) {
                getMovieByTitle(title)
            } else {
                Toast.makeText(this, "Enter a movie title", Toast.LENGTH_SHORT).show()
            }
        }

        val movieId = intent.getStringExtra("movieId")
        if (movieId != null) {
            loadMovieForEdit(movieId)
            binding.saveButton.text = "Update Movie"
        }


        binding.saveButton.setOnClickListener {
            saveMovie()
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }
    }

    private val apiClient = ApiClient()

    private fun getMovieByTitle(title: String) {
        val encodedTitle = URLEncoder.encode(title, "UTF-8")
        val url = "https://www.omdbapi.com/?apikey=1f274e66&t=$encodedTitle"

        apiClient.get(url, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MovieEditsActivity, "API error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { json ->
                    val jsonObject = JSONObject(json)
                    if (jsonObject.getString("Response") == "True") {
                        val movie = MovieModel(
                            id = jsonObject.getString("imdbID"),
                            title = jsonObject.getString("Title"),
                            year = jsonObject.getString("Year"),
                            director = jsonObject.getString("Director"),
                            rating = jsonObject.getString("imdbRating"),
                            posterUrl = jsonObject.getString("Poster")
                        )

                        runOnUiThread {
                            setMovieDetails(movie)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@MovieEditsActivity, "Movie not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun setMovieDetails(movie: MovieModel) {
        currentMovie = movie
        binding.yearInput.setText(movie.year)
        binding.directorInput.setText(movie.director)
        binding.ratingInput.setText(movie.rating)
        binding.posterUrlInput.setText(movie.posterUrl)
    }

    private var currentMovie: MovieModel? = null

    private fun saveMovie() {
        val movie = currentMovie ?: return Toast.makeText(this, "Fetch movie first", Toast.LENGTH_SHORT).show()
        val userId = Firebase.auth.currentUser?.uid ?: return

        val updatedMovie = MovieModel(
            id = movie.id,
            title = binding.titleInput.text.toString(),
            year = binding.yearInput.text.toString(),
            director = binding.directorInput.text.toString(),
            rating = binding.ratingInput.text.toString(),
            posterUrl = binding.posterUrlInput.text.toString()
        )

        Firebase.firestore.collection("users")
            .document(userId)
            .collection("movies")
            .document(updatedMovie.id)
            .set(updatedMovie)
            .addOnSuccessListener {
                Toast.makeText(this, "Movie updated!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update movie", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadMovieForEdit(movieId: String) {
        val userId = Firebase.auth.currentUser?.uid ?: return

        Firebase.firestore.collection("users")
            .document(userId)
            .collection("movies")
            .document(movieId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val movie = document.toObject(MovieModel::class.java)
                    movie?.let {
                        currentMovie = it
                        binding.titleInput.setText(it.title)
                        binding.yearInput.setText(it.year)
                        binding.directorInput.setText(it.director)
                        binding.ratingInput.setText(it.rating)
                        binding.posterUrlInput.setText(it.posterUrl)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load movie for editing", Toast.LENGTH_SHORT).show()
            }
    }
}
