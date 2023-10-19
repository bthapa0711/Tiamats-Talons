import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.testfx.api.FxRobotException;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StepDefinitions extends ApplicationTest {
    private Controller controller;

    @Before
    public void setUp () throws Exception {
        ApplicationTest.launch(Main.class);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
        fxmlLoader.load();
        controller = fxmlLoader.getController();
        controller.emptyDB();
    }

    @After
    public void tearDown () throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    //#region Customer Functions

    @Given("Customers$")
    public void customers(DataTable args) throws SQLException
    {
        ArrayList<Customer> customers = (ArrayList<Customer>) createCustomerList(args);
        controller.addCustomers(customers);
    }

    //#region Customer Buttons and Inputs
    @When("I click on Customers tab")
    public void i_click_on_customers_tab() throws InterruptedException
    {
        clickOn("Customers");
        TimeUnit.MILLISECONDS.sleep(20);
    }

    @When("I add customers$")
    public void i_add_customers(DataTable args) throws Throwable
    {
        List<Customer> customers = createCustomerList(args);

        for(Customer c : customers)
        {
            clickOn("#addCustomerButtonMain");
            clickOn("#newCustomerFirstName");
            write(c.getFirstName());
            clickOn("#newCustomerLastName");
            write(c.getLastName());
            clickOn("#newCustomerPhone");
            write(c.getPhone());
            clickOn("#newCustomerEmail");
            write(c.getEmail());
            clickOn("#newCustomerNotes");
            write(c.getNotes());
            clickOn("#addCustomerButton");
        }
    }

    @When("I update add customer with phone: {string}")
    public void i_update_add_customer_with_phone(String phoneNumber)
    {
        clickOn("#newCustomerPhone");
        write(phoneNumber);
        clickOn("#addCustomerButton");
    }

    @When("I update add customer with full name: {string}")
    public void i_update_add_customer_with_full_name(String fullname)
    {
        String[] name = fullname.split(" ");
        clickOn("#newCustomerFirstName");
        write(name[0]);
        clickOn("#newCustomerLastName");
        write(name[1]);
        clickOn("#addCustomerButton");
    }

    @When("I edit customers$")
    public void i_edit_customers(DataTable args)
    {
        List<Customer> customers = createCustomerList(args);

        for(Customer c : customers)
        {
            clickOn(c.getLastName());
            clickOn("#editCustomerButton");
            doubleClickOn("#updateCustomerFirstName");
            write(c.getFirstName());
            doubleClickOn("#updateCustomerLastName");
            write(c.getLastName());
            doubleClickOn("#updateCustomerPhone");
            clickOn("#updateCustomerPhone");
            write(c.getPhone());
            doubleClickOn("#updateCustomerEmail");
            clickOn("#updateCustomerEmail");
            write(c.getEmail());
            doubleClickOn("#updateCustomerNotes");
            write(c.getNotes());
            clickOn("#updateCustomerButton");
        }
    }

    @When("I delete customers$")
    public void i_delete_customers(DataTable args) throws Throwable
    {
        List<Customer> customers = createCustomerList(args);

        for(Customer c : customers)
        {
            clickOn(c.getLastName());
            clickOn("#deleteCustomerButton");
            clickOn("#yesButton");
        }
    }

    @When("I search customers: {string}")
    public void i_search(String search)
    {
        clickOn("#CustomerSearch");
        write(search);
    }

    @When("I mark customer delinquent: {string}")
    public void i_mark_customer_delinquent(String customer)
    {
        clickOn(customer);
        clickOn("#Delinq");
    }

    //#endregion

    //#region Customer Data Validation
    @Then("I should see customers$")
    public void i_should_see_customers(DataTable args) throws Throwable
    {
        List<Customer> customers = createCustomerList(args);
        List<Customer> actualCustomers = controller.getCustomerList();

        // Check for same number of customers
        assertEquals(customers.size(), actualCustomers.size());
        // Check customers were saved correctly in DB
        for (int i = 0; i < customers.size(); i++)
        {
            assertTrue(customers.get(i).equals(actualCustomers.get(i)));
        }
        // Check each of the customers shows on the application
        for (Customer c : customers)
        {
            clickOn(c.getLastName());
        }
    }

    @Then("I should see and close message: {string}")
    public void i_should_see_and_close_message(String message)
    {
        List<Window> x = robotContext().getWindowFinder().listWindows();
        int pos = robotContext().getWindowFinder().listWindows().size();
        Window duplicate = robotContext().getWindowFinder().listWindows().get(pos - 1);
        Stage s = (Stage) duplicate;
        assertEquals(message, s.getTitle());
        clickOn("OK");
    }

    @Then("I should only see customers$")
    public void i_should_only_see_customers(DataTable args)
    {
        List<Customer> customers = createCustomerList(args);
        List<Customer> totalCustomers = controller.getCustomerList();

        // Check expected result shows on application
        for(Customer c : customers) {
            clickOn(c.getLastName());
        }

        // Check other customers are not shown
        for (Customer c : totalCustomers)
        {
            if (!customers.contains(c))
            {
                try {
                    clickOn(c.getLastName());
                }
                catch (FxRobotException e)
                {
                    assertEquals("the query \"" + c.getLastName() + "\" returned no nodes.", e.getMessage());
                }
            }
        }
    }

    @Then("I should see delinquent on customer: {string}")
    public void i_should_see_delinquent_on_customer(String customer)
    {
        clickOn(customer);
        clickOn("#delinqNoticeText");
    }

    @Then("I should not see delinquent on customer: {string}")
    public void i_should_not_see_delinquent_on_customer(String customer)
    {
        clickOn(customer);
        try {
            clickOn("#delinqNoticeText");
        }
        catch (FxRobotException e)
        {
            assertEquals("the query \"#delinqNoticeText\" returned 1 nodes, but no nodes were visible.", e.getMessage());
        }
    }

    //#endregion

    //#endregion

    //#region Title Functions

    @Given("Titles$")
    public void titles(DataTable args) throws SQLException
    {
        ArrayList<Title> titles = (ArrayList<Title>) createTitleList(args);
        controller.addTitles(titles);
    }

    //#region Title Buttons and Inputs

    @When("I click on Titles tab")
    public void i_click_on_titles_tab()
    {
        clickOn("Titles");
    }

    @When("I add titles$")
    public void i_add_titles(DataTable args)
    {
        List<Title> titles = createTitleList(args);

        for (Title t : titles)
        {
            clickOn("#addTitleButtonMain");

            clickOn("#newTitleTitle");
            write(t.getTitle());
            clickOn("#newTitleProductId");
            write(t.getProductId());
            clickOn("#newTitlePrice");
            write(t.getPriceDollars());
            clickOn("#newTitleNotes");
            write(t.getNotes());

            clickOn("#addTitleButton");
        }
    }

    @When("I edit titles$")
    public void i_edit_titles(DataTable args)
    {
        List<Title> titles = createTitleList(args);

        for(Title t : titles)
        {
            clickOn(t.getTitle());
            clickOn("#editTitleButton");

            doubleClickOn("#updateTitleTitle");
            write(t.getTitle());
            doubleClickOn("#updateTitleProductId");
            write(t.getProductId());
            doubleClickOn("#updateTitlePrice");
            write(t.getPriceDollars());
            doubleClickOn("#updateTitleNotes");
            write(t.getNotes());

            clickOn("#updateTitleButton");
        }
    }

    @When("I delete titles$")
    public void i_delete_titles(DataTable args)
    {
        List<Title> titles = createTitleList(args);

        for(Title t : titles)
        {
            clickOn(t.getTitle());
            clickOn("#deleteTitleButton");
            clickOn("#yesButton");
        }
    }

    @When("I search titles: {string}")
    public void i_search_titles(String title)
    {
        clickOn("#TitleSearch");
        write(title);
        write('\n');
    }

    @When("I update add title with title: {string}")
    public void i_update_add_title_with_title(String title)
    {
        doubleClickOn("#newTitleTitle");
        write(title);
        clickOn("#addTitleButton");
    }

    @When("I flag title: {string}")
    public void i_flag_title(String title)
    {
        i_search_titles(title);
        for (int i = 0; i < 6; i++)
        {
            press(KeyCode.TAB);
            release(KeyCode.TAB);
        }
        press(KeyCode.ENTER);
        release(KeyCode.ENTER);
        push(new KeyCodeCombination(KeyCode.M, KeyCodeCombination.CONTROL_DOWN));
        clickOn("#saveFlagsButton");
    }

    @When("I click release flags")
    public void i_click_release_flags()
    {
        clickOn("#resetFlagsButton");
    }
    //#endregion

    //#region Titles Data Validation
    @Then("I should see titles$")
    public void i_should_see_titles(DataTable args)
    {
        List<Title> titles = createTitleList(args);
        List<Title> actualTitles = controller.getTitlesList();

        // Check for same number of customers
        assertEquals(titles.size(), actualTitles.size());
        // Check customers were saved correctly in DB
        for (int i = 0; i < titles.size(); i++)
        {
            assertTrue(titles.get(i).equals(actualTitles.get(i)));
        }
        // Check each of the customers shows on the application
        for (Title t : titles)
        {
            clickOn(t.getTitle());
        }
    }

    @Then("I should only see titles$")
    public void i_should_only_see_titles(DataTable args)
    {
        List<Title> titles = createTitleList(args);
        List<Title> totalTitles = controller.getTitlesList();

        // Check expected result shows on application
        for (Title t : titles)
        {
            clickOn(t.getTitle());
        }

        // Check other titles are not shown
        for (Title t : totalTitles)
        {
            if (!titles.contains(t))
            {
                try {
                    clickOn(t.getTitle());
                }
                catch (FxRobotException e)
                {
                    assertEquals("the query \"" + t.getTitle() + "\" returned no nodes.", e.getMessage());
                }
            }
        }
    }

    @Then("I should see title is flagged: {string}")
    public void i_should_see_title_is_flagged(String title)
    {
        List<Title> flagged = controller.getFlaggedList();
        assertTrue(flagged.stream().filter(t -> t.getTitle().equals(title)).count() > 0);
    }

    @Then("I should see title is not flagged: {string}")
    public void i_should_see_title_is_not_flagged(String title)
    {
        List<Title> flagged = controller.getFlaggedList();
        assertEquals(flagged.stream().filter(t -> t.getTitle().equals(title)).count(), 0);
    }
    //#endregion

    //#endregion

    //#region Order Functions

    @Given("Request Table for title: {string}")
    public void request_table_for_title(String title, DataTable args) throws SQLException
    {
        ArrayList<RequestTable> requestTables = createRequestTable(args);
        controller.addOrders(title, requestTables);
    }

    //#region Order Buttons
    @When("I add requests for last name: {string}")
    public void i_add_requests_for_last_name(String customer, DataTable args)
    {
        clickOn(customer);
        List<Order> orders = createOrderList(args);
        for (Order o : orders)
        {
            clickOn("#newOrderButton");
            write(o.getTitleName());
            press(KeyCode.TAB);
            release(KeyCode.TAB);
            write(String.valueOf(o.getIssue()));
            press(KeyCode.TAB);
            release(KeyCode.TAB);
            write(String.valueOf(o.getQuantity()));
            clickOn("#addOrderButton");
        }
    }

    @When("I edit requests for last name: {string}")
    public void i_edit_requests_for_last_name(String customer, DataTable args)
    {
        clickOn(customer);
        List<Order> orders = createOrderList(args);
        for (Order o : orders)
        {
            clickOn("#editOrderButton");
            write(o.getTitleName());
            press(KeyCode.TAB);
            release(KeyCode.TAB);
            write(String.valueOf(o.getIssue()));
            press(KeyCode.TAB);
            release(KeyCode.TAB);
            write(String.valueOf(o.getQuantity()));
            clickOn("#updateOrderButton");
        }
    }

    @When("I delete requests for last name: {string}")
    public void i_delete_requests_for_last_name(String customer, DataTable args)
    {
        // Currently deletes the number of orders as it cant choose specific titles
        clickOn(customer);
        List<Order> orders = createOrderList(args);
        for (Order o : orders)
        {
            clickOn("#customerOrderReqItemsColumn");
            press(KeyCode.UP);
            release(KeyCode.UP);
            clickOn("#deleteOrderButton");
            clickOn("#yesButton");
        }
    }
    //#endregion

    //#region Order Validation
    @Then("I should see orders for last name: {string}")
    public void i_should_see_orders_for_last_name(String customer, DataTable args)
    {
        clickOn(customer);
        List<Order> orders = createOrderList(args);
        List<Order> actualOrders = controller.getOrderListForCustomer(customer);

        // Check orders were saved properly in database
        assertEquals(orders.size(), actualOrders.size());
        for (int i = 0; i < orders.size(); i++)
        {
            Order o1 = orders.get(i);
            Order o2 = actualOrders.get(i);
            assertEquals(o1.getTitleName(), o2.getTitleName());
            assertEquals(o1.getIssue(), o2.getIssue());
            assertEquals(o1.getQuantity(), o2.getQuantity());
        }

        // Check that orders show on application
        for (Order o : orders)
        {
            clickOn(o.getTitleName());
        }
    }

    @Then("I should see orders for title: {string}")
    public void i_should_see_orders_for_title(String title, DataTable args)
    {
        clickOn(title);
        List<RequestTable> orders = createRequestTable(args);
        List<RequestTable> actualOrders = controller.getOrderListForTitle(title);

        // Check orders were saved properly in database
        assertEquals(orders.size(), actualOrders.size());
        for (int i = 0; i < orders.size(); i++)
        {
            orders.get(i).equals(actualOrders.get(i));
        }

        // Check that orders show on application
        for (RequestTable o : orders)
        {
            clickOn(o.getRequestLastName());
        }
    }
    //#endredgion
    //#endregion

    //#endregion

    //#region Report Functions
    @When("I click on the Reports tab")
    public void i_click_on_the_reports_tab() throws InterruptedException {
        clickOn("Reports");
        TimeUnit.MILLISECONDS.sleep(10);
    }

    @When("I click on the New Week Pulls tab")
    public void i_click_on_the_new_week_pulls_tab()
    {
        clickOn("New Week Pulls");
    }

    @When("I click on the Monthly Breakdown tab")
    public void i_click_on_the_monthly_breakdown_tab()
    {
        clickOn("Monthly Breakdown");
    }

    @Then("I should see New Week Pulls$")
    public void i_should_see_new_week_pulls(DataTable args)
    {
        List<String> columns = args.row(1);
        String flaggedTitlesTotal = columns.get(0);
        String flaggedTitlesTotalCustomers = columns.get(1);
        String flaggedIssueNumbers = columns.get(2);
        String flaggedNoRequests = columns.get(3);

        String actualFlaggedTitlesTotal  = lookup("#FlaggedTitlesTotalText").queryText().getText();
        String actualFlaggedTotalCustomers = lookup("#FlaggedTitlesTotalCustomersText").queryText().getText();
        String actualFlaggedIssueNumbers = lookup("#FlaggedIssueNumbersText").queryText().getText();
        String actualFlaggedNoRequests = lookup("#FlaggedNoRequestsText").queryText().getText();

        assertEquals(flaggedTitlesTotal, actualFlaggedTitlesTotal);
        assertEquals(flaggedTitlesTotalCustomers, actualFlaggedTotalCustomers);
        assertEquals(flaggedIssueNumbers, actualFlaggedIssueNumbers);
        assertEquals(flaggedNoRequests, actualFlaggedNoRequests);
    }

    @Then("I should see Monthly Breakdown$")
    public void i_should_see_monthly_breakdown(DataTable args)
    {
        List<String> columns = args.row(1);
        String numTitles = columns.get(0);
        String numCustomers = columns.get(1);
        String specialOrderNotes = columns.get(2);
        String issueNumberRequests = columns.get(3);
        String titlesNotFlagged = columns.get(4);
        String titlesNoRequests = columns.get(5);
        String breakdown = String.format("""
                Database currently has:
                   %s Titles
                   %s Customers
                   %s Special Order Notes
                   %s Pending Issue # Requests
                   %s Titles have not been flagged for over six months
                   %s Titles have 0 Customer Requests
                """, numTitles, numCustomers, specialOrderNotes, issueNumberRequests, titlesNotFlagged, titlesNoRequests);
        breakdown = "[" + breakdown.replaceAll("[\\t\\n\\r]+",", ") + "]";

        // Currently formatting between exp and act are wrong, one uses new line other commas plus surrounding bracketsg
        TextArea  text = (TextArea) lookup("#databaseOverview").query().accessibleTextProperty().getBean();
        String actualBreakdown = text.getParagraphs().toString();
        assertEquals(breakdown, actualBreakdown);
    }
    //#endregion

    private ArrayList<Customer> createCustomerList(DataTable t)
    {
        ArrayList<Customer> customers = new ArrayList<>();
        List<List<String>> rows = t.asLists(String.class);

        for (List<String> columns : rows.subList(1, rows.size())) {
            customers.add(
                    new Customer(Objects.toString(columns.get(0), ""),
                            Objects.toString(columns.get(1), ""),
                            Objects.toString(columns.get(2), ""),
                            Objects.toString(columns.get(3), ""),
                            Objects.toString(columns.get(4), ""), false));
        }
        return customers;
    }

    private ArrayList<Title> createTitleList(DataTable t)
    {
        ArrayList<Title> titles = new ArrayList<>();
        List<List<String>> rows = t.asLists(String.class);
        for (List<String> columns : rows.subList(1, rows.size())) {
            String title = Objects.toString(columns.get(0), "");
            int price = Integer.parseInt(dollarsToCents(columns.get(2)));
            String notes = Objects.toString(columns.get(3), "");
            String productId = Objects.toString(columns.get(1), "");
            int id = 0;
            LocalDate date = LocalDate.now();
            if (columns.size() >= 6)
            {
                id = Integer.parseInt(columns.get(4));
                date = columns.get(5) == null ? null : LocalDate.parse(columns.get(5), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            }
            titles.add(
                    new Title(id, title, price, notes, productId, date));
        }
        return titles;
    }

    private ArrayList<Order> createOrderList(DataTable t)
    {
        ArrayList<Order> orders = new ArrayList<>();
        List<List<String>> rows = t.asLists(String.class);
        for (List<String> columns : rows.subList(1, rows.size())) {
            String title = Objects.toString(columns.get(0), "");
            int quantity = columns.get(1) == null ? 0 : Integer.parseInt(columns.get(1));
            int issue = columns.get(2) == null ? 0 : Integer.parseInt(columns.get(2));
            int customerId = 1;
            int titleId = 1;
            if (columns.size() == 5)
            {
                customerId = Integer.parseInt(columns.get(3));
                titleId = Integer.parseInt(columns.get(4));
            }
            orders.add(
                    new Order(
                            customerId,
                            titleId,
                            title,
                            quantity,
                            issue
                    ));
        }
        return orders;
    }

    private ArrayList<RequestTable> createRequestTable(DataTable t)
    {
        ArrayList<RequestTable> requestTables = new ArrayList<>();
        List<List<String>> rows = t.asLists(String.class);
        for (List<String> columns : rows.subList(1, rows.size())) {
            requestTables.add(
                    new RequestTable(
                            Objects.toString(columns.get(0), ""),
                            Objects.toString(columns.get(1), ""),
                            columns.get(2) == null ? 0 : Integer.parseInt(columns.get(2)),
                            columns.get(3) == null ? 0 : Integer.parseInt(columns.get(3))
                    ));
        }
        return requestTables;
    }

    private String dollarsToCents(String priceDollars) {
        if (priceDollars == "") {
            return null;
        }
        priceDollars = priceDollars.replace(".", "");
        priceDollars = priceDollars.replaceAll(",", "");
        return priceDollars;
    }
}
