package edu.upc.fib.prop.business.controllers.impl;

import edu.upc.fib.prop.business.controllers.BusinessController;
import edu.upc.fib.prop.business.search.SearchBooleanExpression;
import edu.upc.fib.prop.business.search.impl.SearchAuthorImpl;
import edu.upc.fib.prop.business.search.impl.SearchBooleanExpressionImpl;
import edu.upc.fib.prop.business.search.impl.SearchDocumentImpl;
import edu.upc.fib.prop.business.users.UsersManager;
import edu.upc.fib.prop.business.users.impl.UsersManagerImpl;
import edu.upc.fib.prop.exceptions.*;
import edu.upc.fib.prop.models.*;
import edu.upc.fib.prop.persistence.controllers.PersistenceController;
import edu.upc.fib.prop.persistence.controllers.impl.PersistenceControllerImpl;
import edu.upc.fib.prop.utils.ImportExport;

import java.sql.SQLException;

public class BusinessControllerImpl implements BusinessController {

    private PersistenceController persistenceController;

    private SearchAuthorImpl searchAuthor;
    private SearchDocumentImpl searchDocument;
    private SearchBooleanExpression searchBooleanExpression;

    private AuthorsCollection authorsCollection;
    private DocumentsCollection documentsCollection;

    private UsersManager usersManager;

    public BusinessControllerImpl() {
        System.out.println("Initializing business controller");

        this.persistenceController = new PersistenceControllerImpl();
        this.searchAuthor = new SearchAuthorImpl();
        this.searchDocument = new SearchDocumentImpl();
        this.searchBooleanExpression = new SearchBooleanExpressionImpl();

        // Load in memory all authors and documents on instantiate
        this.authorsCollection = this.persistenceController.getAuthors();
        this.documentsCollection = this.persistenceController.getDocuments();

        this.usersManager = new UsersManagerImpl();
    }

    /*--------------- Users */


    @Override
    public String checkLoginDetails(String email, String password)
            throws InvalidDetailsException, UserNotFoundException {
        password = usersManager.login(email, password);
        User user = persistenceController.loginUser(email, password);
        usersManager.setCurrentUser(user);
        return user.getName();
    }

    @Override
    public void registerNewUser(String email, String userName, String password, String password2)
            throws InvalidDetailsException, AlreadyExistingUserException {
        User user = usersManager.register(email, userName, password, password2);
        persistenceController.createUser(user);
        usersManager.setCurrentUser(user);
    }

    @Override
    public void updateUser(String newEmail, String newName, String newPassword)
            throws InvalidDetailsException, UserNotFoundException, AlreadyExistingUserException {
        User user = usersManager.register(newEmail, newName, newPassword, newPassword);
        persistenceController.updateUser(usersManager.getCurrentUser(), user);
        usersManager.setCurrentUser(user);
    }

    @Override
    public void deleteUser() throws UserNotFoundException {
        User user = usersManager.getCurrentUser();
        persistenceController.deleteUser(user);
        usersManager.logout();
    }

    @Override
    public void logout() {
        usersManager.logout();
    }

    /*--------------- Documents */

    @Override
    public DocumentsSet searchForAllDocuments() {
        return this.documentsCollection.getAllDocuments();
    }

    @Override
    public Document importDocument(String path)
            throws ImportExportException, AlreadyExistingDocumentException, InvalidDetailsException {
        Document doc = ImportExport.importDocument(path);
        this.storeNewDocument(doc);
        return doc;
    }

    @Override
    public void exportDocument(String pathToExport, Document document, String os) throws ImportExportException {
        ImportExport.exportDocument(pathToExport, document, os);
    }

    @Override
    public SortedDocumentsSet searchDocumentsByRelevance(Document document, int k)
            throws DocumentNotFoundException {
        return this.searchDocument.searchForSimilarDocuments(this.documentsCollection, document, k);
    }

    @Override
    public SortedDocumentsSet searchDocumentsByQuery(String str, int k) {
        Document document = new Document("", "", str);
        document.updateFrequencies();
        return this.searchDocument.searchForSimilarDocuments(this.documentsCollection, document, k);
    }

