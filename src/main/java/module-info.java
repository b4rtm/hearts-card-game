module com.example.hearts {
    requires javafx.controls;
    requires javafx.fxml;
            
                            
    opens com.example.hearts.client to javafx.fxml;
    exports com.example.hearts.client;
}