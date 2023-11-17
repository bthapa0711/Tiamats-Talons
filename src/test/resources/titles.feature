Feature: Titles
  Test functionality of titles tab
  Note: This feature is meant to be run with an empty database

  Scenario: Create, Edit, and Delete a Title
    When I click on Titles tab
    When I add titles
      | Title     | Product Id  | Price   | Notes     |
      | Superman  | 10          | 10      |           |
    Then I should see titles
      | Title     | Product Id  | Price   | Notes     |
      | Superman  | 10          | 10      |           |
    When I edit titles
      | Title       | Product Id  | Price   | Notes     |
      | Superman    | 10          | 20      |           |
    Then I should see titles
      | Title       | Product Id  | Price   | Notes     |
      | Superman    | 10          | 20      |           |
    When I delete titles
      | Title       | Product Id  | Price   | Notes     |
      | Superman    | 10          | 20      |           |
    Then I should see titles
      | Title     | Product Id  | Price   | Notes     |

  Scenario: Create, Edit, and Delete Multiple Titles
    When I click on Titles tab
    When I add titles
      | Title     | Product Id  | Price      | Notes     |
      | Superman  | 10          | 12.00      |           |
      | Batman    | 11          | 13.50      |           |
      | Joker     | 13          | 10.25      |           |
    Then I should see titles
      | Title     | Product Id  | Price      | Notes     |
      | Batman    | 11          | 13.50      |           |
      | Joker     | 13          | 10.25      |           |
      | Superman  | 10          | 12.00      |           |
    When I edit titles
      | Title       | Product Id  | Price   | Notes     |
      | Batman      | 11          | 16      |           |
      | Joker       | 13          | 9       |           |
    Then I should see titles
      | Title       | Product Id  | Price   | Notes     |
      | Batman      | 11          | 16      |           |
      | Joker       | 13          | 9       |           |
      | Superman    | 10          | 12.00   |           |
    When I delete titles
      | Title     | Product Id  | Price   | Notes     |
      | Superman  | 10          | 12      |           |
      | Batman    | 11          | 16      |           |
      | Joker     | 13          | 9       |           |
    Then I should see titles
      | Title     | Product Id  | Price   | Notes     |

  Scenario: Creating Duplicate Titles
    When I click on Titles tab
    When I add titles
      | Title     | Product Id  | Price   | Notes     |
      | Superman  | 10          | 12      |           |
      | Superman  | 10          | 12      |           |
    Then I should see and close message: "Duplicate Title Entry"
    When I update add title with title: "Superman IV"
    Then I should see titles
      | Title           | Product Id  | Price   | Notes     |
      | Superman        | 10          | 12      |           |
      | Superman IV     | 10          | 12      |           |
    When I delete titles
      | Title           | Product Id  | Price   | Notes     |
      | Superman        | 10          | 12      |           |
      | Superman IV     | 10          | 12      |           |
    Then I should see titles
      | Title           | Product Id  | Price   | Notes     |

  Scenario: Search for Titles
    When I click on Titles tab
    When I add titles
      | Title     | Product Id  | Price   | Notes     |
      | Superman  | 10          | 12      |           |
      | Batman    | 11          | 13      |           |
      | Joker     | 13          | 10      |           |
    Then I should see titles
      | Title     | Product Id  | Price   | Notes     |
      | Batman    | 11          | 13      |           |
      | Joker     | 13          | 10      |           |
      | Superman  | 10          | 12      |           |
    When I search titles: "Jo"
    Then I should only see titles
      | Title     | Product Id  | Price   | Notes     |
      | Joker     | 13          | 10      |           |
    When I delete titles
      | Title     | Product Id  | Price   | Notes     |
      | Joker     | 13          | 10      |           |
    Then I should see titles
      | Title     | Product Id  | Price   | Notes     |
      | Batman    | 11          | 13      |           |
      | Superman  | 10          | 12      |           |
    When I delete titles
      | Title     | Product Id  | Price   | Notes     |
      | Superman  | 10          | 12      |           |
      | Batman    | 11          | 13      |           |
    Then I should see titles
      | Title     | Product Id  | Price   | Notes     |

  Scenario: Create Invalid Titles
    When I click on Titles tab
    When I add titles
      | Title     | Product Id  | Price   | Notes     |
      | Superman  | 10          | 10      |           |
    Then I should see titles
      | Title     | Product Id  | Price   | Notes     |
      | Superman  | 10          | 10      |           |
    When I add titles
      | Title     | Product Id  | Price   | Notes     |
      |           |             | 14      |           |
    Then I should see and close message: "No title entered"
    When I update add title with title: "Catwoman"
    Then I should see titles
      | Title       | Product Id  | Price   | Notes     |
      | Catwoman    |             | 14      |           |
      | Superman    | 10          | 10      |           |
    When I delete titles
      | Title       | Product Id  | Price   | Notes     |
      | Superman    | 10          | 10      |           |
      | Catwoman    |             | 14      |           |
    Then I should see titles
      | Title     | Product Id  | Price   | Notes     |

  Scenario: Create and Release Save Flags
    When I click on Titles tab
    When I add titles
      | Title     | Product Id  | Price       | Notes     |
      | Superman  | 10          | 12.10       |           |
      | Batman    | 11          | 13.50       |           |
      | Joker     | 13          | 10.00       |           |
    Then I should see titles
      | Title     | Product Id  | Price       | Notes     |
      | Batman    | 11          | 13.50       |           |
      | Joker     | 13          | 10.00       |           |
      | Superman  | 10          | 12.10       |           |
    When I flag title: "Batman"
    Then I should see and close message: "Message"
    Then I should see title is flagged: "Batman"
    When I click release flags
    Then I should see and close message: "Confirmation"
    Then I should see title is not flagged: "Batman"
    When I delete titles
      | Title     | Product Id  | Price       | Notes     |
      | Superman  | 10          | 12.10       |           |
      | Batman    | 11          | 13.50       |           |
      | Joker     | 13          | 10.00       |           |
    Then I should see titles
      | Title     | Product Id  | Price   | Notes     |

  Scenario: Sorting Titles Incasesensitive
    When I click on Titles tab
    When I add titles
      | Title     | Product Id  | Price   | Notes     |
      | AB        | 1           | 10      |           |
      | bA        | 3           | 10      |           |
      | ac        | 2           | 10      |           |
      | bC        | 5           | 10      |           |
      | BB        | 4           | 10      |           |
    Then I should see titles
      | Title     | Product Id  | Price   | Notes     |
      | AB        | 1           | 10      |           |
      | ac        | 2           | 10      |           |
      | bA        | 3           | 10      |           |
      | BB        | 4           | 10      |           |
      | bC        | 5           | 10      |           |
    When I add titles
      | Title     | Product Id  | Price   | Notes     |
      | aa        | 0           | 10      |           |
    Then I should see titles
      | Title     | Product Id  | Price   | Notes     |
      | aa        | 0           | 10      |           |
      | AB        | 1           | 10      |           |
      | ac        | 2           | 10      |           |
      | bA        | 3           | 10      |           |
      | BB        | 4           | 10      |           |
      | bC        | 5           | 10      |           |
    When I delete titles
      | Title     | Product Id  | Price   | Notes     |
      | aa        | 0           | 10      |           |
      | AB        | 1           | 10      |           |
      | ac        | 2           | 10      |           |
      | bA        | 3           | 10      |           |
      | BB        | 4           | 10      |           |
      | bC        | 5           | 10      |           |
    Then I should see titles
      | Title     | Product Id  | Price   | Notes     |

  Scenario: Search by Product Id
    When I click on Titles tab
    When I add titles
      | Title     | Product Id  | Price      | Notes     |
      | Superman  | 10          | 12.00      |           |
      | Batman    | 11          | 13.50      |           |
      | Joker     | 13          | 10.25      |           |
    Then I should see titles
      | Title     | Product Id  | Price      | Notes     |
      | Batman    | 11          | 13.50      |           |
      | Joker     | 13          | 10.25      |           |
      | Superman  | 10          | 12.00      |           |
    When I search titles: "11"
    Then I should only see titles
      | Title     | Product Id  | Price      | Notes     |
      | Batman    | 11          | 13.50      |           |
    When I delete titles
      | Title     | Product Id  | Price      | Notes     |
      | Batman    | 11          | 13.50      |           |
    Then I should see titles
      | Title     | Product Id  | Price   | Notes     |
      | Joker     | 13          | 10.25      |           |
      | Superman  | 10          | 12.00      |           |
    When I delete titles
      | Title     | Product Id  | Price      | Notes     |
      | Joker     | 13          | 10.25      |           |
      | Superman  | 10          | 12.00      |           |
    Then I should see titles
      | Title     | Product Id  | Price   | Notes     |
