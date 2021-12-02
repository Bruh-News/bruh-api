package edu.uark.dlb018.bruhapi.post;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import edu.uark.dlb018.bruhapi.post.Post;
import edu.uark.dlb018.bruhapi.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
public class PostController {
    //db login info - not worried about security since it's just a school project
    //if we were to put this in a prod environment this would of course be secured
    private final String dbUrl = "jdbc:postgresql://ec2-35-171-171-27.compute-1.amazonaws.com:5432/d2bq84hem29g4o";
    private final String dbUser = "lanhkxskvkvrqt";
    private final String dbPassword = "4344420cfef54ad993a6050bf7bc2434d09bf38981654b4d4b8e2e91e4288784";

    //POST endpoint to create new site post
    @PostMapping("/createpost")
    public ResponseEntity<Long> CreatePost(@RequestBody Post post){
        //define insert query
        String insertQuery = "INSERT INTO posts (u_id, posttext, datetime, p_id, media) VALUES(?,?,?,?,?)";
        long id = 0;

        //connect to db
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = dbConnect();
                 PreparedStatement pstmt = conn.prepareStatement(insertQuery,
                         Statement.RETURN_GENERATED_KEYS)) {

                //insert post info into query
                pstmt.setLong(1, post.getUserId());
                pstmt.setString(2, post.getPostText());
                pstmt.setTimestamp(3, post.getDateTime());
                pstmt.setLong(4, post.getParentId());
                pstmt.setString(5, post.getMedia());

                int affectedRows = pstmt.executeUpdate();
                // check the affected rows
                if (affectedRows > 0) {
                    // get the ID back
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getLong(1);
                        }
                        return new ResponseEntity<Long>(id, HttpStatus.OK);
                    } catch (SQLException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    //GET endpoint to retrieve single post
    @GetMapping("/getpost")
    ResponseEntity<Post> GetPost(@RequestParam(value="id") long id){
        //define select query
        String selectQuery = "SELECT * FROM posts WHERE id=?";

        //connect to db
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = dbConnect();
                 PreparedStatement pstmt = conn.prepareStatement(selectQuery,
                         Statement.RETURN_GENERATED_KEYS)) {

                //set id in query
                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();
                if(!rs.next()){
                    return new ResponseEntity<Post>(HttpStatus.EXPECTATION_FAILED);
                } else{
                    //create Post object from retrieved info
                    Long uid = rs.getLong("u_id");
                    String posttext = rs.getString("posttext");
                    Long datetime = rs.getTimestamp("datetime").getTime()/1000;
                    Long pid = rs.getLong("p_id");
                    String media = rs.getString("media");
                    Post foundPost;
                    if(media != null){
                        foundPost = new Post(uid, posttext, datetime, pid, media);
                    } else{
                        foundPost = new Post(uid, posttext, datetime, pid);
                    }
                    //return constructed Post object
                    return new ResponseEntity<Post>(foundPost, HttpStatus.OK);
                }

            } catch(Exception e){
                System.out.println(e.getMessage());
            }
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        return new ResponseEntity<Post>(HttpStatus.BAD_REQUEST);
    }

