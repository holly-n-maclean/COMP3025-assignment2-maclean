package ca.georgiancollege.assignment2

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import ca.georgiancollege.assignment2.databinding.MoviesBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.net.URL
import kotlin.concurrent.thread

class MovieAdapter(
    private val movies: List<MovieModel>,
    private val onEdit: (MovieModel) -> Unit,
    private val onDelete: (MovieModel) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(val binding: MoviesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: MovieModel) {
            val context = binding.root.context
            binding.titleText.text = movie.title
            binding.yearText.text = movie.year
            binding.ratingText.text = "Rating: ${movie.rating}"

            thread {
                try {
                    val input = URL(movie.posterUrl).openStream()
                    val bitmap = BitmapFactory.decodeStream(input)
                    (binding.posterImage.context as Activity).runOnUiThread {
                        binding.posterImage.setImageBitmap(bitmap)
                    }
                } catch (_: Exception) {}
            }

            binding.editButton.setOnClickListener {
                val intent = Intent(context, MovieEditsActivity::class.java)
                intent.putExtra("movieId", movie.id)
                context.startActivity(intent)
            }
            binding.deleteButton.setOnClickListener {
                val userId = Firebase.auth.currentUser?.uid
                if (userId != null) {
                    Firebase.firestore.collection("users")
                        .document(userId)
                        .collection("movies")
                        .document(movie.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(binding.root.context, "Movie deleted", Toast.LENGTH_SHORT).show()
                            onDelete(movie)
                        }
                        .addOnFailureListener {
                            Toast.makeText(binding.root.context, "Failed to delete", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = MoviesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount() = movies.size
}