    @Override
    public Float rateDocument(String title, String author, int rating) throws DocumentNotFoundException {

        try {
            Document document = persistenceController.getDocument(title, author);
            persistenceController.rateDocument(document, rating, this.usersManager.getCurrentUser().getEmail());
            Document updatedDoc = persistenceController.getDocument(document.getTitle(), document.getAuthor());
            documentsCollection.updateDocument(document, updatedDoc);
            return updatedDoc.getRating();
        } catch (InvalidDetailsException | AlreadyExistingDocumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void addDocumentToFavourites(String title, String author) throws DocumentNotFoundException {
        persistenceController.addDocumentToFavourites(title, author, this.usersManager.getCurrentUser().getEmail());
    }

    @Override
    public void deleteDocumentFromFavourites(String title, String author) throws DocumentNotFoundException {
        persistenceController.deleteDocumentFromFavourites(title, author, this.usersManager.getCurrentUser().getEmail());
    }

    @Override
    public Document getRocchioQuery(String query, SortedDocumentsSet list, double rv, float b, float c) {
       /*     throws DocumentNotFoundException {
        Document docquery = new Document("", "", "query");
        docquery.updateFrequencies();
        return searchDocument.getRocchioQuery(docquery,list,rv,b,c);*/
        return null;
    }

    @Override
    public boolean isDocumentFavourite(String title, String author) {
        return persistenceController.isDocumentFavourite(title, author, usersManager.getCurrentUser().getEmail());
    }

    @Override
    public void updateDocument(String title, String author, Document newDoc) throws AlreadyExistingDocumentException, InvalidDetailsException, DocumentNotFoundException {
        Document oldDoc = searchDocumentsByTitleAndAuthor(title, author);
        if(!(newDoc.getAuthor().equals("") && newDoc.getTitle().equals("") && newDoc.getContent().equals(""))) {
            if(!(oldDoc.getAuthor().toLowerCase().equals(newDoc.getAuthor().toLowerCase()) &&
                    oldDoc.getAuthor().toLowerCase().equals(newDoc.getAuthor().toLowerCase()))) {
                if (documentsCollection.containsTitleAndAuthor(newDoc.getTitle(), newDoc.getAuthor()))
                    throw new AlreadyExistingDocumentException();
            }
            if(!oldDoc.getTitle().equals(newDoc.getTitle()) || !oldDoc.getAuthor().equals(newDoc.getAuthor())){
                persistenceController.deleteAllFavouritesOfDocument(oldDoc);
                persistenceController.deleteAllRatingsOfDocument(oldDoc);
                newDoc.setRating(1f);
            }
            Document updatedDoc = documentsCollection.updateDocument(oldDoc, newDoc);
            persistenceController.updateDocument(oldDoc, updatedDoc);

            reloadDBData();
        }
    }

    @Override
    public DocumentsSet searchDocumentsByBooleanExpression(String booleanExpression) throws InvalidQueryException {
        return searchBooleanExpression.searchDocumentsByBooleanExpression(booleanExpression, documentsCollection);
    }

    /*--------------- Authors */

    @Override
    public AuthorsCollection searchMatchingAuthors(String authorPrefix) throws AuthorNotFoundException {
        return this.searchAuthor.filterByPrefix(this.authorsCollection, authorPrefix);
    }

    @Override
    public DocumentsCollection searchDocumentsByAuthor(String authorName) throws DocumentNotFoundException {
        return this.searchDocument.filterByAuthor(this.documentsCollection, authorName);
    }

    @Override
    public Document searchDocumentsByTitleAndAuthor(String title, String authorName)
            throws DocumentNotFoundException {
        return this.searchDocument.filterByTitleAndAuthor(this.documentsCollection, title, authorName);
    }

    @Override
    public DocumentsCollection getCurrentUserDocuments() {
        String user = this.usersManager.getCurrentUser().getEmail();
        return this.searchDocument.filterByUser(this.documentsCollection, user);
    }

    @Override
    public DocumentsCollection getCurrentUserFavourites() {
        String user = this.usersManager.getCurrentUser().getEmail();
        return this.persistenceController.getFavouriteDocuments(user);
    }

    @Override
    public void storeNewDocument(Document document) throws AlreadyExistingDocumentException, InvalidDetailsException {
        document.setUser(usersManager.getCurrentUser().getEmail());
        if (!document.isCorrect()) {
            throw new InvalidDetailsException();
        } else if (documentsCollection.containsTitleAndAuthor(document.getTitle(), document.getAuthor())) {
            throw new AlreadyExistingDocumentException();
        } else {
            document.updateFrequencies();
            document.updatePositions();
            try {
                documentsCollection.addDocument(document);
                persistenceController.writeNewDocument(document);
                reloadDBData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteDocument(String title, String authorName) throws DocumentNotFoundException {
        Document document = searchDocumentsByTitleAndAuthor(title, authorName);
        documentsCollection.deleteDocument(document);
        persistenceController.deleteDocument(document);
        persistenceController.deleteAllFavouritesOfDocument(document);
        persistenceController.deleteAllRatingsOfDocument(document);
        reloadDBData();
    }

    //////////

    // TODO: Improve performance
    private void reloadDBData() {
        authorsCollection = persistenceController.getAuthors();
        documentsCollection = persistenceController.getDocuments();
    }

}
