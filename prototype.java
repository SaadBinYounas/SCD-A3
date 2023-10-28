import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
                String[] parts = line.split(", ");
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

    public void addBook(Book book) {
        books.add(book);
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
}

public class prototype {
    private DefaultTableModel tableModel;
    private JTable table;
    private JFrame bookContentFrame;
    private JTextPane textPane;
    private String currentBookTitle;
    private boolean bookContentChanged;
    private JFrame mainFrame;
    private Library library = new Library();

    public prototype() {
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
        JButton addItemButton = new JButton("Add Item");
        JButton editItemButton = new JButton("Edit Item");
        JButton deleteItemButton = new JButton("Delete Item");

        buttonPanel.add(addItemButton);
        buttonPanel.add(editItemButton);
        buttonPanel.add(deleteItemButton);

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

    private void loadDataFromFile(String fileName) {
        // Now handled by the Library class
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new prototype());
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

                content.append("</body></html>");

                textPane.setText(content.toString());
            } catch (IOException e) {
                e.printStackTrace();
                textPane.setText("Error: Could not load book content.");
            }

            bookContentFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    askToCloseCurrentBookWindow(currentBookTitle);
                }
            });

            bookContentFrame.setVisible(true);
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

            if (choice == JOptionPane.YES_OPTION) {
                bookContentFrame.dispose();
                bookContentFrame = null;
                showBookContent(library.findBookByTitle(bookTitle));
            }
        }
    }

    private void askToCloseMainWindow() {
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
