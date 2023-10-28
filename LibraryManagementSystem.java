import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.Arrays;

class Book {
    String author;
    int year;
    String title;
    double itemPrice;
    int popularity;

    public Book(String t, String a, int y, double p) {
        title = t;
        author = a;
        year = y;
        itemPrice = p;
    }

    public void setTitle(String t) {
        this.title = t;
    }

    public void setAuthor(String a) {
        this.author = a;
    }

    public void setYear(int y) {
        this.year = y;
    }

    public void setPopularity(int p) {
        popularity = p;
    }

    public double getPrice() {
        return itemPrice;
    }

    public void setPrice(double p) {
        this.itemPrice = p;
    }

    public double calculatePrice() {
        return (itemPrice + (itemPrice * 20) / 100 + 200);
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getYear() {
        return year;
    }
}

class Library {
    ArrayList<Book> books = new ArrayList<>();

    public void readDataFromFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 6) {
                    System.out.println("Invalid data format: " + line);
                    continue;
                }

                int itemType = Integer.parseInt(parts[0]);
                String itemTitle = parts[1];
                String[] itemAuthorsOrCompany = Arrays.copyOfRange(parts, 2, parts.length - 3);
                int itemYearOrPublicationDate;
                int itemPopularityCount;
                int itemPrice;

                switch (itemType) {
                    case 1: // Book
                        if (parts.length != 6) {
                            System.out.println("Invalid book data format: " + line);
                            continue;
                        }
                        itemYearOrPublicationDate = Integer.parseInt(parts[3]);
                        itemPopularityCount = Integer.parseInt(parts[4]);
                        itemPrice = Integer.parseInt(parts[5]);
                        Book book = new Book(itemTitle, String.join(" ", itemAuthorsOrCompany), itemYearOrPublicationDate, itemPrice);
                        book.setPopularity(itemPopularityCount);
                        addBook(book);
                        break;
                    default:
                        System.out.println("Invalid bookstype: " + itemType);
                        break;
                }
            }
            System.out.println("Data from " + fileName + " has been successfully loaded into the library.");
        } catch (IOException e) {
            System.out.println("An error occurred while reading the file: " + e.getMessage());
        }
    }

    
    public Book findBookByTitle(String title) {
        for (Book book : books) {
            if (book.getTitle().equals(title)) {
                return book;
            }
        }
        return null;
    }

    public ArrayList<Book> getBooks() {
        return books;
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public void deleteBook(String title) {
        Book book = findBookByTitle(title);
        if (book != null) {
            books.remove(book);
        }
    }

    public void editBook(String title, Book editedBook) {
        Book book = findBookByTitle(title);
        if (book != null) {
            book.setTitle(editedBook.getTitle());
            book.setAuthor(editedBook.getAuthor());
            book.setYear(editedBook.getYear());
            book.setPrice(editedBook.getPrice());
        }
    }
}

public class LibraryManagementSystem {
    private DefaultTableModel tableModel;
    private JTable table;
    private JFrame bookContentFrame;
    private JTextPane textPane;
    private String currentBookTitle;
    private boolean bookContentChanged;
    private JFrame mainFrame;
    private Library library = new Library();

