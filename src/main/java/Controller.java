import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

//import java.beans.EventHandler;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.ResourceBundle;

public class Controller implements Initializable {

/*######################################################################/
//////////////////////////// Class Variables ////////////////////////////
/######################################################################*/

    //#region Class Variables

    private boolean unsaved = false;

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> customerLastNameColumn;
    @FXML private TableColumn<Customer, String> customerFirstNameColumn;
    @FXML private TableColumn<Customer, String> customerPhoneColumn;
    @FXML private TableColumn<Customer, String> customerEmailColumn;

    @FXML private TableView<Title> titleTable;
    @FXML private TableColumn<Title, Boolean> titleFlaggedColumn;
    @FXML private TableColumn<Title, String> titleTitleColumn;
    @FXML private TableColumn<Title, String> titlePriceColumn;
    @FXML private TableColumn<Title, String> titleNotesColumn;

    @FXML private TableView<Order> customerOrderTable;
    @FXML private TableColumn<Order, String> customerOrderReqItemsColumn;
    @FXML private TableColumn<Order, String> customerOrderQuantityColumn;
    @FXML private TableColumn<Order, String> customerOrderIssueColumn;

    @FXML private TableView<FlaggedTable> flaggedTable;  
    @FXML private TableColumn<FlaggedTable, String> flaggedTitleColumn;             //Jack
    @FXML private TableColumn<FlaggedTable, String> flaggedIssueColumn;             //Jack
    @FXML private TableColumn<FlaggedTable, String> flaggedPriceColumn;             //Jack
    @FXML private TableColumn<FlaggedTable, String> flaggedQuantityColumn;          //Jack
    @FXML private TableColumn<FlaggedTable, String> flaggedNumRequestsColumn;

    @FXML private TableView<RequestTable> requestsTable;
    @FXML private TableColumn<RequestTable, String> requestLastNameColumn;
    @FXML private TableColumn<RequestTable, String> requestFirstNameColumn;
    @FXML private TableColumn<RequestTable, Integer> requestQuantityColumn;

    @FXML private TableView<Title> monthlyBreakdownTable;
    @FXML private TableColumn<Title, String> breakdownTitleColumn;
    @FXML private TableColumn<Title, String> breakdownQuantityColumn;
    @FXML private TableColumn<Title, String> breakdownPendingIssueColumn;
    @FXML private TableColumn<Title, String> breakdownFlaggedColumn;

    @FXML private TableView<RequestTable> titleOrdersTable;
    @FXML private TableColumn<RequestTable, String> titleOrderLastNameColumn;
    @FXML private TableColumn<RequestTable, String> titleOrderFirstNameColumn;
    @FXML private TableColumn<RequestTable, Integer> titleOrderQuantityColumn;
    @FXML private TableColumn<RequestTable, Integer> titleOrderIssueColumn;

    @FXML private Text customerFirstNameText;
    @FXML private Text customerLastNameText;
    @FXML private Text customerPhoneText;
    @FXML private Text customerEmailText;

    @FXML private Text titleTitleText;
    @FXML private Text titlePriceText;
    @FXML private Text titleNotesText;
    @FXML private Text titleDateFlagged;
    @FXML private Text titleDateFlaggedNoticeText;
    @FXML private Text titleNumberRequestsText;

    //for the summary info in "new week pulls" tab in "reports" tab
    @FXML private Text FlaggedTitlesTotalText;
    @FXML private Text FlaggedTitlesTotalCustomersText;
    @FXML private Text FlaggedIssueNumbersText;
    @FXML private Text FlaggedNoRequestsText;

    //for the summary info on a particular flagged title, when clicked
    @FXML private Text RequestTitleText;
    @FXML private Text RequestQuantityText;
    @FXML private Text RequestNumCustomersText;

    @FXML private TextArea databaseOverview;

    private static Connection conn = null;

    private boolean setAll;
    //#endregion

/*######################################################################/
////////////////////////// Getters and Setters //////////////////////////
/######################################################################*/

    //#region Getters and Setters

