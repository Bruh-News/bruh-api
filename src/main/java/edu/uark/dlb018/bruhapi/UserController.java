package edu.uark.dlb018.bruhapi;

import java.sql.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
public class UserController {
    private final String dbUrl = "jdbc:postgresql://ec2-35-171-171-27.compute-1.amazonaws.com:5432/d2bq84hem29g4o";
    private final String dbUser = "lanhkxskvkvrqt";
    private final String dbPassword = "4344420cfef54ad993a6050bf7bc2434d09bf38981654b4d4b8e2e91e4288784";

    @PostMapping("/createuser")
    public ResponseEntity<Long> CreateUser(@RequestBody User user){
        String insertQuery = "INSERT INTO users (username, email, password) VALUES(?,?,?)";
        long id = 0;
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = dbConnect();
                 PreparedStatement pstmt = conn.prepareStatement(insertQuery,
                         Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getEmail());
                pstmt.setString(3, user.getPassword());

                int affectedRows = pstmt.executeUpdate();
                // check the affected rows
                if (affectedRows > 0) {
                    // get the ID back
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getLong(1);
                        }
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

        return new ResponseEntity<Long>(id, HttpStatus.OK);
    }

    @PostMapping("/setuserattribute")
    public ResponseEntity SetUserAttribute(
        @RequestParam(value="userId") Long userId,
        @RequestParam(value="attributeName") String attributeName,
        @RequestParam(value="attributeValue") String attributeValue
    ){
        String updateQuery = "UPDATE users SET " + attributeName + " = ? WHERE id = ?";
        long id = 0;
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = dbConnect();
                 PreparedStatement pstmt = conn.prepareStatement(updateQuery,
                         Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, attributeValue);
                pstmt.setLong(2, userId);

                pstmt.executeUpdate();
                return new ResponseEntity(HttpStatus.OK);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        return new ResponseEntity<Long>(id, HttpStatus.OK);
    }

    @GetMapping("/getuser")
    public ResponseEntity<User> GetUser(@RequestParam(value = "id") Long id){
        String selectQuery = "SELECT * FROM users WHERE id=?";
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = dbConnect();
                 PreparedStatement pstmt = conn.prepareStatement(selectQuery,
                         Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();
                if(!rs.next()){
                    return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
                } else{
                    String un = rs.getString("username");
                    String email = rs.getString("email");
                    String pw = rs.getString("password");
                    User foundUser = new User(un, email, pw);
                    return new ResponseEntity<User>(foundUser, HttpStatus.OK);
                }

            } catch(Exception e){
                System.out.println(e.getMessage());
            }
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/deleteallusers")
    public ResponseEntity DeleteAllUsers(){
        String selectQuery = "DELETE FROM users";
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = dbConnect();
                 PreparedStatement pstmt = conn.prepareStatement(selectQuery,
                         Statement.RETURN_GENERATED_KEYS)) {

                pstmt.executeQuery();
                return new ResponseEntity(HttpStatus.OK);

            } catch(Exception e){
                System.out.println(e.getMessage());
            }
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
    }

    public Connection dbConnect() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
}
