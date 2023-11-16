import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Main class for the application. This class contains a main method which
 * launches the application.
 */

public class Main extends Application {

    Stage window;
    private static String keyword;
    private Controller controller;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Startup code for the application. This loads the FXML and controller and
     * creates the window to display it.
     * @param primaryStage The primary stage for the application
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = fxmlLoader.load();
        controller = fxmlLoader.getController();
        window = primaryStage;

        Scene scene = new Scene(root, 1200, 700);

        //Bibash sets an event handler that responds to key typing events within the scene
        keyword = "";
        scene.setOnKeyTyped(this::handleKeyTyped);

        window.setTitle("Dragon's Lair Pull List");
        //Add handler for closing the window with unsaved flags
        window.setOnCloseRequest(closeRequest -> {
            System.out.println(controller.isUnsaved());
            if (controller.isUnsaved()) {
                Alert unsavedAlert = new Alert(Alert.AlertType.CONFIRMATION, "You have unsaved changes to flags. " +
                        "Please save to avoid losing changes. Select 'Cancel' to go back.");
                unsavedAlert.showAndWait()
                        .filter(response -> response == ButtonType.CANCEL)
                        .ifPresent(response -> {
                            closeRequest.consume();
                        });;
            }
        });

        window.setScene(scene);
        window.setMinHeight(700);
        window.setMinWidth(1200);
        window.show();
    }

    //Bibash
    private void handleKeyTyped(KeyEvent event) {
        char typedChar = event.getCharacter().charAt(0);
        keyword += typedChar;
        System.out.println(keyword);
        if (controller.currentPage == Controller.CURRENT_PAGE.CUSTOMER && controller.getSelectedCustomer() != null)
            controller.handleCustomerJumping(keyword);
        if (controller.currentPage == Controller.CURRENT_PAGE.TITLE && controller.getSelectedTitle() != null)
            controller.handleTitleJumping(keyword);
    }


    public static void clearKeyword() {
        keyword = "";
    }
}