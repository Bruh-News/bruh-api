package edu.uark.dlb018.bruhapi.user;

import java.sql.*;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.mail.internet.InternetAddress;

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

        try{
            InternetAddress emailAddress = new InternetAddress(user.getEmail());
            emailAddress.validate();
        } catch(Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity<Long>(HttpStatus.BAD_REQUEST);
        }

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

        return new ResponseEntity<Long>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/setuserattribute")
    public ResponseEntity SetUserAttribute(@RequestBody List<UserAttribute> attributes){
        if(attributes.size() == 0) return new ResponseEntity(HttpStatus.BAD_REQUEST);
        String updateQuery;
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = dbConnect();) {
                for(int i = 0; i < attributes.size(); i++){
                    updateQuery = "UPDATE users SET " + attributes.get(i).getAttributeName() + " = ? WHERE id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(updateQuery, Statement.RETURN_GENERATED_KEYS);
                    pstmt.setString(1, attributes.get(i).getAttributeValue());
                    pstmt.setLong(2, attributes.get(i).getUserId());

                    pstmt.executeUpdate();
                    return new ResponseEntity(HttpStatus.OK);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        return new ResponseEntity(HttpStatus.BAD_REQUEST);
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
                    return new ResponseEntity<User>(HttpStatus.EXPECTATION_FAILED);
                } else{
                    String un = rs.getString("username");
                    String email = rs.getString("email");
                    String pw = rs.getString("password");
                    String pl = rs.getString("politicalleaning");
                    String age = rs.getString("age");
                    String race = rs.getString("race");
                    String nationality = rs.getString("nationality");
                    String religion = rs.getString("religion");
                    String parentalstatus = rs.getString("parentalstatus");
                    String gender = rs.getString("gender");
                    User foundUser = new User(un, email, pw);
                    foundUser.setPoliticalLeaning(pl);
                    foundUser.setAge(age);
                    foundUser.setRace(race);
                    foundUser.setNationality(nationality);
                    foundUser.setReligion(religion);
                    foundUser.setParentalStatus(parentalstatus);
                    foundUser.setGender(gender);
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