    public LibraryManagementSystem() {
        mainFrame = new JFrame("Library Management System");

        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                askToCloseMainWindow();
            }
        });

        mainFrame.setSize(600, 400);

        String[] columnNames = {"Title", "Author", "Publication Year", "Price", "Read Item"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Allow editing only for the "Read Item" column
            }
        };
        table = new JTable(tableModel);
        table.getColumn("Read Item").setCellRenderer(new ButtonRenderer());
        table.getColumn("Read Item").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane tableScrollPane = new JScrollPane(table);

        JPanel tablePanel = new JPanel();
        tablePanel.add(tableScrollPane);

        JPanel buttonPanel = new JPanel();

        // Add, Edit, and Delete buttons
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewBook();
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editSelectedBook();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedBook();
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(tablePanel, BorderLayout.CENTER);
        mainFrame.add(buttonPanel, BorderLayout.SOUTH);

        mainFrame.setVisible(true);

        // Read data from the file and populate the library
        library.readDataFromFile("data.txt");

        // Populate the table with books from the library
        for (Book book : library.getBooks()) {
            String[] data = {book.getTitle(), book.getAuthor(), String.valueOf(book.getYear()), String.valueOf(book.getPrice()), "Read"};
            tableModel.addRow(data);
        }
    }

    private void addNewBook() {
        // Create a new JFrame for adding a book
        JFrame addBookFrame = new JFrame("Add Book");
        addBookFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addBookFrame.setSize(400, 200);

        JPanel addBookPanel = new JPanel();
        addBookPanel.setLayout(new GridLayout(4, 2));

        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField yearField = new JTextField();
        JTextField priceField = new JTextField();

        addBookPanel.add(new JLabel("Title:"));
        addBookPanel.add(titleField);
        addBookPanel.add(new JLabel("Author:"));
        addBookPanel.add(authorField);
        addBookPanel.add(new JLabel("Publication Year:"));
        addBookPanel.add(yearField);
        addBookPanel.add(new JLabel("Price:"));
        addBookPanel.add(priceField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = titleField.getText();
                String author = authorField.getText();
                int year = Integer.parseInt(yearField.getText());
                double price = Double.parseDouble(priceField.getText());

                Book newBook = new Book(title, author, year, price);
                library.addBook(newBook);

                // Update the table with the new book
                String[] rowData = {title, author, String.valueOf(year), String.valueOf(price), "Read"};
                tableModel.addRow(rowData);

                // Write the new book to the data.txt file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("data.txt", true))) {
                    writer.write("1," + title + "," + author + "," + year + ",0," + price);
                    writer.newLine();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                addBookFrame.dispose();
            }
        });

        addBookPanel.add(saveButton);

        addBookFrame.add(addBookPanel);
        addBookFrame.setVisible(true);
    }

    private void editSelectedBook() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String title = (String) tableModel.getValueAt(selectedRow, 0);
            Book book = library.findBookByTitle(title);
            if (book != null) {
                JFrame editBookFrame = new JFrame("Edit Book");
                editBookFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                editBookFrame.setSize(400, 200);

                JPanel editBookPanel = new JPanel();
                editBookPanel.setLayout(new GridLayout(4, 2));

                JTextField titleField = new JTextField(book.getTitle());
                JTextField authorField = new JTextField(book.getAuthor());
                JTextField yearField = new JTextField(String.valueOf(book.getYear()));
                JTextField priceField = new JTextField(String.valueOf(book.getPrice()));

                editBookPanel.add(new JLabel("Title:"));
                editBookPanel.add(titleField);
                editBookPanel.add(new JLabel("Author:"));
                editBookPanel.add(authorField);
                editBookPanel.add(new JLabel("Publication Year:"));
                editBookPanel.add(yearField);
                editBookPanel.add(new JLabel("Price:"));
                editBookPanel.add(priceField);

                JButton saveButton = new JButton("Save");
                saveButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String newTitle = titleField.getText();
                        String newAuthor = authorField.getText();
                        int newYear = Integer.parseInt(yearField.getText());
                        double newPrice = Double.parseDouble(priceField.getText());

                        Book editedBook = new Book(newTitle, newAuthor, newYear, newPrice);
                        library.editBook(title, editedBook);

                        // Update the table with the edited book
                        tableModel.setValueAt(newTitle, selectedRow, 0);
                        tableModel.setValueAt(newAuthor, selectedRow, 1);
                        tableModel.setValueAt(newYear, selectedRow, 2);
                        tableModel.setValueAt(newPrice, selectedRow, 3);

                        // Update the book in the data.txt file
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data.txt"))) {
                            for (Book b : library.getBooks()) {
                                writer.write("1," + b.getTitle() + "," + b.getAuthor() + "," + b.getYear() + ",0," + b.getPrice());
                                writer.newLine();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                        editBookFrame.dispose();
                    }
                });

                editBookPanel.add(saveButton);

                editBookFrame.add(editBookPanel);
                editBookFrame.setVisible(true);
            }
        }
    }

    private void deleteSelectedBook() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String title = (String) tableModel.getValueAt(selectedRow, 0);
            library.deleteBook(title);
            tableModel.removeRow(selectedRow);

            // Update the data.txt file after deleting the book
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("data.txt"))) {
                for (Book b : library.getBooks()) {
                    writer.write("1," + b.getTitle() + "," + b.getAuthor() + "," + b.getYear() + ",0," + b.getPrice());
                    writer.newLine();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void loadDataFromFile(String fileName) {
        // Now handled by the Library class
        library.readDataFromFile(fileName);

        // After loading the data, update the table with the new book entries
        tableModel.setRowCount(0); // Clear the table
        for (Book book : library.getBooks()) {
            String[] data = {book.getTitle(), book.getAuthor(), String.valueOf(book.getYear()), String.valueOf(book.getPrice()), "Read"};
            tableModel.addRow(data);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryManagementSystem());
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    // When "Read" button is clicked, open the book content
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        String bookTitle = (String) tableModel.getValueAt(selectedRow, 0);
                        openBookContentWindow(bookTitle);
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        private void openBookContentWindow(String bookTitle) {
            if (bookContentFrame != null) {
                askToCloseCurrentBookWindow(bookTitle);
            } else {
                Book book = library.findBookByTitle(bookTitle);
                if (book != null) {
                    showBookContent(book);
                }
            }
        }

        private void showBookContent(Book book) {
            bookContentFrame = new JFrame(book.getTitle());
            currentBookTitle = book.getTitle();
            textPane = new JTextPane();
            textPane.setContentType("text/html");
            textPane.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(textPane);
            bookContentFrame.add(scrollPane);
            bookContentFrame.setSize(800, 600);

            try {
                // Display book information using book's methods
                StringBuilder content = new StringBuilder();
                content.append("<html><body>");
                content.append("Title: ").append(book.getTitle()).append("<br>");
                content.append("Author: ").append(book.getAuthor()).append("<br>");
                content.append("Year: ").append(book.getYear()).append("<br>");
                content.append("Price: ").append(book.getPrice()).append("<br>");

                // Attempt to read and display the content from a file with the book's title as the filename
                try (BufferedReader fileReader = new BufferedReader(new FileReader(book.getTitle() + ".txt"))) {
                    String line;
                    while ((line = fileReader.readLine()) != null) {
                        content.append(line).append("<br>");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    content.append("Error: Could not load book content.");
                }

                content.append("</body></html>");

                textPane.setText(content.toString());
            } catch (Exception e) {
                e.printStackTrace();
                textPane.setText("Error: Could not load book content.");
            }

            bookContentFrame.addWindowListener(new WindowAdapter() 
            {
                @Override
                public void windowClosing(WindowEvent e) {
                    askToCloseCurrentBookWindow(currentBookTitle);
                }
            });
        }

            private void askToCloseCurrentBookWindow(String bookTitle) {
            int choice = JOptionPane.showOptionDialog(
                    bookContentFrame,
                    "Do you want to close the currently opened book window?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Cancel", "Exit"},
                    "Cancel"
            );


            bookContentFrame.setVisible(true);
        }
    }

    public void askToCloseMainWindow() {
        int choice = JOptionPane.showOptionDialog(
                mainFrame,
                "Do you want to close the Library Management System?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Exit", "Cancel"},
                "Cancel"
        );

        if (choice == JOptionPane.YES_OPTION) {
            mainFrame.dispose();
            System.exit(0);
        }
    }
}