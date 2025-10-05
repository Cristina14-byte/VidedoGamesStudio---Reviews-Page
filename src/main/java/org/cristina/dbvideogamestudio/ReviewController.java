package org.cristina.dbvideogamestudio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import org.cristina.dbvideogamestudio.connection.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReviewController {

    @FXML
    private ComboBox<String> customerComboBox;

    @FXML
    private ComboBox<String> gameOrDlcComboBox;

    @FXML
    private TextArea reviewCommentsTextArea;

    @FXML
    private Spinner<Integer> ratingSpinner;

    private ObservableList<String> customerList = FXCollections.observableArrayList();
    private ObservableList<String> itemsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadCustomers();
        loadGamesAndDlcs();
        setupRatingSpinner();
    }

    private void loadCustomers() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        Connection connection = connectionFactory.getConnection();
        String query = "SELECT name FROM Customers";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                customerList.add(resultSet.getString("name"));
            }
            customerComboBox.setItems(customerList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load customers.", AlertType.ERROR);
        }
    }

    private void loadGamesAndDlcs() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        Connection connection = connectionFactory.getConnection();
        String query = """
                SELECT name, 'Game' AS type FROM Games
                UNION
                SELECT CONCAT('DLC "', g.name, '": "', d.name, '"') AS name, 'DLC' AS type
                FROM DLCs d
                JOIN Games g ON d.game_id = g.game_id
                """;
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                itemsList.add(resultSet.getString("name"));
            }
            gameOrDlcComboBox.setItems(itemsList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load games and DLCs.", AlertType.ERROR);
        }
    }

    private void setupRatingSpinner() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 5);
        ratingSpinner.setValueFactory(valueFactory);
    }

    @FXML
    public void handleSubmitButtonAction(ActionEvent event) {
        String selectedCustomer = customerComboBox.getValue();
        String selectedName = gameOrDlcComboBox.getValue();
        String comments = reviewCommentsTextArea.getText();
        int rating = ratingSpinner.getValue();

        if (selectedCustomer == null || selectedCustomer.isEmpty()) {
            showAlert("Validation Error", "Please select a customer.", AlertType.WARNING);
            return;
        }

        if (selectedName == null || selectedName.isEmpty()) {
            showAlert("Validation Error", "Please select a game/DLC and enter your review.", AlertType.WARNING);
            return;
        }

        if (comments.isEmpty()) {
            showAlert("Validation Error", "Please enter a comment.", AlertType.WARNING);
            return;
        }

        ConnectionFactory connectionFactory = new ConnectionFactory();
        Connection connection = connectionFactory.getConnection();

        Integer customerId = null;
        try (PreparedStatement stmt = connection.prepareStatement("SELECT customer_id FROM Customers WHERE name = ?")) {
            stmt.setString(1, selectedCustomer);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) customerId = rs.getInt("customer_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to retrieve customer information.", AlertType.ERROR);
            return;
        }

        if (customerId == null) {
            showAlert("Validation Error", "Customer not found. Please try again.", AlertType.WARNING);
            return;
        }

        Integer gameId = null;
        Integer dlcId = null;

        if (selectedName.startsWith("DLC")) {
            String dlcName = selectedName.substring(selectedName.indexOf(":") + 1).replace("\"", "").trim();
            try (PreparedStatement stmt = connection.prepareStatement("SELECT dlc_id, game_id FROM DLCs WHERE name = ?")) {
                stmt.setString(1, dlcName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        dlcId = rs.getInt("dlc_id");
                        gameId = rs.getInt("game_id");
                    } else {
                        showAlert("Error", "DLC not found in the database.", AlertType.ERROR);
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to retrieve DLC information.", AlertType.ERROR);
                return;
            }
        } else {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT game_id FROM Games WHERE name = ?")) {
                stmt.setString(1, selectedName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        gameId = rs.getInt("game_id");
                    } else {
                        showAlert("Error", "Game not found in the database.", AlertType.ERROR);
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to retrieve game information.", AlertType.ERROR);
                return;
            }
        }

        String insertQuery = "INSERT INTO Reviews (customer_id, game_id, dlc_id, rating, comments) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, gameId);
            if (dlcId != null) {
                stmt.setInt(3, dlcId);
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            stmt.setInt(4, rating);
            stmt.setString(5, comments);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Success", "Review submitted successfully!", AlertType.INFORMATION);
                clearForm();
            } else {
                showAlert("Error", "Failed to submit the review.", AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while submitting the review.", AlertType.ERROR);
        }
    }

    private Integer getGameIdByName(String name) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT game_id FROM Games WHERE name = ?")) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("game_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Integer getDlcIdByName(String fullName) {
        String dlcName = extractDlcName(fullName);
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT dlc_id FROM DLCs WHERE name = ?")) {
            stmt.setString(1, dlcName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("dlc_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Integer getGameIdByDlc(Integer dlcId) {
        if (dlcId == null) return null;
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT game_id FROM DLCs WHERE dlc_id = ?")) {
            stmt.setInt(1, dlcId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("game_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String extractDlcName(String fullName) {
        if (fullName.contains(":")) {
            return fullName.split(":")[1].replace("\"", "").trim();
        }
        return fullName;
    }

    private void clearForm() {
        customerComboBox.getSelectionModel().clearSelection();
        reviewCommentsTextArea.clear();
        gameOrDlcComboBox.getSelectionModel().clearSelection();
        ratingSpinner.getValueFactory().setValue(5);
    }
}
