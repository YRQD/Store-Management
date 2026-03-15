package Infrastructure.DbController;

import java.sql.Connection;

public class Constant {

    public static final String url = "jdbc:postgresql://localhost:5432/StoreManagement";
    public static final String user = "postgres";
    public static final String password = "2552";

    public static Connection con;
}
