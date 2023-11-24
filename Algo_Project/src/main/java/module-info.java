module com.example.algo_project {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.algo_project to javafx.fxml;
    exports com.example.algo_project;
}