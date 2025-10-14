import java.util.*;

class Movie {
    private String title;
    private int id;
    public Movie(String title, int id){
        this.id = id;
        this.title = title;
    }
    public int getId(){
        return this.id;
    }
    public String getTitle(){
        return this.title;
    }
}

class User {
    private int id;
    private String name;
    public User(int id, String name){
        this.id = id;
        this.name = name;
    }
    public int getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
}

enum MovieRating {
    NOT_RATED,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE
}

class RatingRegister {
    private Map<Integer, List<Movie>> userMovies;
    private Map<Integer, HashMap<Integer, MovieRating>> movieRatings;

    private List<Movie> movies;
    private List<User> users;

    public RatingRegister(){
        this.userMovies = new HashMap<>();
        this.movieRatings = new HashMap<>();
        this.movies = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    public void addRating(User user, Movie movie, MovieRating rating){
        if(!this.movieRatings.containsKey(movie.getId())){
            this.movieRatings.put(movie.getId(), new HashMap<>());
            this.movies.add(movie);
        }
        if(!this.userMovies.containsKey(user.getId())){
            this.userMovies.put(user.getId(), new ArrayList<>());
            this.users.add(user);
        }
        this.userMovies.get(user.getId()).add(movie);
        this.movieRatings.get(movie.getId()).put(user.getId(), rating);
    }

    public double getAverageRating(Movie movie){
        if(!this.movieRatings.containsKey(movie.getId())){
            return MovieRating.NOT_RATED.ordinal();
        }
        Map<Integer, MovieRating> ratings = this.movieRatings.get(movie.getId());
        int sum = 0;
        for(MovieRating rating: ratings.values()){
            sum += rating.ordinal();
        }
        return (double) sum / ratings.size();
    }

    public List<User> getUsers(){
        return this.users;
    }
    public List<Movie> getMovies(){
        return this.movies;
    }

    public List<Movie> getUserMovies(User user){
        if(!this.userMovies.containsKey(user.getId())){
            return new ArrayList<>();
        }
        return this.userMovies.get(user.getId());
    }

    public Map<Integer, MovieRating> getMovieRatings(Movie movie){
        if(!this.movieRatings.containsKey(movie.getId())){
            return new HashMap<>();
        }
        return this.movieRatings.get(movie.getId());
    }
}

public class MovieRec {
    private RatingRegister ratingRegister;

    public MovieRec(RatingRegister ratingRegister){
        this.ratingRegister = ratingRegister;
    }

    public String recommendMovie(User user){
        if(this.ratingRegister.getUserMovies(user).isEmpty()){
            return this.recommendMovieNewUser();
        } else {
            return this.recommendMovieExistingUser(user);
        }
    }
    // recommend the highest avg rating to new user
    private String recommendMovieNewUser(){
        Movie bestMovie = null;
        double bestRating = 0;
        for(Movie movie: this.ratingRegister.getMovies()){
            double rating = this.ratingRegister.getAverageRating(movie);
            if(rating > bestRating){
                bestMovie = movie;
                bestRating = rating;
            }
        }
        return bestMovie != null ? bestMovie.getTitle() : null;
    }
    // recommend the highest rated movie from another user with similar interest
    private String recommendMovieExistingUser(User user){
        Movie bestMovie = null;
        int similarityScore = Integer.MAX_VALUE;

        for(User reviewer: this.ratingRegister.getUsers()){
            if(reviewer.getId() == user.getId()){
                continue;
            }
            int score = this.getSimilarityScore(user, reviewer);
            if(score < similarityScore){
                similarityScore = score;
                Movie recommendedMovie = this.recommendUnwatchedMovie(user, reviewer);
                bestMovie = recommendedMovie != null ? recommendedMovie : bestMovie;
            }
        }
        return bestMovie != null ? bestMovie.getTitle() : null;
    }
    private int getSimilarityScore(User user1, User user2){
        int score = Integer.MAX_VALUE;
        for(Movie movie: this.ratingRegister.getUserMovies(user2)){
            Map<Integer, MovieRating> ratings = this.ratingRegister.getMovieRatings(movie);
            // if user1 also rated the movie add diff
            if(ratings.containsKey(user1.getId())){
                score = (score == Integer.MAX_VALUE) ? 0 : score;
                score += Math.abs(ratings.get(user1.getId()).ordinal()
                - ratings.get(user2.getId()).ordinal());
            }
        }
        return score;
    }

    private Movie recommendUnwatchedMovie(User user, User reviewer){
        Movie bestMovie = null;
        int bestRating = 9;

        for(Movie movie: this.ratingRegister.getUserMovies(reviewer)){
            Map<Integer, MovieRating> ratings = this.ratingRegister.getMovieRatings(movie);
            // if user has not watched the movie, and reviewer gave it high rating. recommend it
            if(!ratings.containsKey(user.getId()) &&
            ratings.get(reviewer.getId()).ordinal() > bestRating){
                bestMovie = movie;
                bestRating = ratings.get(reviewer.getId()).ordinal();
            }
        }
        return bestMovie;
    }
}

class BankMain {
    public static void main(String[] args){
        User user1 = new User(1, "user1");
        User user2 = new User(2, "user2");
        User user3 = new User(3, "user3");

        Movie movie1 = new Movie( "Batman Begins", 1);
        Movie movie2 = new Movie( "Liar Liar", 2);
        Movie movie3 = new Movie( "The Godfather", 3);

        RatingRegister ratings = new RatingRegister();
        ratings.addRating(user1, movie1, MovieRating.FIVE);
        ratings.addRating(user1, movie2, MovieRating.TWO);
        ratings.addRating(user2, movie2, MovieRating.TWO);
        ratings.addRating(user2, movie3, MovieRating.FOUR);

        MovieRec recommender = new MovieRec(ratings);

        System.out.println(recommender.recommendMovie(user1));
        System.out.println(recommender.recommendMovie(user2));
        System.out.println(recommender.recommendMovie(user3));
    }
}