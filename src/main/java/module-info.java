module org.cristina.dbvideogamestudio {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires com.almasb.fxgl.all;
    requires java.sql;

    opens org.cristina.dbvideogamestudio to javafx.fxml;
    exports org.cristina.dbvideogamestudio;
}