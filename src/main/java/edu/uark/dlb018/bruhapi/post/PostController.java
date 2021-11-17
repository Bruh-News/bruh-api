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