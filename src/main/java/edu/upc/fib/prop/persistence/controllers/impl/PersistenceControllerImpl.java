package edu.upc.fib.prop.persistence.controllers.impl;

import edu.upc.fib.prop.models.AuthorsCollection;
import edu.upc.fib.prop.models.Document;
import edu.upc.fib.prop.models.DocumentsCollection;
import edu.upc.fib.prop.exceptions.AlreadyExistingDocumentException;
import edu.upc.fib.prop.persistence.controllers.PersistenceController;
import edu.upc.fib.prop.persistence.dao.authors.DaoAuthors;
import edu.upc.fib.prop.persistence.dao.documents.DaoDocuments;
import edu.upc.fib.prop.persistence.dao.files.DaoFiles;
import edu.upc.fib.prop.persistence.dao.users.DaoUsers;
import edu.upc.fib.prop.utils.Constants;
import edu.upc.fib.prop.utils.FileUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

public class PersistenceControllerImpl implements PersistenceController {

    private DaoUsers daoUsers;
    private DaoAuthors daoAuthors;
    private DaoDocuments daoDocuments;
    private DaoFiles daoFiles;

    private Connection c;

    public PersistenceControllerImpl() {
        System.out.println("Initializing persistence controller");
        initializeDB();
        daoUsers = new DaoUsers();
        daoAuthors = new DaoAuthors();
        daoDocuments = new DaoDocuments();
        daoFiles = new DaoFiles();
    }

    @Override
    public AuthorsCollection getAuthors() {
        openConnection();
        AuthorsCollection authorsCollection = daoAuthors.getAllAuthors(this.c);
        closeConnection();
        return authorsCollection;
    }

    @Override
    public DocumentsCollection getDocuments() {
        openConnection();
        DocumentsCollection documentsCollection = daoDocuments.getAllDocuments(this.c);
        closeConnection();
        return documentsCollection;
    }

    @Override
    public Set<String> getExcludedWords(String lang) {
        return daoFiles.getExcludedWords(lang);
    }

    @Override
    public void writeNewDocument(Document document) throws AlreadyExistingDocumentException {
        daoDocuments.addNewDocument(this.c, document);
    }

    /* Private helper methods */

    private void openConnection() {
        try {
            Class.forName(Constants.JDBC_DRIVER);
            this.c = DriverManager.getConnection(Constants.DB_DEVELOPMENT);
            this.c.setAutoCommit(false);
            System.out.println("Opened database connection successfully");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            this.c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeDB() {
        openConnection();

        Statement statement;
        String sql;
        try {
            statement = c.createStatement();
            sql = FileUtils.readFile("src/main/resources/sql/dbInitializer.sql");
            statement.executeUpdate(sql);
            statement.close();

            c.commit();
            System.out.println("DB initialized successfully");

            /*
            statement = c.createStatement();
            sql = FileUtils.readFile("src/main/resources/sql/dbFiller.sql");
            statement.executeUpdate(sql);
            statement.close();

            c.commit();
            System.out.println("DB filled successfully");
            */
        } catch (SQLException e) {
            e.printStackTrace();
        }

        closeConnection();
    }

}