    //GET endpoint to get the posts on a given page of a given user's feed
    //this one is a bit more algorithmic, had a lot of fun writing it
    @GetMapping("/getpostsinuserfeed")
    ResponseEntity<List<Post>> GetPostsInUserFeed(@RequestParam(value="id") long id,
                                                  @RequestParam(value="page") int page,
                                                  @RequestParam(value="pageLength") int pageLength){
        //define selection query - this one requires a bit of explaining
        //this query selects all users' IDs such that their user attributes differ from those of
            //the given user in at least one way
        String selectQuery = "SELECT u.id FROM users u, (SELECT * FROM users WHERE id = ?) s WHERE " +
                             //politicalleaning is different or...
                             "u.politicalleaning <> s.politicalleaning OR " +
                                "(u.politicalleaning IS NULL AND s.politicalleaning IS NOT NULL) OR " +
                                "(u.politicalleaning IS NOT NULL AND s.politicalleaning IS NULL) OR " +
                             //age is different or...
                             "u.age <> s.age OR " +
                                "(u.politicalleaning IS NULL AND s.politicalleaning IS NOT NULL) OR " +
                                "(u.politicalleaning IS NOT NULL AND s.politicalleaning IS NULL) OR " +
                             //race is different or...
                             "u.race <> s.race OR " +
                                "(u.race IS NULL AND s.race IS NOT NULL) OR " +
                                "(u.race IS NOT NULL AND s.race IS NULL) OR " +
                             //nationality is different or...
                             "u.nationality <> s.nationality OR " +
                                "(u.nationality IS NULL AND s.nationality IS NOT NULL) OR " +
                                "(u.nationality IS NOT NULL AND s.nationality IS NULL) OR " +
                             //religion is different or...
                             "u.religion <> s.religion OR " +
                                "(u.religion IS NULL AND s.religion IS NOT NULL) OR " +
                                "(u.religion IS NOT NULL AND s.religion IS NULL) OR " +
                             //parentalstatus is different or...
                             "u.parentalstatus <> s.parentalstatus OR " +
                                "(u.parentalstatus IS NULL AND s.parentalstatus IS NOT NULL) OR " +
                                "(u.parentalstatus IS NOT NULL AND s.parentalstatus IS NULL) OR " +
                             //gender is different
                             "u.gender <> s.gender OR " +
                                "(u.gender IS NULL AND s.gender IS NOT NULL) OR " +
                                "(u.gender IS NOT NULL AND s.gender IS NULL)";
        //ideally, this query would be replaced with some machine learning that understands HOW different
            //a found user is from te given user, and some other machine learning that alters the weight
            //of the user's attributes as they "agree" more and more with different people's posts.
        //for now though, this is all we need. show them posts by people who aren't the exact same as them.
        //this is also not very scalable because the query starts taking a way longer amount of time as we
            //get more users, and the rest of this endpoint starts taking way longer as we get more posts.
        //again, though: school project. those are things to think about later.

        ArrayList<Long> diffUsers = new ArrayList<Long>();
        //connect to db
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = dbConnect();
                 PreparedStatement pstmt = conn.prepareStatement(selectQuery,
                         Statement.RETURN_GENERATED_KEYS)) {

                //insert given user id into select query
                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();

                //executes the first rs.next() and makes sure it exists
                if(!rs.next()){
                    return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
                }

                //...so long as it does, add it and check again, repeat
                do{
                    diffUsers.add(rs.getLong("id"));
                } while(rs.next());

            } catch(Exception e){
                System.out.println(e.getMessage());
            }

        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        //aggregate all posts by the collected different users
        //this is part of the reason it isn't very scalable as-is
        ArrayList<Post> postsByDiffUsers = new ArrayList<Post>();
        for(int i = 0; i < diffUsers.size(); i++){
            List<Post> postsByUserI = GetAllPostsByUser(diffUsers.get(i)).getBody();
            if(postsByUserI != null) {
                for (int j = 0; j < postsByUserI.size(); j++) {
                    if(postsByUserI.get(j).getParentId() == 0)
                        postsByDiffUsers.add(postsByUserI.get(j));
                }
            }
        }

        //make sure there will be at least one post on this page of posts
        //page size is passed in, retrieved from user preferences
        if(postsByDiffUsers.size() < pageLength * (page - 1)) return new ResponseEntity(HttpStatus.BAD_REQUEST);

        //find some prime number smaller than the number of posts but hopefully as large as possible to
            //increase the index by (modulo'd by the number of posts) to create a pseudo-random order of
            //posts to appear to the user that doesn't repeat
        double paginationMultiplier = /*(Math.random() * 0.25) + 0.5*/ 0.6;
        long skip = (long)((double)postsByDiffUsers.size() * paginationMultiplier);
        boolean isPrime = isPrime(skip);
        while(!isPrime && postsByDiffUsers.size() > 3){
            skip = (skip % 2 == 0 ? skip + 1 : skip + 2) % postsByDiffUsers.size();
            isPrime = isPrime(skip) && skip != postsByDiffUsers.size();
        }