    /**
     * Searches the database for whether or not the given title has any pending issue requests
     * @param titleId the ID of the title to search for
     * @return True is the title has any pending issue requests, false otherwise
     */
    private boolean hasPendingIssueRequest(int titleId) {
        boolean pendingIssueRequest = false;
        ResultSet result;
        Statement s = null;
        try
        {
            String sql = String.format("""
                    SELECT COUNT(*) FROM ORDERS
                    WHERE ISSUE IS NOT NULL AND TITLEID=%s
                    """, titleId);

            s = conn.createStatement();
            result = s.executeQuery(sql);
            result.next();
            if (result.getInt(1) > 0)
            {
                pendingIssueRequest = true;
            }
            result.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        return pendingIssueRequest;
    }

    /**
     * Returns true or false based on if there are unsaved changes to New
     * Release Flags or not.
     * @return A boolean for whether or not there are unsaved changes
     */
    public boolean isUnsaved() {
        return unsaved;
    }

    /**
     * Gets a list representing all Customers in the database
     * @return An ObservableList of Customer objects
     */
    public ObservableList<Customer> getCustomers() {

        ObservableList<Customer> customers = FXCollections.observableArrayList();

        Statement s = null;
        try
        {
            s = conn.createStatement();
            ResultSet results = s.executeQuery("select * from Customers ORDER BY LASTNAME");

            while(results.next())
            {
                int customerId = results.getInt(1);
                String firstName = results.getString(2);
                String lastName = results.getString(3);
                String phone = results.getString(4);
                String email = results.getString(5);
                customers.add(new Customer(customerId, firstName, lastName, phone, email));
            }
            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return customers;
    }

    /**
     * Helper method to get a variety of information from the database
     */
    private void getDatabaseInfo() {
        int numTitles = titleTable.getItems().size();
        int numCustomers = customerTable.getItems().size();
        int specialOrderNotes = 0;
        int issueNumberRequests = getNumIssueRequests();
        int titlesNotFlagged = 0;
        int titlesNoRequests = 0;

        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        for (Title title : titleTable.getItems()) {

            if (title.getNotes().compareTo("") != 0) {
                specialOrderNotes++;
            }
            if (title.getDateFlagged() == null) {
                titlesNotFlagged++;
            }
            else if (title.getDateFlagged().isBefore(sixMonthsAgo)) {
                titlesNotFlagged++;
            }
            if (getNumberRequests(title.getId()) == 0) {
                titlesNoRequests++;
            }
        }

        databaseOverview.setText(String.format("""
                Database currently has:
                   %s Titles
                   %s Customers
                   %s Special Order Notes
                   %s Pending Issue # Requests
                   %s Titles have not been flagged for over six months
                   %s Titles have 0 Customer Requests
                """, numTitles, numCustomers, specialOrderNotes, issueNumberRequests, titlesNotFlagged, titlesNoRequests));
    }

    /**
     * Gets all of the flagged titles and related information to fill the Flagged Table
     * @return an obeservable lsit of FlaggedTable object with the requested data
     */
    public ObservableList<FlaggedTable> getFlaggedTitles() {

        ObservableList<FlaggedTable> flaggedTitles = FXCollections.observableArrayList();

        Statement s = null;
        try
        {
            s = conn.createStatement();

            ResultSet results = s.executeQuery("""
            SELECT TITLEID, TITLE, ISSUE_FLAGGED, PRICE, SUM(QUANTITY) AS QUANTITY, COUNT(CUSTOMERID) AS NUM_REQUESTS FROM (
                                                                                                                       SELECT TITLES.TITLEID, TITLES.TITLE, TITLES.ISSUE_FLAGGED, ORDERS.CUSTOMERID, ORDERS.ISSUE, TITLES.PRICE, ORDERS.QUANTITY
                                                                                                                       from TITLES
                                                                                                                                INNER JOIN ORDERS ON ORDERS.TITLEID = TITLES.TITLEID
                                                                                                                       WHERE TITLES.FLAGGED = true AND (ISSUE = ISSUE_FLAGGED OR ISSUE IS NULL)
                                                                                                                   ) AS FLAGGED_ORDERS
            GROUP BY TITLEID, TITLE, PRICE, ISSUE_FLAGGED
            ORDER BY TITLE
            """);
            
            while(results.next())
            {
                int titleId = results.getInt("TITLEID");
                String title = results.getString("TITLE");
                int issue = results.getInt("ISSUE_FLAGGED");
                int price= results.getInt("PRICE");
                int quantity = results.getInt("QUANTITY");
                int numRequests = results.getInt("NUM_REQUESTS");

                flaggedTitles.add(new FlaggedTable( titleId, title, issue, price, quantity, numRequests));

            }
            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return flaggedTitles;
    }

    /**
     * helper method to get the second piece of summary info on "new week pulls" tab: the number of customers the titles are flagged for
     */
    private int getNumCustomers(){
        int numCustomers = 0;

        Statement s = null;
        try
        {
            s = conn.createStatement();
            ResultSet results = s.executeQuery("""
                SELECT COUNT(*) FROM (
                        SELECT DISTINCT CUSTOMERID FROM ORDERS
                        LEFT JOIN TITLES T on ORDERS.TITLEID = T.TITLEID
                        WHERE FLAGGED = TRUE
                    ) AS FLAGGED_CUSTOMERS
            """);

            results.next();
            numCustomers = results.getInt(1);

            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return numCustomers;
    }

    /**
     * helper method to get the third piece of summary info on "new week pulls" tab: the number of titles that have triggered issue #'s
     */
    private int getNumFlaggedWithIssueNumbers(){
        int numTitlesWithIssueNumbers = 0;

        Statement s = null;
        try
        {
            s = conn.createStatement();
            ResultSet results = s.executeQuery("""
                SELECT COUNT(*) AS TRIGGERED_ISSUE_COUNT FROM (
                    SELECT DISTINCT TITLEID FROM (
                                                     SELECT TITLES.TITLEID, ORDERS.ISSUE
                                                     FROM TITLES
                                                              INNER JOIN ORDERS ON ORDERS.TITLEID = TITLES.TITLEID
                                                     WHERE TITLES.FLAGGED = true AND ISSUE = ISSUE_FLAGGED
                                                 ) AS FLAGGED_ORDERS
                    ) AS ISSUE_NOT_NULL_TITLES
            """);

            results.next();
            numTitlesWithIssueNumbers = results.getInt(1);

            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return numTitlesWithIssueNumbers;
    }

    /**
     * Gets the total number of issue requests in the database
     * @return the total number of issue requests in the database
     */
    private int getNumIssueRequests() {
        int numTitlesWithIssueNumbers = 0;

        Statement s = null;
        try
        {
            s = conn.createStatement();
            ResultSet results = s.executeQuery("""
                SELECT COUNT(*) AS TRIGGERED_ISSUE_COUNT FROM (
                    SELECT DISTINCT TITLEID FROM (
                                                     SELECT TITLES.TITLEID, ORDERS.ISSUE
                                                     FROM TITLES
                                                              INNER JOIN ORDERS ON ORDERS.TITLEID = TITLES.TITLEID
                                                     WHERE ISSUE IS NOT NULL
                                                 ) AS FLAGGED_ORDERS
                    ) AS ISSUE_NOT_NULL_TITLES
            """);

            results.next();
            numTitlesWithIssueNumbers = results.getInt(1);

            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return numTitlesWithIssueNumbers;
    }

    /**
     * Gets the number of Orders for a specified Title
     * @param titleId The title to count orders for
     * @return The number of orders
     */
    private int getNumberRequests(int titleId) {
        int ordersCount = 0;
        ResultSet result;
        Statement s = null;
        try
        {
            String sql = String.format("""
                    SELECT COUNT(*) FROM ORDERS
                    WHERE titleID = %s
                    """, titleId);

            s = conn.createStatement();
            result = s.executeQuery(sql);
            while(result.next()) {
                ordersCount = result.getInt(1);
            }
            result.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return ordersCount;
    }

    /**
     * helper method to get the first piece of summary info on "new week pulls" tab: the total # of flagged titles
     */
    private int getNumTitlesCurrentlyFlagged() {

        int numTitlesCurrentlyFlagged = 0;

        Statement s = null;
        try
        {
            s = conn.createStatement();
            ResultSet results = s.executeQuery("SELECT COUNT(TITLES.FLAGGED) AS FlagCount FROM TITLES WHERE FLAGGED=TRUE");

            results.next();
            numTitlesCurrentlyFlagged = results.getInt("FlagCount");

            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return numTitlesCurrentlyFlagged;
    }

    /**
     * helper method to get the fourth piece of summary info on "new week pulls" tab: the number of titles with no customer requests
     */
    private int getNumTitlesNoRequests() {
        int numTitlesWithNoRequests = 0;

        Statement s = null;
        try
        {
            s = conn.createStatement();
            ResultSet results = s.executeQuery("""
                        SELECT COUNT(*) FROM (
                                SELECT DISTINCT T.TITLEID
                                FROM ORDERS
                                LEFT JOIN TITLES T on ORDERS.TITLEID = T.TITLEID
                                WHERE FLAGGED = TRUE
                        ) AS FLAGGED_WITH_REQUESTS
                        RIGHT JOIN TITLES ON TITLES.TITLEID = FLAGGED_WITH_REQUESTS.TITLEID
                        WHERE FLAGGED_WITH_REQUESTS.TITLEID IS NULL AND FLAGGED = TRUE
            """);

            results.next();
            numTitlesWithNoRequests = results.getInt(1);

            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return numTitlesWithNoRequests;
    }

    /**
     * Gets a list representing all Orders in the database.
     * @return An ObservableList of Order objects
     */
    public ObservableList<Order> getOrderTable() {
        ObservableList<Order> orders = FXCollections.observableArrayList();

        Statement s = null;
        try
        {
            s = conn.createStatement();
            ResultSet results = s.executeQuery("SELECT ORDERS.CUSTOMERID, ORDERS.TITLEID, TITLES.title, ORDERS.QUANTITY, ORDERS.ISSUE FROM TITLES" +
                    " INNER JOIN ORDERS ON Orders.titleID=TITLES.TitleId ORDER BY TITLE");

            while(results.next())
            {
                int customerId = results.getInt(1);
                int titleId = results.getInt(2);
                String title = results.getString(3);
                int quantity = results.getInt(4);
                int issue = results.getInt(5);

                orders.add(new Order(customerId, titleId, title, quantity, issue));
            }
            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return orders;
    }

    /**
     * Gets requests for a title and issue. Specify any number less than 1 to specify all issues
     * @param titleId The title to get the requests for
     * @param issue The issue to get the requests for.
     * @return an ObservableList of RequestTable objects of the requested requests
     */
    public ObservableList<RequestTable> getRequests(int titleId, int issue) {

        ObservableList<RequestTable> requestsTable = FXCollections.observableArrayList();

        Statement s = null;
        try
        {
            String sql = "";
            if (issue > 0) {
                sql = String.format("""
                        SELECT CUSTOMERS.LASTNAME, CUSTOMERS.FIRSTNAME, ORDERS.QUANTITY, ORDERS.ISSUE FROM CUSTOMERS
                        INNER JOIN ORDERS ON ORDERS.CUSTOMERID=CUSTOMERS.CUSTOMERID
                        WHERE ORDERS.TITLEID=%s AND (ORDERS.ISSUE=%s OR ORDERS.ISSUE IS NULL)
                        ORDER BY CUSTOMERS.LASTNAME
                        """, titleId, issue);
            } 
            else if (issue == -9) {
                sql = String.format("""
                        SELECT CUSTOMERS.LASTNAME, CUSTOMERS.FIRSTNAME, ORDERS.QUANTITY, ORDERS.ISSUE FROM CUSTOMERS
                        INNER JOIN ORDERS ON ORDERS.CUSTOMERID=CUSTOMERS.CUSTOMERID
                        WHERE ORDERS.TITLEID=%s
                        ORDER BY CUSTOMERS.LASTNAME
                        """, titleId);
            }
            else {
                sql = String.format("""
                        SELECT CUSTOMERS.LASTNAME, CUSTOMERS.FIRSTNAME, ORDERS.QUANTITY, ORDERS.ISSUE FROM CUSTOMERS
                        INNER JOIN ORDERS ON ORDERS.CUSTOMERID=CUSTOMERS.CUSTOMERID
                        WHERE ORDERS.TITLEID=%s AND ORDERS.ISSUE IS NULL
                        ORDER BY CUSTOMERS.LASTNAME
                        """, titleId);
            }

            s = conn.createStatement();

            ResultSet results = s.executeQuery(sql);

            while(results.next())
            {
                String lastName = results.getString(1);
                String firstName = results.getString(2);
                int quantity = results.getInt(3);
                int issueNumber = results.getInt(4); 
                requestsTable.add(new RequestTable( lastName, firstName, quantity, issueNumber ));
            }
            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return requestsTable;
    }

    /**
     * Gets a list representing all Titles in the database
     * @return An ObeservableList of all Title objects
     */
    public ObservableList<Title> getTitles() {

        ObservableList<Title> titles  = FXCollections.observableArrayList();

        try
        {
            Statement s = conn.createStatement();
            ResultSet results = s.executeQuery("select * from Titles order by TITLE");

            while(results.next())
            {
                int titleId = results.getInt("TITLEID");
                String title = results.getString("TITLE");
                int price= results.getInt("PRICE");
                String notes = results.getString("NOTES");
                boolean flagged = results.getBoolean("FLAGGED");
                Date dateFlagged = results.getDate("DATE_FLAGGED");
                int issueFlagged = results.getInt("ISSUE_FLAGGED");
                if (dateFlagged != null) {
                    titles.add(new Title(titleId, title, price, notes, flagged, dateFlagged.toLocalDate(), issueFlagged));
                }
                else {
                    titles.add(new Title(titleId, title, price, notes, flagged, null, issueFlagged));
                }
            }
            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        for (Title t : titles) {
            t.flaggedProperty().addListener((obs, wasFlagged, isFlagged) -> {
                if (isFlagged) {
                    try {
                        Statement s = conn.createStatement();
                        String sql = "SELECT * FROM ORDERS WHERE TITLEID = " + t.getId() + " AND ISSUE IS NOT NULL";
                        ResultSet results = s.executeQuery(sql);

                        if (results.next()) {
                            int titleId = results.getInt("TITLEID");
                            if (t.getId() == titleId) {
                                TextInputDialog dialog = new TextInputDialog();
                                dialog.setContentText("This title has at least one issue # request.\nPlease enter the issue # for the new release.");
                                dialog.setTitle("Confirm Issue");
                                dialog.setHeaderText("");
                                final Button buttonOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                                buttonOk.addEventFilter(ActionEvent.ACTION, event -> {
                                    try {
                                        t.setIssueFlagged(Integer.parseInt(dialog.getEditor().getText()));
                                    } catch (NumberFormatException e) {
                                        event.consume();
                                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please enter a valid integer", ButtonType.OK);
                                        alert.show();
                                    }
                                });
                                if (dialog.showAndWait().isEmpty()) {
                                    t.setFlagged(false);
                                }
                            }
                        }
                        results.close();
                        s.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    this.unsaved = true;
                }
                if (!isFlagged && wasFlagged) this.unsaved = true;
            });
        }
        return titles;
    }

    /**
     * Gets the total quantity from all orders for a specified Title
     * @param titleId The title to get quantity
     * @return The sum of all request quantities
     */
    private int getTitleQuantity(int titleId) {
        int quantity = 0;
        ResultSet result;
        Statement s = null;
        try
        {
            String sql = String.format("""
                    SELECT SUM(QUANTITY) FROM ORDERS
                    WHERE titleID = %s
                    """, titleId);

            s = conn.createStatement();
            result = s.executeQuery(sql);
            while(result.next()) {
                quantity = result.getInt(1);
            }
            result.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return quantity;
    }

    //#endregion

/*######################################################################/
///////////////////////////// Initialization ////////////////////////////
/######################################################################*/

    //#region Initalization

    /**
     * Initiializes the state of the application. Creates a connection to the database,
     * loads all Customer, Title, and Order data, populates all tables, and creates
     * listeners.
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        createConnection();

        //Populate columns for Customer Table
        customerLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        customerFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        customerPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        customerEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        customerTable.getItems().setAll(this.getCustomers());

        //Populate columns for Orders Table
        customerOrderReqItemsColumn.setCellValueFactory(new PropertyValueFactory<>("TitleName"));
        customerOrderQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        customerOrderIssueColumn.setCellValueFactory(cell -> {
            if (cell.getValue().getIssue() > 0) {
                return new SimpleStringProperty(Integer.toString(cell.getValue().getIssue()));
            } else {
                return new SimpleStringProperty("");
            }
        });

        // Comparator to sort by price, set to columns that contain price information
        Comparator<String> priceComparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                // avoid empty string exceptions, sort first
                if (o1.isEmpty()) return -1;
                if (o2.isEmpty()) return 1;
                // empty strings avoided, compare doubles
                Double o1d = Double.valueOf(o1);
                Double o2d = Double.valueOf(o2);
                if (o1d < o2d) return -1;
                if (o1d > o2d) return 1;
                return 0;
            }
        };
        titlePriceColumn.setComparator(Comparator.nullsFirst(priceComparator));
        flaggedPriceColumn.setComparator(Comparator.nullsFirst(priceComparator));

        //Populate columns for Title Table, sort by title column
        titleFlaggedColumn.setCellValueFactory(c -> c.getValue().flaggedProperty());
        titleFlaggedColumn.setCellFactory(tc -> new CheckBoxTableCell<>());
        titleTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titlePriceColumn.setCellValueFactory(new PropertyValueFactory<>("priceDollars"));
        titlePriceColumn.setCellValueFactory(cell -> {
            if (cell.getValue().getPrice() > 0) {
                return new SimpleStringProperty(cell.getValue().getPriceDollars());
            } else {
                return new SimpleStringProperty("");
            }
        });
        titleNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        titleTable.getItems().setAll(this.getTitles());
        titleTable.getSortOrder().add(titleTitleColumn);
        

        //Populate columns for flagged titles table in New Week Pulls Tab
        flaggedTitleColumn.setCellValueFactory(new PropertyValueFactory<>("flaggedTitleName"));
        flaggedIssueColumn.setCellValueFactory(cell -> {
            if (cell.getValue().getFlaggedIssueNumber() > 0) {
                return new SimpleStringProperty(Integer.toString(cell.getValue().getFlaggedIssueNumber()));
            } else {
                return new SimpleStringProperty("");
            }
        });
        flaggedPriceColumn.setCellValueFactory(cell -> {
            if (cell.getValue().getFlaggedPriceCents() > 0) {
                return new SimpleStringProperty(cell.getValue().getFlaggedPriceDollars());
            } else {
                return new SimpleStringProperty("");
            }
        });
        flaggedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("flaggedQuantity"));
        flaggedNumRequestsColumn.setCellValueFactory(new PropertyValueFactory<>("flaggedNumRequests"));

        //for requests table, sort by title
        requestLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("RequestLastName"));
        requestFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("RequestFirstName"));
        requestQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("RequestQuantity"));

        breakdownTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        breakdownQuantityColumn.setCellValueFactory(cell -> new SimpleStringProperty(Integer.toString(getTitleQuantity(cell.getValue().getId()))));
        breakdownPendingIssueColumn.setCellValueFactory(cell -> {
            if (hasPendingIssueRequest(cell.getValue().getId())) {
                return new SimpleStringProperty("Y");
            } else {
                return new SimpleStringProperty("N");
            }
        });
        breakdownFlaggedColumn.setCellValueFactory(cell -> {
            LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
            if (cell.getValue().getDateFlagged() == null) {
                return new SimpleStringProperty("N");
            } else if (cell.getValue().getDateFlagged().isBefore(sixMonthsAgo)) {
                return new SimpleStringProperty("N");
            }
            return new SimpleStringProperty("Y");
        });
        monthlyBreakdownTable.getItems().setAll(getTitles());
        monthlyBreakdownTable.getSortOrder().add(breakdownTitleColumn);

        //Title Orders table
        titleOrderLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("RequestLastName"));
        titleOrderFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("RequestFirstName"));
        titleOrderQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("RequestQuantity"));
        titleOrderIssueColumn.setCellValueFactory(new PropertyValueFactory<>("RequestIssue"));

        //Load the data for the Reports tab
        this.loadReportsTab();

        //Add Listener for selected Customer
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                customerFirstNameText.setText(newSelection.getFirstName());
                customerLastNameText.setText(newSelection.getLastName());
                customerPhoneText.setText(newSelection.getPhone());
                customerEmailText.setText(newSelection.getEmail());
                updateOrdersTable(newSelection);
            }
        });

        //Add Listener for Titles table
        titleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                titleTitleText.setText(newSelection.getTitle());
                if (newSelection.getPrice() > 0) {
                    titlePriceText.setText(newSelection.getPriceDollars());
                } else {
                    titlePriceText.setText("");
                }
                titleNotesText.setText(newSelection.getNotes());
                String numberRequests = String.format("This Title Currently has %s Customer Requests", getNumberRequests(newSelection.getId()));
                LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
                if (newSelection.getDateFlagged() != null) {
                    titleDateFlagged.setText(newSelection.getDateFlagged().toString());
                    if (newSelection.getDateFlagged().isBefore(sixMonthsAgo)) {
                        titleDateFlaggedNoticeText.setVisible(true);
                    }
                    else {
                        titleDateFlaggedNoticeText.setVisible(false);
                    }
                }
                else {
                    titleDateFlagged.setText("Never");
                    titleDateFlaggedNoticeText.setVisible(true);
                }
                titleNumberRequestsText.setText(numberRequests);

                // System.out.println(titleOrderFirstNameColumn.toString());
                // System.out.println(titleOrderLastNameColumn.toString());
                // System.out.println(titleOrderQuantityColumn.toString());
                // System.out.println(this.getRequests(newSelection.getId(), -1).size() + " : " + newSelection.getId());
                titleOrdersTable.getItems().setAll(this.getRequests(newSelection.getId(), -9));
            }
        });

        //add listener for selected flagged title
        flaggedTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {

                //first the summary info for the flagged title is set
                if (newSelection.getFlaggedIssueNumber() > 0) {
                    RequestTitleText.setText(newSelection.getFlaggedTitleName() + " " + newSelection.getFlaggedIssueNumber());
                }
                else {
                    RequestTitleText.setText(newSelection.getFlaggedTitleName());
                }
                RequestQuantityText.setText(Integer.toString(newSelection.getFlaggedQuantity()));
                RequestNumCustomersText.setText(Integer.toString(newSelection.getFlaggedNumRequests()));

                // System.out.println(this.getRequests(newSelection.getTitleId(), -1).size() + " : " + newSelection.getTitleId());
                requestsTable.getItems().setAll(this.getRequests(newSelection.getTitleId(), newSelection.getFlaggedIssueNumber()));
            }
        });

        getDatabaseInfo();
    }

    //#endregion

/*######################################################################/
///////////////////////////// FXML Functions ////////////////////////////
/######################################################################*/

    //#region FXML Functions

    /**
     * Runs when the Add Customer button is pressed. Creates a new window for
     * the user to enter information and create a customer. Re-renders the
     * Customer table on window close.
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleAddCustomer(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NewCustomerBox.fxml"));
            Parent root = fxmlLoader.load();

            NewCustomerController newCustomerController = fxmlLoader.getController();
            newCustomerController.setConnection(conn);

            Stage window = new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.setTitle("Add Customer");
            window.setResizable(false);
            window.setHeight(280);
            window.setWidth(400);
            window.setScene(new Scene(root));
            window.setOnHidden( e -> {
                customerTable.getItems().setAll(getCustomers());
                this.loadReportsTab();
                getDatabaseInfo();
            });

            window.show();
        } catch (Exception e) {
            System.out.println("Error when opening window. This is probably a bug");
            e.printStackTrace();
        }
    }

    /**
     * Runs when the Add Title button is pressed. Creates a new window for
     * the user to enter information and create a title. Re-renders the
     * Title table on window close.
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleAddTitle(ActionEvent event) {
        if (unsaved)
        {
            AlertBox.display("Flags Have Not Been Saved", "Please save or reset flags before adding a title.");
        }
        else 
        {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NewTitleBox.fxml"));
                Parent root = fxmlLoader.load();

                NewTitleController newTitleController = fxmlLoader.getController();
                newTitleController.setConnection(this.conn);

                Stage window = new Stage();
                window.initModality(Modality.APPLICATION_MODAL);
                window.setTitle("Add Title");
                window.setResizable(false);
                window.setHeight(285);
                window.setWidth(400);
                window.setScene(new Scene(root));
                window.setOnHidden( e -> {
                    titleTable.getItems().setAll(getTitles());
                    this.loadReportsTab();
                    getDatabaseInfo();
                });

                window.show();
            } catch (Exception e) {
                System.out.println("Error when opening window. This is probably a bug");
                e.printStackTrace();
            }
        }
    }

    /**
     * Runs when the Delete Customer button is pressed. Creates a dialog for the
     * user to confirm deletion of the selected Customer. It also deletes every order
     * linked to the customer. Re-renders the Customer and the order tables on window close.
     *
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleDeleteCustomer(ActionEvent event) {
        String firstName = customerFirstNameText.getText();
        String lastName = customerLastNameText.getText();

        if (customerTable.getSelectionModel().getSelectedItem() == null) {
            AlertBox.display("Confirm Delete", "Please select a customer.");
        }
        else {
            int customerId = customerTable.getSelectionModel().getSelectedItem().getId();

            boolean confirmDelete = ConfirmBox.display(
                    "Confirm Delete",
                    "Are you sure you would like to delete customer " + firstName + " " + lastName + "?");
            if (confirmDelete) {
                PreparedStatement s = null; // To prepare and execute the sql statement to delete the customer
                String sql = "DELETE FROM Customers WHERE customerId = ?";
                String sql2 = "DELETE FROM Orders WHERE customerId = ?";

                try {
                    s = conn.prepareStatement(sql2);
                    s.setString(1, Integer.toString(customerId));
                    s.executeUpdate();
                    s.close();

                    s = conn.prepareStatement(sql);
                    s.setString(1, Integer.toString(customerId));
                    s.executeUpdate();
                    s.close();

                    Log.LogEvent("Customer Deleted", "Deleted Customer - " + firstName + " " + lastName);

                } catch (SQLException sqlExcept) {
                    sqlExcept.printStackTrace();
                }
            }
            customerTable.getItems().setAll(getCustomers());
            customerFirstNameText.setText("");
            customerLastNameText.setText("");
            customerPhoneText.setText("");
            customerEmailText.setText("");

            titleTable.getItems().setAll(getTitles());
            if (customerTable.getSelectionModel().getSelectedItem() != null) {
                updateOrdersTable(customerTable.getSelectionModel().getSelectedItem());
            }

            getDatabaseInfo();
            this.loadReportsTab();
        }
    }

    /**
     * Runs when the Delete Request button is pressed. Creates a dialog for the
     * user to confirm deletion of the selected Order. Re-renders the Order
     * table on window close.
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleDeleteOrder(ActionEvent event) {
        String titleDP = titleTitleText.getText();
        String title = customerOrderTable.getSelectionModel().getSelectedItem().getTitleName();

        if (customerOrderTable.getSelectionModel().getSelectedItem() == null) {
            AlertBox.display("Confirm Delete", "Please select an order.");
        } else {
            int customerId = customerOrderTable.getSelectionModel().getSelectedItem().getCustomerId();
            int titleId = customerOrderTable.getSelectionModel().getSelectedItem().getTitleId();
            int quantity = customerOrderTable.getSelectionModel().getSelectedItem().getQuantity();
            int issue = customerOrderTable.getSelectionModel().getSelectedItem().getIssue();


            boolean confirmDelete = ConfirmBox.display(
                    "Confirm Delete",
                    "Are you sure you would like to delete " + title + "?");
            if (confirmDelete) {
                PreparedStatement s = null;
                String sql;
                if (issue == 0) {
                    sql = "DELETE FROM ORDERS WHERE CUSTOMERID = ? AND TITLEID = ? AND ISSUE IS NULL";
                } else {
                    sql = "DELETE FROM ORDERS WHERE CUSTOMERID = ? AND TITLEID = ? AND ISSUE = ?";
                }
                try {
                    s = conn.prepareStatement(sql);
                    s.setInt(1, customerId);
                    s.setInt(2, titleId);
                    if (issue != 0) {
                        s.setInt(3, issue);
                    }
                    s.executeUpdate();
                    s.close();

                    Log.LogEvent("Deleted Order", "Deleted order - CustomerID: " + customerId + " - Title: " + title + " - Quantity: " + quantity + " - Issue: " + Integer.valueOf(issue));
                } catch (SQLException sqlExcept) {
                    sqlExcept.printStackTrace();
                }
            }
            titleTable.getItems().setAll(getTitles());
            updateOrdersTable(customerTable.getSelectionModel().getSelectedItem());

            titleOrdersTable.getItems().clear();

            this.loadReportsTab();
            getDatabaseInfo();
        }
    }

    /**
     * Runs when the Delete Title button is pressed. Creates a dialog for the
     * user to confirm deletion of the selected Title. It also deletes evry order
     * linked to this title. Re-renders the Title and Order table on window close.
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleDeleteTitle(ActionEvent event) {
        String title = titleTitleText.getText();

        if (titleTable.getSelectionModel().getSelectedItem() == null) {
            AlertBox.display("Confirm Delete", "Please select a title.");
        }
        else if (unsaved)
        {
            AlertBox.display("Flags Have Not Been Saved", "Please save or reset flags before deleting a title.");
        }
        else {
            int titleId = titleTable.getSelectionModel().getSelectedItem().getId();
            int req = getNumberRequests(titleId);
            boolean confirmDelete;
            if ( req > 0)
            {
                confirmDelete = ConfirmBox.display(
                    "Confirm Delete",
                    "Are you sure you would like to delete " + title + "?\nThere are " + req + "requests for this title!");
            }
            else 
            {
                confirmDelete = ConfirmBox.display(
                    "Confirm Delete",
                    "Are you sure you would like to delete " + title + "?");}
            if (confirmDelete) {
                PreparedStatement s = null;
                String sql = "DELETE FROM TITLES WHERE TITLEID = ?";
                String sql2 = "DELETE FROM ORDERS WHERE TITLEID = ?";

                try {
                    s = conn.prepareStatement(sql2);
                    s.setString(1, Integer.toString(titleId));
                    s.executeUpdate();
                    s.close();

                    s = conn.prepareStatement(sql);
                    s.setString(1, Integer.toString(titleId));
                    s.executeUpdate();
                    s.close();

                    Log.LogEvent("Deleted Title", "Deleted Title - Title: " + title + " - TitleID: " + titleId);
                } catch (SQLException sqlExcept) {
                    sqlExcept.printStackTrace();
                }
            }
            titleTable.getItems().setAll(getTitles());
            titleTitleText.setText("");
            titlePriceText.setText("");
            titleNotesText.setText("");

            titleTable.getItems().setAll(getTitles());
            if (customerTable.getSelectionModel().getSelectedItem() != null) {
                updateOrdersTable(customerTable.getSelectionModel().getSelectedItem());
            }

            titleOrdersTable.getItems().clear();

            getDatabaseInfo();
            this.loadReportsTab();
        }
    }

    /**
     * Runs when the Edit Customer button is pressed. Creates a new window for
     * the user to enter information and edit a Customer. Re-renders the
     * Customer table on window close.
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleEditCustomer(ActionEvent event) {
        if (customerTable.getSelectionModel().getSelectedItem() == null) {
            AlertBox.display("Confirm Edit", "Please select a customer.");
        }
        else {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("EditCustomerBox.fxml"));
                Parent root = fxmlLoader.load();

                EditCustomerController editCustomerController = fxmlLoader.getController();
                editCustomerController.setConnection(conn);
                editCustomerController.setCustomer(customerTable.getSelectionModel().getSelectedItem());

                Stage window = new Stage();
                window.initModality(Modality.APPLICATION_MODAL);
                window.setTitle("Edit Customer");
                window.setResizable(false);
                window.setHeight(280);
                window.setWidth(400);
                window.setScene(new Scene(root));
                window.setOnHidden(e -> {
                    customerTable.getItems().setAll(getCustomers());
                    customerFirstNameText.setText("");
                    customerLastNameText.setText("");
                    customerPhoneText.setText("");
                    customerEmailText.setText("");
                    this.loadReportsTab();
                    getDatabaseInfo();
                });
                window.show();
            } catch (Exception e) {
                System.out.println("Error when opening window. This is probably a bug");
                e.printStackTrace();
            }
        }
    }

    /**
     * Runs when the Edit request(order) button is pressed. Creates a new window for
     * the user to enter information and edit order. Re-renders the
     * Orders table on window close.
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleEditOrder(ActionEvent event) {
        if (customerOrderTable.getSelectionModel().getSelectedItem() == null) {
            AlertBox.display("Confirm Edit", "Please select an order.");
        }
        else {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("EditOrderBox.fxml"));
                Parent root = fxmlLoader.load();

                EditOrderController editOrderController = fxmlLoader.getController();
                editOrderController.setConnection(conn);

                editOrderController.populate(this.getTitles());
                editOrderController.setOrder(customerOrderTable.getSelectionModel().getSelectedItem());

                Stage window = new Stage();
                window.initModality(Modality.APPLICATION_MODAL);
                window.setTitle("Edit Order");
                window.setResizable(false);

                window.setHeight(285);
                window.setWidth(400);

                window.setScene(new Scene(root));
                window.setOnHidden(e -> {
                    updateOrdersTable(customerTable.getSelectionModel().getSelectedItem());
                    this.loadReportsTab();
                    getDatabaseInfo();

                    titleTable.getItems().setAll(getTitles());
                });
                window.show();
            } catch (Exception e) {
                System.out.println("Error when opening window. This is probably a bug");
                e.printStackTrace();
            }

            titleOrdersTable.getItems().clear();
        }
    }

    /**
     * Runs when the Edit Title button is pressed. Creates a new window for
     * the user to enter information and edit a title. Re-renders the
     * Title table on window close.
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleEditTitle(ActionEvent event) {
        if (titleTable.getSelectionModel().getSelectedItem() == null) {
            AlertBox.display("Confirm Edit", "Please select a title.");
        }
        else if (unsaved)
        {
            AlertBox.display("Flags Have Not Been Saved", "Please save or reset flags before editing a title.");
        }
        else {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("EditTitleBox.fxml"));
                Parent root = fxmlLoader.load();

                EditTitleController editTitleController = fxmlLoader.getController();
                editTitleController.setConnection(conn);
                editTitleController.setTitle(titleTable.getSelectionModel().getSelectedItem());

                Stage window = new Stage();
                window.initModality(Modality.APPLICATION_MODAL);
                window.setTitle("Edit Title");
                window.setResizable(false);

                window.setHeight(285);
                window.setWidth(400);

                window.setScene(new Scene(root));
                window.setOnHidden(e -> {
                    titleTable.getItems().setAll(getTitles());
                    titleTitleText.setText("");
                    titlePriceText.setText("");
                    titleNotesText.setText("");
                    this.loadReportsTab();
                    getDatabaseInfo();
                });
                window.show();
            } catch (Exception e) {
                System.out.println("Error when opening window. This is probably a bug");
                e.printStackTrace();
            }
        }
    }

    /**
     * Runs when the Add Request button is pressed. Creates a new window for
     * the user to enter information and create an Order. Re-renders the
     * Orders table on window close.
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleNewOrder(ActionEvent event) {
        if (customerTable.getSelectionModel().getSelectedItem() == null) {
            AlertBox.display("New Order", "Please select a customer.");
        }
        else {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AddOrderBox.fxml"));
                Parent root = fxmlLoader.load();

                NewOrderController newOrderController = fxmlLoader.getController();
                newOrderController.setConnection(conn);
                newOrderController.setCustomerID(customerTable.getSelectionModel().getSelectedItem().getId());
                newOrderController.populate(this.getTitles());
                newOrderController.setNewOrder();

                Stage window = new Stage();
                window.initModality(Modality.APPLICATION_MODAL);
                window.setTitle("New Order");
                window.setResizable(false);
                window.setHeight(250);
                window.setWidth(400);
                window.setScene(new Scene(root));
                window.setOnHidden(e ->  {
                    updateOrdersTable(customerTable.getSelectionModel().getSelectedItem());
                    this.loadReportsTab();
                    getDatabaseInfo();

                    if (titleTable.getSelectionModel().getSelectedItem() != null) {
                        Title title = titleTable.getSelectionModel().getSelectedItem();
                        titleOrdersTable.getItems().setAll(this.getRequests(title.getId(), -9));
                    }
                });
                window.show();

            } catch (Exception e) {
                System.out.println("Error when opening window. This is probably a bug");
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates a report that organizes all customer requests by title. Writes every title with customer request
     * information underneath to an Excel spreadsheet.
     * @param event
     */
    @FXML
    void handleExportAllRequestsByTitle(ActionEvent event) {
        ObservableList<Title> titles = titleTable.getItems();

        LocalDate today = LocalDate.now();
        String fileName = "All Customer Requests by Title " + today + ".xlsx";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Location");
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());

        if (file != null) {
            file = addFileExtension(file);

            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet(fileName);
            sheet.setColumnWidth(0, 6000);

            Row header = sheet.createRow(0);

            org.apache.poi.ss.usermodel.Cell headerCell = header.createCell(0);
            headerCell.setCellValue("Date: " + today);
            header = sheet.createRow(1);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 2));
            headerCell = header.createCell(0);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCell.setCellStyle(cellStyle);
            headerCell.setCellValue("All Customer Requests by Title");
            int rowIndex = 2;
            for (Title title : titles) {
                rowIndex = exportSingleTitle(workbook, title, rowIndex, false, false);
            }

            Log.LogEvent("Export All Requests", "Exported all requests by title");

            saveReport(file, workbook);
        }
    }

    /**
     * Exports all titles into a list with quantities and number of requests in an Excel spreadsheet
     * @param event the event that triggered this method call
     */
    @FXML
    void handleExportAllTitles(ActionEvent event) {
        LocalDate today = LocalDate.now();
        String fileName = "All Titles " + today + ".xlsx";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Location");
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());

        if (file != null) {
            file = addFileExtension(file);

            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet(fileName);
            sheet.setColumnWidth(0, 8000);
            sheet.setColumnWidth(3, 4500);

            Row header = sheet.createRow(0);

            org.apache.poi.ss.usermodel.Cell headerCell = header.createCell(0);
            headerCell.setCellValue("Date: " + today);
            header = sheet.createRow(1);
            sheet.addMergedRegion(new CellRangeAddress(1,1,0,3));
            headerCell = header.createCell(0);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCell.setCellStyle(cellStyle);
            headerCell.setCellValue("All Titles");

            sheet.setRepeatingRows(new CellRangeAddress(3,3,0,3));
            sheet.getHeader().setRight("Page &P of &N");

            Font bold = workbook.createFont();
            bold.setBold(true);

            CellStyle headStyle = workbook.createCellStyle();
            headStyle.setFont(bold);
            headStyle.setAlignment(HorizontalAlignment.RIGHT);

            Row row = sheet.createRow(3);
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
            CellStyle titleHeadStyle = workbook.createCellStyle();
            titleHeadStyle.setFont(bold);
            cell.setCellStyle(titleHeadStyle);
            cell.setCellValue("Title");

            cell = row.createCell(1);
            cell.setCellValue("Issue");
            cell.setCellStyle(headStyle);

            cell = row.createCell(2);
            cell.setCellValue("Quantity");
            cell.setCellStyle(headStyle);

            cell = row.createCell(3);
            cell.setCellValue("Number of Requests");
            cell.setCellStyle(headStyle);

            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);

            ResultSet result;
            Statement s = null;
            try
            {
                String sql = """
                    SELECT TITLE, ISSUE, SUM(QUANTITY) AS QUANTITY, COUNT(CUSTOMERID) AS NUM_REQUESTS FROM (
                       SELECT TITLES.TITLEID, TITLES.TITLE, ORDERS.CUSTOMERID, ORDERS.ISSUE, ORDERS.QUANTITY
                       from TITLES
                                LEFT JOIN ORDERS ON ORDERS.TITLEID = TITLES.TITLEID
                    ) AS ORDERS
                    GROUP BY TITLEID, TITLE, ISSUE
                    ORDER BY TITLE, ISSUE
                    """;

                s = conn.createStatement();
                result = s.executeQuery(sql);

                CellStyle rightAlign = workbook.createCellStyle();
                rightAlign.setAlignment(HorizontalAlignment.RIGHT);

                int i = 4;
                while (result.next()) {
                    String title = result.getString("TITLE");
                    Object issue = result.getObject("ISSUE");
                    int quantity = result.getInt("QUANTITY");
                    int numRequests = result.getInt("NUM_REQUESTS");

                    row = sheet.createRow(i);

                    cell = row.createCell(0);
                    cell.setCellValue(title);
                    cell.setCellStyle(wrapStyle);

                    cell = row.createCell(1);
                    if (issue != null) {
                        cell.setCellValue(Integer.parseInt(issue.toString()));
                    } else {
                        cell.setCellValue("All");
                    }
                    cell.setCellStyle(rightAlign);

                    cell = row.createCell(2);
                    cell.setCellValue(quantity);
                    cell.setCellStyle(rightAlign);

                    cell = row.createCell(3);
                    cell.setCellValue(numRequests);
                    cell.setCellStyle(wrapStyle);

                    i++;
                }
                result.close();
                s.close();

                Log.LogEvent("Export All Titles", "Exported all titles");

                saveReport(file, workbook);
            }
            catch (SQLException sqlExcept)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Database Error. Report may not have saved successfully", ButtonType.OK);
                alert.setTitle("Database Error");
                alert.show();
            }
        }
    }

    /**
     * Exports a list of all customers to an Excel spreadsheet
     * @param event The event that triggered this method call
     */
    @FXML
    void handleExportCustomerList(ActionEvent event) {
        LocalDate today = LocalDate.now();
        String fileName = "Customer List " + today + ".xlsx";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Location");
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());

        if (file != null) {
            file = addFileExtension(file);

            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet(fileName);
            sheet.setColumnWidth(0, 8000);
            sheet.setColumnWidth(1, 4000);
            sheet.setColumnWidth(2, 6000);

            Row header = sheet.createRow(0);

            org.apache.poi.ss.usermodel.Cell headerCell = header.createCell(0);
            headerCell.setCellValue("Date: " + today);
            header = sheet.createRow(1);
            sheet.addMergedRegion(new CellRangeAddress(1,1,0,2));
            headerCell = header.createCell(0);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCell.setCellStyle(cellStyle);
            headerCell.setCellValue("All Customers by Last Name");

            sheet.setRepeatingRows(new CellRangeAddress(3,3,0,2));
            sheet.getHeader().setRight("Page &P of &N");

            Font bold = workbook.createFont();
            bold.setBold(true);

            CellStyle headStyle = workbook.createCellStyle();
            headStyle.setFont(bold);

            Row row = sheet.createRow(3);
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
            CellStyle titleHeadStyle = workbook.createCellStyle();
            titleHeadStyle.setFont(bold);
            cell.setCellStyle(titleHeadStyle);
            cell.setCellValue("Customer");

            cell = row.createCell(1);
            cell.setCellStyle(headStyle);
            cell.setCellValue("Phone");

            cell = row.createCell(2);
            cell.setCellStyle(headStyle);
            cell.setCellValue("Email");

            ResultSet result;
            Statement s = null;
            try
            {
                String sql = """
                            SELECT * FROM CUSTOMERS
                            ORDER BY LASTNAME
                            """;

                s = conn.createStatement();
                result = s.executeQuery(sql);
                int i = 4;
                while(result.next()) {
                    row = sheet.createRow(i);
                    cell = row.createCell(0);
                    String lastName = result.getString("LASTNAME");
                    String firstName = result.getString("FIRSTNAME");
                    cell.setCellValue(lastName + ", " + firstName);
                    cell = row.createCell(1);
                    cell.setCellValue(result.getString("PHONE"));
                    cell = row.createCell(2);
                    cell.setCellValue(result.getString("EMAIL"));
                    i++;
                }
                result.close();
                s.close();

                Log.LogEvent("Export All Customers", "Exporting all customers");

                saveReport(file, workbook);
            }
            catch (SQLException sqlExcept)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Database Error. Report may not have saved successfully", ButtonType.OK);
                alert.setTitle("Database Error");
                alert.show();
            }
        }
    }

    /**
     * Creates a report to export all of the flagged titles, in the same format as the All Requests by Title report
     */
    @FXML
    void handleExportFlaggedTitles(ActionEvent event) {
        ObservableList<FlaggedTable> titles = getFlaggedTitles();
        ObservableList<Title> titlesTable = titleTable.getItems();

        LocalDate today = LocalDate.now();
        String fileName = "All Flagged Titles " + today + ".xlsx";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Location");
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());

        if (file != null) {
            file = addFileExtension(file);

            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet(fileName);
            sheet.setColumnWidth(0, 6000);

            Row header = sheet.createRow(0);

            org.apache.poi.ss.usermodel.Cell headerCell = header.createCell(0);
            headerCell.setCellValue("Date: " + today);
            header = sheet.createRow(1);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 2));
            headerCell = header.createCell(0);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCell.setCellStyle(cellStyle);
            headerCell.setCellValue("All Flagged Titles with Requests");
            int rowIndex = 2;
            for (FlaggedTable flagged : titles) {
                Title tempTitle = null;
                for (Title title : titlesTable) {
                    if (title.getId() == flagged.getTitleId()) {
                        tempTitle = title;
                    }
                }
                rowIndex = exportSingleTitle(workbook, tempTitle, rowIndex, false, true);
            }

            Log.LogEvent("Export Flagged Titles", "Exporting all flagged titles");

            saveReport(file, workbook);
        }
    }

    /**
     * Exports a list of all titles with no customer requests to an Excel spreadsheet
     * @param event the event that triggered this method call
     */
    @FXML
    void handleExportNoRequestTitles(ActionEvent  event) {
        LocalDate today = LocalDate.now();
        String fileName = "Zero Request Titles " + today + ".xlsx";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Location");
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());
        file = addFileExtension(file);

        if (file != null) {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet(fileName);
            sheet.setColumnWidth(0, 10000);

            Row header = sheet.createRow(0);

            org.apache.poi.ss.usermodel.Cell headerCell = header.createCell(0);
            headerCell.setCellValue("Date: " + today);
            header = sheet.createRow(1);
            headerCell = header.createCell(0);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCell.setCellStyle(cellStyle);
            headerCell.setCellValue("Titles with Zero Requests");

            sheet.setRepeatingRows(new CellRangeAddress(3,3,0,0));
            sheet.getHeader().setRight("Page &P of &N");

            Font bold = workbook.createFont();
            bold.setBold(true);

            CellStyle headStyle = workbook.createCellStyle();
            headStyle.setFont(bold);

            Row row = sheet.createRow(3);
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
            cell.setCellStyle(headStyle);
            cell.setCellValue("Title");

            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);

            ObservableList<Title> titles = titleTable.getItems();
            int i = 0;
            for (Title title : titles) {
                if (getNumberRequests(title.getId()) == 0) {
                    row = sheet.createRow(i+4);

                    cell = row.createCell(0);
                    cell.setCellValue(title.getTitle());
                    cell.setCellStyle(wrapStyle);

                    i++;
                }
            }
            row = sheet.createRow(i+4);
            cell = row.createCell(0);
            cell.setCellValue("Total: " + i);

            Log.LogEvent("Export No Request Titles", "Exporting all titles with no requests");

            saveReport(file, workbook);
        }
    }

    /**
     * Exports all titles with pending issue number requests to an Excel spreadsheet
     * @param event the event that triggered this method call
     */
    @FXML
    void handleExportPendingIssueNumbers(ActionEvent event) {
        LocalDate today = LocalDate.now();
        String fileName = "Pending Issue Requests " + today + ".xlsx";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Location");
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());

        if (file != null) {
            file = addFileExtension(file);

            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet(fileName);
            sheet.setColumnWidth(0, 8000);
            sheet.setColumnWidth(3, 6000);

            Row header = sheet.createRow(0);

            org.apache.poi.ss.usermodel.Cell headerCell = header.createCell(0);
            headerCell.setCellValue("Date: " + today);
            header = sheet.createRow(1);
            sheet.addMergedRegion(new CellRangeAddress(1,1,0,2));
            headerCell = header.createCell(0);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCell.setCellStyle(cellStyle);
            headerCell.setCellValue("Pending Issue Number Requests");

            sheet.setRepeatingRows(new CellRangeAddress(3,3,0,3));
            sheet.getHeader().setRight("Page &P of &N");

            Font bold = workbook.createFont();
            bold.setBold(true);

            CellStyle headStyle = workbook.createCellStyle();
            headStyle.setFont(bold);
            headStyle.setAlignment(HorizontalAlignment.RIGHT);

            Row row = sheet.createRow(3);
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
            CellStyle titleHeadStyle = workbook.createCellStyle();
            titleHeadStyle.setFont(bold);
            cell.setCellStyle(titleHeadStyle);
            cell.setCellValue("Title");

            cell = row.createCell(1);
            cell.setCellValue("Issue");
            cell.setCellStyle(headStyle);

            cell = row.createCell(2);
            cell.setCellValue("Quantity");
            cell.setCellStyle(headStyle);

            cell = row.createCell(3);
            cell.setCellStyle(titleHeadStyle);
            cell.setCellValue("Customer");

            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);

            ResultSet result;
            Statement s = null;
            try
            {
                String sql = """
                            SELECT T.TITLE, ISSUE_REQUESTS.CUSTOMERID, ISSUE_REQUESTS.TITLEID, ISSUE_REQUESTS.QUANTITY, ISSUE_REQUESTS.ISSUE, ISSUE_REQUESTS.FIRSTNAME, ISSUE_REQUESTS.LASTNAME
                            FROM (
                                              SELECT O.CUSTOMERID, O.TITLEID, O.QUANTITY, O.ISSUE, C.FIRSTNAME, C.LASTNAME
                                              FROM ORDERS O
                                                       INNER JOIN CUSTOMERS C on O.CUSTOMERID = C.CUSTOMERID
                                              WHERE ISSUE IS NOT NULL
                                          ) AS ISSUE_REQUESTS
                            INNER JOIN TITLES T on ISSUE_REQUESTS.TITLEID = T.TITLEID
                            ORDER BY T.TITLE
                            """;

                s = conn.createStatement();
                result = s.executeQuery(sql);
                CellStyle reqItemStyle = workbook.createCellStyle();
                reqItemStyle.setWrapText(true);
                CellStyle rightAlign = workbook.createCellStyle();
                rightAlign.setAlignment(HorizontalAlignment.RIGHT);

                int i = 4;
                while (result.next()) {
                    String title = result.getString("TITLE");
                    String name = result.getString("LASTNAME") + " " + result.getString("FIRSTNAME");
                    Object issue = result.getObject("ISSUE");
                    int quantity = result.getInt("QUANTITY");

                    row = sheet.createRow(i);

                    cell = row.createCell(0);
                    cell.setCellValue(title);
                    cell.setCellStyle(wrapStyle);

                    cell = row.createCell(1);
                    if (issue != null) {
                        cell.setCellValue(Integer.parseInt(issue.toString()));
                    } else {
                        cell.setCellValue("All");
                    }
                    cell.setCellStyle(rightAlign);

                    cell = row.createCell(2);
                    cell.setCellValue(quantity);
                    cell.setCellStyle(rightAlign);

                    cell = row.createCell(3);
                    cell.setCellValue(name);
                    cell.setCellStyle(wrapStyle);

                    i++;
                }
                result.close();
                s.close();

                Log.LogEvent("Export Pending Issue Titles", "Exporting all titles with pending issue numbers");

                saveReport(file, workbook);
            }
            catch (SQLException sqlExcept)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Database Error. Report may not have saved successfully", ButtonType.OK);
                alert.setTitle("Database Error");
                alert.show();
            }
        }
    }

    /**
     * Creates a Excel report for a single Customer. Gets all available requests for the customer and writes them
     * to an Excel spreadsheet.
     */
    @FXML
    void handleExportSingleCustomer(ActionEvent event) {
        Customer customer = customerTable.getSelectionModel().getSelectedItem();

        if (customer == null) {
            Alert selectedAlert = new Alert(Alert.AlertType.INFORMATION, "Please select a customer.", ButtonType.OK);
            selectedAlert.setTitle("Confirm Export");
            selectedAlert.setHeaderText("");
            selectedAlert.show();
        }

        LocalDate today = LocalDate.now();
        String fileName = customer.getFullName() + " Requests " + today + ".xlsx";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Location");
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());

        if (file != null) {
            file = addFileExtension(file);

            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet(fileName);
            sheet.setColumnWidth(0, 6000);

            Row header = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell headerCell = header.createCell(0);
            headerCell.setCellValue("Date: " + today);
            header = sheet.createRow(1);
            sheet.addMergedRegion(new CellRangeAddress(1,1,0,2));
            headerCell = header.createCell(0);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCell.setCellStyle(cellStyle);
            headerCell.setCellValue("Single Customer Request List");

            sheet.setRepeatingRows(new CellRangeAddress(3,3,0,2));
            sheet.getHeader().setRight("Page &P of &N");

            Row row = sheet.createRow(2);
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
            cell.setCellValue("Customer: " + customer.getFullName());

            Font bold = workbook.createFont();
            bold.setBold(true);
            CellStyle headStyle = workbook.createCellStyle();
            headStyle.setFont(bold);
            headStyle.setAlignment(HorizontalAlignment.RIGHT);

            row = sheet.createRow(3);

            cell = row.createCell(0);
            CellStyle reqItemHeadStyle = workbook.createCellStyle();
            reqItemHeadStyle.setFont(bold);
            cell.setCellStyle(reqItemHeadStyle);
            cell.setCellValue("Requested Item");

            cell = row.createCell(1);
            cell.setCellStyle(headStyle);
            cell.setCellValue("Issue");

            cell = row.createCell(2);
            cell.setCellStyle(headStyle);
            cell.setCellValue("Quantity");

            CellStyle reqItemStyle = workbook.createCellStyle();
            reqItemStyle.setWrapText(true);
            CellStyle rightAlign = workbook.createCellStyle();
            rightAlign.setAlignment(HorizontalAlignment.RIGHT);

            Statement s = null;
            try
            {
                String sql = String.format("""
                        SELECT ORDERS.CUSTOMERID, ORDERS.TITLEID, TITLES.title, ORDERS.QUANTITY, ORDERS.ISSUE FROM TITLES
                        INNER JOIN ORDERS ON Orders.titleID=TITLES.TitleId
                        WHERE ORDERS.CUSTOMERID=%s
                        ORDER BY TITLE
                        """, customer.getId());

                s = conn.createStatement();
                ResultSet results = s.executeQuery(sql);

                int i = 4;
                int totalQuantity = 0;
                while(results.next())
                {
                    row = sheet.createRow(i);
                    cell = row.createCell(0);
                    cell.setCellStyle(reqItemStyle);
                    cell.setCellValue(results.getString("TITLE"));
                    cell = row.createCell(1);
                    Object issue = results.getObject("ISSUE");
                    if (issue != null) {
                        cell.setCellValue(Integer.parseInt(issue.toString()));
                    } else {
                        cell.setCellValue("All");
                    }
                    cell.setCellStyle(rightAlign);
                    cell = row.createCell(2);
                    int quantity = results.getInt("QUANTITY");
                    cell.setCellValue(quantity);
                    cell.setCellStyle(rightAlign);
                    totalQuantity += quantity;
                    i++;
                }
                results.close();
                s.close();

                row = sheet.createRow(i);
                cell = row.createCell(0);
                cell.setCellValue("Total:");
                cell = row.createCell(2);
                cell.setCellValue(totalQuantity);

                Log.LogEvent("Export Single Customer", "Exporting a single customer - Customer Name: " + customer.getFirstName() + " " + customer.getLastName());

                saveReport(file, workbook);
            }
            catch (SQLException sqlExcept)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Database Error. Report may not have saved successfully", ButtonType.OK);
                alert.setTitle("Database Error");
                alert.show();
            }
        }
    }

    /**
     * This method is called when the singleTitleReportButton button is
     * clicked.
     */
    @FXML
    void handleExportSingleTitle(ActionEvent event) {
        Title title = titleTable.getSelectionModel().getSelectedItem();
        if (title == null) {
            Alert selectedAlert = new Alert(Alert.AlertType.INFORMATION, "Please select a title.", ButtonType.OK);
            selectedAlert.setTitle("Confirm Export");
            selectedAlert.setHeaderText("");
            selectedAlert.show();
        } else {

            LocalDate today = LocalDate.now();
            String fileName = title.getTitle() + " Requests " + today + ".xlsx";

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Location");
            fileChooser.setInitialFileName(fileName);
            File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());

            if (file != null) {
                file = addFileExtension(file);

                Workbook workbook = new XSSFWorkbook();

                Sheet sheet = workbook.createSheet(fileName);
                sheet.setColumnWidth(0, 6000);

                Row header = sheet.createRow(0);

                org.apache.poi.ss.usermodel.Cell headerCell = header.createCell(0);
                headerCell.setCellValue("Date: " + today);
                header = sheet.createRow(1);
                sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 2));
                headerCell = header.createCell(0);
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                headerCell.setCellStyle(cellStyle);
                headerCell.setCellValue("Single Title Customer List");
                exportSingleTitle(workbook, title, 2, true, false);
                saveReport(file, workbook);
            }
        }
    }

    /**
     * Handler for the Export Single Title button in the reports tab. Creates a report for a single title that is
     * selected in the reports tab
     * @param event the event that triggered this method call
     */
    @FXML
    void handleExportSingleTitleFlaggedTable(ActionEvent event) {
        FlaggedTable flaggedTableTitle = flaggedTable.getSelectionModel().getSelectedItem();
        if (flaggedTableTitle == null) {
            Alert selectedAlert = new Alert(Alert.AlertType.INFORMATION, "Please select a title.", ButtonType.OK);
            selectedAlert.setTitle("Confirm Export");
            selectedAlert.setHeaderText("");
            selectedAlert.show();
        } else {
            Title title = new Title(flaggedTableTitle.getTitleId(), flaggedTableTitle.getFlaggedTitleName(), flaggedTableTitle.getFlaggedPriceCents(), "");

            LocalDate today = LocalDate.now();
            String fileName = title.getTitle() + " Requests " + today + ".xlsx";

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Location");
            fileChooser.setInitialFileName(fileName);
            File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());

            if (file != null) {
                file = addFileExtension(file);

                Workbook workbook = new XSSFWorkbook();

                Sheet sheet = workbook.createSheet(fileName);
                sheet.setColumnWidth(0, 6000);

                Row header = sheet.createRow(0);
                org.apache.poi.ss.usermodel.Cell headerCell = header.createCell(0);
                headerCell.setCellValue("Date: " + today);
                header = sheet.createRow(1);
                sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 2));
                headerCell = header.createCell(0);
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                headerCell.setCellStyle(cellStyle);
                headerCell.setCellValue("Single Title Customer List");

                // TODO: Add a log call?

                exportSingleTitle(workbook, title, 2, true, false);
                saveReport(file, workbook);
            }
        }
    }

    /**
     * Exports a list of all titles that have not been flagged in at least 6 months to an Excel spreadsheet
     * @param event the event that triggered this method call
     */
    @FXML
    void handleExportStalledTitles(ActionEvent event) {
        LocalDate today = LocalDate.now();
        String fileName = "Stalled Titles " + today + ".xlsx";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Location");
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());
        file = addFileExtension(file);

        if (file != null) {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet(fileName);
            sheet.setColumnWidth(0, 8000);
            sheet.setColumnWidth(1, 6000);

            Row header = sheet.createRow(0);

            org.apache.poi.ss.usermodel.Cell headerCell = header.createCell(0);
            headerCell.setCellValue("Date: " + today);
            header = sheet.createRow(1);
            sheet.addMergedRegion(new CellRangeAddress(1,1,0,1));
            headerCell = header.createCell(0);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCell.setCellStyle(cellStyle);
            headerCell.setCellValue("All Stalled Titles");

            sheet.setRepeatingRows(new CellRangeAddress(3,3,0,1));
            sheet.getHeader().setRight("Page &P of &N");

            Font bold = workbook.createFont();
            bold.setBold(true);

            CellStyle headStyle = workbook.createCellStyle();
            headStyle.setFont(bold);

            Row row = sheet.createRow(3);
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
            cell.setCellStyle(headStyle);
            cell.setCellValue("Title");

            cell = row.createCell(1);
            cell.setCellValue("Date Last Flagged");
            cell.setCellStyle(headStyle);

            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);

            ObservableList<Title> titles = titleTable.getItems();
            LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
            int i = 4;
            for (Title title : titles) {
                if (title.getDateFlagged() == null || title.getDateFlagged().isBefore(sixMonthsAgo)) {
                    row = sheet.createRow(i);

                    cell = row.createCell(0);
                    cell.setCellValue(title.getTitle());
                    cell.setCellStyle(wrapStyle);

                    cell = row.createCell(1);
                    if (title.getDateFlagged() != null) {
                        cell.setCellValue(title.getDateFlagged().toString());
                    } else {
                        cell.setCellValue("Never");
                    }
                    cell.setCellStyle(wrapStyle);

                    i++;
                }
            }

            Log.LogEvent("Export Stalled Titles", "Exporting all stalled titles");

            saveReport(file, workbook);
        }
    }

  
    /**
     * Sets the Flagged attribute of all Titles to false
     */
    @FXML
    void resetFlags() {
        Alert resetAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to reset all flags?" +
                " This cannot be undone.");
        resetAlert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> {
                    PreparedStatement s = null;
                    String sql = """
                                UPDATE Titles
                                SET FLAGGED = FALSE, ISSUE_FLAGGED = NULL
                                """;
                    try {
                        s = conn.prepareStatement(sql);
                        s.executeUpdate();
                        s.close();
                    } catch (SQLException sqlExcept) {
                        sqlExcept.printStackTrace();
                    }
                    titleTable.getItems().setAll(getTitles());
                    this.loadReportsTab();
                    getDatabaseInfo();
                });
        this.unsaved = false;

        Log.LogMessage("Flags Reset");
    }

    /**
     * Saves the current state and date of all New Release Flags to the database
     */
    @FXML
    void saveFlags() {

        ObservableList<Title> titles = titleTable.getItems();
        ZonedDateTime startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        long todayMillis = startOfToday.toEpochSecond() * 1000;
        Date today = new Date(todayMillis);

        Alert savingAlert = new Alert(Alert.AlertType.INFORMATION, "Saving New Release Flags...", ButtonType.OK);

        savingAlert.setTitle("Saving");
        savingAlert.setHeaderText("");
        savingAlert.setContentText("Saving New Release Flags...");
        savingAlert.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        savingAlert.getDialogPane().getScene().getWindow().setOnCloseRequest(Event::consume);
        savingAlert.show();

        for (int i = 0; i < titles.size(); i++) {
            PreparedStatement s = null;
            if (titles.get(i).isFlagged()) {
                String sql = """
                    UPDATE Titles
                    SET FLAGGED = TRUE, DATE_FLAGGED = ?, ISSUE_FLAGGED = ?
                    WHERE TITLEID = ?
                    """;
                try {
                    s = conn.prepareStatement(sql);
                    s.setString(1, DateFormat.getDateInstance().format(today));
                    if (titles.get(i).getIssueFlagged() == 0) {
                        s.setString(2, null);
                    } else {
                        s.setString(2, Integer.toString(titles.get(i).getIssueFlagged()));
                    }
                    s.setString(3, Integer.toString(titles.get(i).getId()));
                    s.executeUpdate();
                    s.close();
                } catch (SQLException sqlExcept) {
                    sqlExcept.printStackTrace();
                }
            }
            else {
                String sql = """
                    UPDATE Titles
                    SET FLAGGED = FALSE, ISSUE_FLAGGED = NULL
                    WHERE TITLEID = ?
                    """;
                try {
                    s = conn.prepareStatement(sql);
                    s.setString(1, Integer.toString(titles.get(i).getId()));
                    s.executeUpdate();
                    s.close();
                } catch (SQLException sqlExcept) {
                    sqlExcept.printStackTrace();
                }
            }

        }

        savingAlert.close();

        Alert savedAlert = new Alert(Alert.AlertType.INFORMATION, "Saved Flags!", ButtonType.OK);
        savedAlert.setHeaderText("");
        savedAlert.show();
        this.unsaved = false;
        titleTable.getItems().setAll(getTitles());
        this.loadReportsTab();
        getDatabaseInfo();

        Log.LogMessage("Flags Saved");
    }

    @FXML
    void handleCustomerKeyboardInput(KeyEvent event)
    {
        System.out.println("Customer keyboard input triggered: " + event.getCode().toString());

        if (event.isControlDown() && event.getCode() == KeyCode.F)
        {
            Scene scene = customerOrderTable.getScene();

            TextField search = (TextField) scene.lookup("#CustomerSearch");
            search.requestFocus();
        }
    }

    @FXML
    void passCustomerKeyboardFocus(MouseEvent event)
    {
        if (event.getEventType() == event.MOUSE_CLICKED)
        {
            customerOrderTable.getScene().lookup("#CustomerAnchorPane").requestFocus();
        }
    }

    @FXML
    void handleTitleKeyboardInput(KeyEvent event)
    {
        System.out.println("Title keyboard input triggered: " + event.getCode().toString());

        if (event.isControlDown() && event.getCode() == KeyCode.F)
        {
            Scene scene = titleTable.getScene();

            TextField search = (TextField) scene.lookup("#TitleSearch");
            search.requestFocus();
        }
        else if (event.isControlDown() && event.getCode() == KeyCode.M)
        {
            Scene scene = titleTable.getScene();

            titleTable.getSelectionModel().selectedItemProperty().getValue().setFlagged(!titleTable.getSelectionModel().selectedItemProperty().getValue().isFlagged());
        }
    }

    @FXML
    void passTitleKeyboardFocus(MouseEvent event)
    {
        if (event.getEventType() == event.MOUSE_CLICKED)
        {
            titleTable.getScene().lookup("#TitleAnchorPane").requestFocus();
        }
    }

    @FXML 
    void handleTitleSearchKeyboardInput(KeyEvent event)
    {
        Scene scene = titleTable.getScene();
        String search = ((TextField)scene.lookup("#TitleSearch")).getText().toLowerCase();

        if (search.equals("") || search == null)
        {
            titleTable.getItems().setAll(getTitles());
        }

        ObservableList<Title> titles = null;
        if (event.getCode() == KeyCode.BACK_SPACE)
        {
            titles = getTitles();
        }
        else 
        {
            titles = titleTable.getItems();
        }

        ObservableList<Title> sortedTitles = FXCollections.observableArrayList();

        for (Title title : titles) {
            if (title.getTitle().toLowerCase().contains(search))
            {
                sortedTitles.add(title);
            }
        }

        titleTable.getItems().setAll(sortedTitles);
    }

    @FXML 
    void handleCustomerSearchKeyboardInput(KeyEvent event)
    {
        Scene scene = customerTable.getScene();
        String search = ((TextField)scene.lookup("#CustomerSearch")).getText().toLowerCase();

        if (search.equals("") || search == null)
        {
            customerTable.getItems().setAll(getCustomers());
        }

        ObservableList<Customer> customers = null;
        if (event.getCode() == KeyCode.BACK_SPACE)
        {
            customers = getCustomers();
        }
        else 
        {
            customers = customerTable.getItems();
        }

        ObservableList<Customer> sortedCustomers = FXCollections.observableArrayList();

        for (Customer customer : customers) {
            if (customer.getFullName().toLowerCase().contains(search))
            {
                sortedCustomers.add(customer);
            }
        }

        customerTable.getItems().setAll(sortedCustomers);
    }

    //#endregion

