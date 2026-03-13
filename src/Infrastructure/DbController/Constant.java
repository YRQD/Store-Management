package Infrastructure.DbController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Constant {

    public static final String url = "jdbc:postgresql://localhost:5432/StoreManagement";
    public static final String user = "postgres";
    public static final String password = "2552";

    public static Connection con;
    public static Statement st;
    public static ResultSet rsl;
    public static PreparedStatement pst;
}
