package ca.georgiancollege.assignment2

data class MovieModel(
    val id: String = "",
    val title: String = "",
    val director: String = "",
    val rating: String = "",
    val year: String = "",
    val posterUrl: String = ""
) {
    constructor() : this("", "", "", "", "", "")
}