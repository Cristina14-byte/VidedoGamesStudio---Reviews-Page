# Video Game Review-Page App

This is a mini-project created for the **Database** course in **Year 2, Semester 1**. The application allows users to add reviews for games and DLCs in a PostgreSQL database. It is developed in **JavaFX** and demonstrates basic concepts of database management and GUI interaction.

## Features

- Displays a list of customers from the database.
- Displays a list of available games and DLCs.
- Allows users to submit a review for a game or DLC, including:
  - Comments
  - Rating from 1 to 10
- Saves reviews in the PostgreSQL database while maintaining referential integrity between customers, games, and DLCs.
- Validates required fields before submission.

## Project Structure

- `MainApplication.java` – Main class that launches the JavaFX application. 	
- `ReviewController.java` – Controller that handles the GUI logic and database interactions.
- `connection/ConnectionFactory.java` – Class responsible for managing the PostgreSQL connection.
- `hello-view.fxml` – FXML layout file for the user interface.

## Requirements

- Java 17+
- JavaFX 22+
- PostgreSQL 14+ (or a compatible version)