/*######################################################################/
//////////////////////////// Custom Functions ///////////////////////////
/######################################################################*/
    
    //#region Custom Functions

    /**
     * Helper method to make the extension of a file .xlsx
     * @param file file to add the extension to
     * @return a child of the file with the new extension
     */
    private File addFileExtension(File file) {
        if (file != null) {
            int index = file.getName().lastIndexOf('.');
            if (index == -1) {
                file = new File(file.getParent(), file.getName() + ".xlsx");
            } else {
                file = new File(file.getParent(), file.getName().substring(0, index) + ".xlsx");
            }
        }
        return file;
    }

    /**
     * Creates a connection to the database and sets the global conn variable.
     */
    private void createConnection() {
        try {
            conn = DriverManager.getConnection("jdbc:derby:" + System.getProperty("user.home") + "/DragonSlayer/derbyDB");
        } catch (SQLException e) {
            if (e.getErrorCode() == 40000) {
                CreateDB.main(null);
                try {
                    conn = DriverManager.getConnection("jdbc:derby:" + System.getProperty("user.home") + "/DragonSlayer/derbyDB");
                } catch (SQLException se) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Could not create derby database. Please report this bug.", ButtonType.OK);
                    alert.setTitle("Database Error");
                    alert.setHeaderText("");
                    alert.show();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Database error. This is either a bug, or you messed with the DragonSlayer/derbyDB folder.", ButtonType.OK);
                alert.setTitle("Database Error");
                alert.setHeaderText("");
                alert.show();
            }
        }
    }

    /**
     * Helper method to get the customer requests for a single title and write them to an Excel spreadsheet. Will
     * skip all titles with no requests unless the force parameter is set to true
     * @param workbook the Excel workbook to write to
     * @param title the title to get information on
     * @param rowIndex the row of the spreadsheet to start on
     * @param force whether to force writing all titles with no requests or not
     * @return the index of the last row that was written to
     */
    private int exportSingleTitle(Workbook workbook, Title title, int rowIndex, boolean force, boolean flaggedReport) {
        String sql = String.format("""
                SELECT FIRSTNAME, LASTNAME, ISSUE, QUANTITY FROM ORDERS
                LEFT JOIN CUSTOMERS C on C.CUSTOMERID = ORDERS.CUSTOMERID
                WHERE TITLEID = %s
                ORDER BY LASTNAME
                """, title.getId());

        try {
            Statement s = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet result = s.executeQuery(sql);
            if (!result.last() && !force) {
                return rowIndex;
            }
            result.beforeFirst();

            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.createRow(rowIndex);
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
            cell.setCellValue("Title: " + title.getTitle());

            Font bold = workbook.createFont();
            bold.setBold(true);
            CellStyle headStyle = workbook.createCellStyle();
            headStyle.setFont(bold);
            headStyle.setAlignment(HorizontalAlignment.RIGHT);

            row = sheet.createRow(rowIndex + 1);
            cell = row.createCell(0);
            CellStyle customerHeadStyle = workbook.createCellStyle();
            customerHeadStyle.setFont(bold);
            cell.setCellStyle(customerHeadStyle);
            cell.setCellValue("Customer");

            cell = row.createCell(1);
            cell.setCellStyle(headStyle);
            cell.setCellValue("Issue");

            cell = row.createCell(2);
            cell.setCellStyle(headStyle);
            cell.setCellValue("Quantity");

            CellStyle reqItemStyle = workbook.createCellStyle();
            reqItemStyle.setWrapText(true);
            CellStyle rightAlign = workbook.createCellStyle();
            rightAlign.setAlignment(HorizontalAlignment.RIGHT);

            int i = rowIndex + 2;
            int totalQuantity = 0;
            while (result.next()) {
                String name = result.getString("LASTNAME") + " " + result.getString("FIRSTNAME");
                Object issue = result.getObject("ISSUE");
                int quantity = result.getInt("QUANTITY");
                if (issue != null && flaggedReport) {
                    int tempIssue = (int) issue;
                    if (tempIssue != title.getIssueFlagged()) {
                        continue;
                    }
                }

                row = sheet.createRow(i);

                cell = row.createCell(0);
                cell.setCellValue(name);

                cell = row.createCell(1);
                if (issue != null) {
                    cell.setCellValue(Integer.parseInt(issue.toString()));
                } else {
                    cell.setCellValue("All");
                }
                cell.setCellStyle(rightAlign);

                cell = row.createCell(2);
                cell.setCellValue(quantity);
                cell.setCellStyle(rightAlign);

                totalQuantity += quantity;
                i++;
            }
            result.close();
            s.close();

            row = sheet.createRow(i);
            cell = row.createCell(0);
            cell.setCellValue("Total:");
            cell = row.createCell(2);
            cell.setCellValue(totalQuantity);
            rowIndex = i + 2;

            Log.LogEvent("Export A Single Title Report", "Exported Title: " + title.getTitle());
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Database Error. Report may not have saved successfully", ButtonType.OK);
            alert.setTitle("Database Error");
            alert.show();
        }

        return rowIndex;
    }

    /**
     * Private helper method to make it easier to refresh the data
     * for the reports tab
     */
    private void loadReportsTab() {
        FlaggedTitlesTotalText.setText(Integer.toString(this.getNumTitlesCurrentlyFlagged()));
        FlaggedTitlesTotalCustomersText.setText(Integer.toString((this.getNumCustomers())));
        FlaggedIssueNumbersText.setText(Integer.toString(this.getNumFlaggedWithIssueNumbers()));
        FlaggedNoRequestsText.setText(Integer.toString(getNumTitlesNoRequests()));

        flaggedTable.getItems().setAll(this.getFlaggedTitles());
    }

    /**
     * Helper method to save a report
     * @param file The file to save
     * @param workbook the workbook to save
     */
    private void saveReport(File file, Workbook workbook) {
        Alert savingAlert = new Alert(Alert.AlertType.INFORMATION, "Saving Report", ButtonType.OK);
        try {
            savingAlert.setTitle("Saving");
            savingAlert.setHeaderText("");
            savingAlert.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
            savingAlert.getDialogPane().getScene().getWindow().setOnCloseRequest(Event::consume);
            savingAlert.show();

            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

            savingAlert.close();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Report saved successfully!", ButtonType.OK);
            alert.setTitle("File Saved");
            alert.setHeaderText("");
            alert.show();

            Log.LogEvent("Report Saved", "Saved a report to: " + file.getAbsolutePath());

        } catch (Exception e) {
            savingAlert.close();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error writing to file. Report may not have saved successfully. Make sure the file is not in use by another program.", ButtonType.OK);
            alert.setTitle("Save Error");
            alert.show();
        }
    }

    /**
     * Adds all orders for a given Customer to the Orders table.
     * @param customer The Customer to update the Order Table for
     */
    void updateOrdersTable(Customer customer){
        ObservableList<Order> allOrders = getOrderTable();
        ObservableList<Order> customerOrders = FXCollections.observableArrayList();
        for(int i=0; i < allOrders.size(); i++) {
            if (allOrders.get(i).getCustomerId() == customer.getId())
                customerOrders.add(allOrders.get(i));
        }
        customerOrderTable.getItems().setAll(customerOrders);
    }

    /**
     * Adds all orders for a given Title to the Title Orders table.
     * @param customer The Customer to update the Order Table for
     */
    // void getTitleOrders(Title title){
    //     ObservableList<RequestTable> = getRequests(title.getId(), -1);

    //     String titleName = title.getTitle();

    //     for(int i=0; i < allOrders.size(); i++) 
    //     {
    //         if (allOrders.get(i).getTitleName().equals(titleName))
    //         {
    //             for (Customer customer: customers)
    //             {
    //                 if (allOrders.get(i).getCustomerId() == customer.getId())
    //                     customersOrdering.add(allOrders.get(i));
    //             }
    //         }
    //     }
    //     customerOrderTable.getItems().setAll(customersOrdering);
    // }

    //#endregion

}