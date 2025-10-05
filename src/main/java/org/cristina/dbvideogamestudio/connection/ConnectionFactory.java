package org.cristina.dbvideogamestudio.connection;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionFactory {
    public Connection databaseLink;

    public Connection getConnection() {
        String DBNAME = "videogamesstudio_db";
        String DRIVER = "org.postgresql.Driver";
        String DBURL = "jdbc:postgresql://localhost:5432/videogamesstudio_db";
        String USER = "postgres";
        String PASS = "cris1706";

        try {
            Class.forName(DRIVER);
            databaseLink = DriverManager.getConnection(DBURL,USER,PASS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return databaseLink;
    }
}