        //perform the prime number skip for any previous pages to catch the index up to where we are
        long index = (skip * (page - 1)) % pageLength;
//        for(int i = 0; i < pageLength * (page - 1); i++)
//            index = (index + skip) % pageLength;

        //continue increasing index by the chosen prime number, add posts to return list in that order
            //for as long as we don't hit the next page or the end of the array
        ArrayList<Post> postsToReturn = new ArrayList<Post>();
        for(int i = pageLength * (page - 1); i < postsByDiffUsers.size() && i < pageLength * page; i++){
            index = (index + skip) % pageLength;
            postsToReturn.add(postsByDiffUsers.get(i));
        }

        return new ResponseEntity<List<Post>>(postsToReturn, HttpStatus.OK);
    }

    //helper function to determine if a number is prime
    //because of slowness of Math.sqrt(), it might be faster to go up to n/2 instead, but it's fine for now
    //this is minimum for loop cycles at least
    private boolean isPrime(long n){
        if(n <= 1) return false;
        for(int i = 2; i < Math.sqrt(n); i++){
            if(n % i == 0) return false;
        }
        return true;
    }

    //GET endpoint to retrieve all posts by a user
    @GetMapping("/getallpostsbyuser")
    ResponseEntity<List<Post>> GetAllPostsByUser(@RequestParam(value="id") long id){
        String selectQuery = "SELECT * FROM posts WHERE u_id=?";

        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = dbConnect();
                 PreparedStatement pstmt = conn.prepareStatement(selectQuery,
                         Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();
                if(!rs.next()){
                    return new ResponseEntity<List<Post>>(HttpStatus.EXPECTATION_FAILED);
                } else{
                    List<Post> posts = new ArrayList<Post>();
                    do {
                        Long uid = rs.getLong("u_id");
                        String posttext = rs.getString("posttext");
                        Long datetime = rs.getTimestamp("datetime").getTime()/1000;
                        Long pid = rs.getLong("p_id");
                        String media = rs.getString("media");
                        Post foundPost;
                        if(media != null){
                            foundPost = new Post(uid, posttext, datetime, pid, media);
                        } else{
                            foundPost = new Post(uid, posttext, datetime, pid);
                        }
                        posts.add(foundPost);
                    } while(rs.next());
                    return new ResponseEntity<List<Post>>(posts, HttpStatus.OK);
                }

            } catch(Exception e){
                System.out.println(e.getMessage());
            }
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        return new ResponseEntity<List<Post>>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/getcommentsonpost")
    public ResponseEntity<List<Post>> GetCommentsOnPost(@RequestParam(value="id") long id){
        String selectQuery = "SELECT * FROM posts WHERE p_id=?";

        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = dbConnect();
                 PreparedStatement pstmt = conn.prepareStatement(selectQuery,
                         Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();
                if(!rs.next()){
                    return new ResponseEntity<List<Post>>(HttpStatus.EXPECTATION_FAILED);
                } else{
                    List<Post> posts = new ArrayList<Post>();
                    do {
                        Long uid = rs.getLong("u_id");
                        String posttext = rs.getString("posttext");
                        Long datetime = rs.getTimestamp("datetime").getTime()/1000;
                        Long pid = rs.getLong("p_id");
                        String media = rs.getString("media");
                        Post foundPost;
                        if(media != null){
                            foundPost = new Post(uid, posttext, datetime, pid, media);
                        } else{
                            foundPost = new Post(uid, posttext, datetime, pid);
                        }
                        posts.add(foundPost);
                    } while(rs.next());
                    return new ResponseEntity<List<Post>>(posts, HttpStatus.OK);
                }

            } catch(Exception e){
                System.out.println(e.getMessage());
            }
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        return new ResponseEntity<List<Post>>(HttpStatus.BAD_REQUEST);
    }

    public Connection dbConnect() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
}