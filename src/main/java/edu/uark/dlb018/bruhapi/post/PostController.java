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
    private final String dbUrl = "jdbc:postgresql://ec2-35-171-171-27.compute-1.amazonaws.com:5432/d2bq84hem29g4o";
    private final String dbUser = "lanhkxskvkvrqt";
    private final String dbPassword = "4344420cfef54ad993a6050bf7bc2434d09bf38981654b4d4b8e2e91e4288784";

    @PostMapping("/createpost")
    public ResponseEntity<Long> CreatePost(@RequestBody Post post){
        String insertQuery = "INSERT INTO posts (u_id, posttext, datetime, p_id, media) VALUES(?,?,?,?,?)";
        long id = 0;

        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = dbConnect();
                 PreparedStatement pstmt = conn.prepareStatement(insertQuery,
                         Statement.RETURN_GENERATED_KEYS)) {

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

    @GetMapping("/getpost")
    ResponseEntity<Post> GetPost(@RequestParam(value="id") long id){
        String selectQuery = "SELECT * FROM posts WHERE id=?";

        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = dbConnect();
                 PreparedStatement pstmt = conn.prepareStatement(selectQuery,
                         Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();
                if(!rs.next()){
                    return new ResponseEntity<Post>(HttpStatus.EXPECTATION_FAILED);
                } else{
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

    @GetMapping("/getpostsinuserfeed")
    ResponseEntity<List<Post>> GetPostsInUserFeed(@RequestParam(value="id") long id,
                                                  @RequestParam(value="page") int page){
        String selectQuery = "SELECT u.id FROM users u, (SELECT * FROM users WHERE id = ?) s WHERE " +
                             "u.politicalleaning <> s.politicalleaning OR " +
                                "(u.politicalleaning IS NULL AND s.politicalleaning IS NOT NULL) OR " +
                                "(u.politicalleaning IS NOT NULL AND s.politicalleaning IS NULL) OR " +
                             "u.age <> s.age OR " +
                                "(u.politicalleaning IS NULL AND s.politicalleaning IS NOT NULL) OR " +
                                "(u.politicalleaning IS NOT NULL AND s.politicalleaning IS NULL) OR " +
                             "u.race <> s.race OR " +
                                "(u.race IS NULL AND s.race IS NOT NULL) OR " +
                                "(u.race IS NOT NULL AND s.race IS NULL) OR " +
                             "u.nationality <> s.nationality OR " +
                                "(u.nationality IS NULL AND s.nationality IS NOT NULL) OR " +
                                "(u.nationality IS NOT NULL AND s.nationality IS NULL) OR " +
                             "u.religion <> s.religion OR " +
                                "(u.religion IS NULL AND s.religion IS NOT NULL) OR " +
                                "(u.religion IS NOT NULL AND s.religion IS NULL) OR " +
                             "u.parentalstatus <> s.parentalstatus OR " +
                                "(u.parentalstatus IS NULL AND s.parentalstatus IS NOT NULL) OR " +
                                "(u.parentalstatus IS NOT NULL AND s.parentalstatus IS NULL) OR " +
                             "u.gender <> s.gender OR " +
                                "(u.gender IS NULL AND s.gender IS NOT NULL) OR " +
                                "(u.gender IS NOT NULL AND s.gender IS NULL)";
        ArrayList<Long> diffUsers = new ArrayList<Long>();
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = dbConnect();
                 PreparedStatement pstmt = conn.prepareStatement(selectQuery,
                         Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();

                if(!rs.next()){
                    return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
                }

                do{
                    diffUsers.add(rs.getLong("id"));
                } while(rs.next());

            } catch(Exception e){
                System.out.println(e.getMessage());
            }

        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        ArrayList<Post> postsByDiffUsers = new ArrayList<Post>();
        for(int i = 0; i < diffUsers.size(); i++){
            List<Post> postsByUserI = GetAllPostsByUser(diffUsers.get(i)).getBody();
            if(postsByUserI != null) {
                for (int j = 0; j < postsByUserI.size(); j++) {
                    postsByDiffUsers.add(postsByUserI.get(j));
                }
            }
        }

        int pageLength = 12;
        if(postsByDiffUsers.size() < pageLength * (page - 1)) return new ResponseEntity(HttpStatus.BAD_REQUEST);

        double paginationMultiplier = (Math.random() * 0.25) + 0.5;
        long skip = (long)((double)postsByDiffUsers.size() * paginationMultiplier);
        boolean isPrime = isPrime(skip);
        while(!isPrime && postsByDiffUsers.size() > 3){
            skip = (skip % 2 == 0 ? skip + 1 : skip + 2) % postsByDiffUsers.size();
            isPrime = isPrime(skip) && skip != postsByDiffUsers.size();
        }

        long index = 0;
        for(int i = 0; i < pageLength * (page - 1); i++)
            index = (index + skip) % pageLength;

        ArrayList<Post> postsToReturn = new ArrayList<Post>();
        for(int i = pageLength * (page - 1); i < postsByDiffUsers.size() && i < pageLength * page; i++){
            index = (index + skip) % pageLength;
            postsToReturn.add(postsByDiffUsers.get(i));
        }

        return new ResponseEntity<List<Post>>(postsToReturn, HttpStatus.OK);
    }

    private boolean isPrime(long n){
        if(n <= 1) return false;
        for(int i = 2; i < Math.sqrt(n); i++){
            if(n % i == 0) return false;
        }
        return true;
    }

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

    public Connection dbConnect() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
}