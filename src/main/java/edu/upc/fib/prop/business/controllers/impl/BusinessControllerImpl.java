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
import edu.upc.fib.prop.utils.FileUtils;
import edu.upc.fib.prop.utils.ImportExport;

import javax.print.Doc;
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
    public void checkLoginDetails(String email, String password)
            throws InvalidDetailsException, UserNotFoundException {
        password = usersManager.login(email, password);
        User user = persistenceController.loginUser(email, password);
        usersManager.setCurrentUser(user);
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
    public Document importDocument(String path) throws ImportExportException, AlreadyExistingDocumentException, InvalidDetailsException, DocumentContentNotFoundException {
        Document doc = ImportExport.importDocument(path);
        this.storeNewDocument(doc);
        return doc;
    }

    @Override
    public void exportDocument(String pathToExport, Document document, String os) throws ImportExportException, DocumentContentNotFoundException {
        ImportExport.exportDocument(pathToExport, document, os);
    }

    @Override
    public SortedDocumentsSet searchDocumentsByRelevance(Document document, int k)
            throws DocumentNotFoundException {
        return this.searchDocument.searchForSimilarDocuments(this.documentsCollection, document, k);
    }

    @Override
    public SortedDocumentsSet searchDocumentsByQuery(String str, int k)
            throws DocumentNotFoundException {
        this.persistenceController.createContentFile(str, "query.txt");
         Document document = new Document("", "", "query.txt");
        try {
            document.updateFrequencies();
        } catch (DocumentContentNotFoundException e) {
            e.printStackTrace();
        }
        this.persistenceController.deleteContentFile("query.txt");
        return this.searchDocument.searchForSimilarDocuments(this.documentsCollection, document, k);
    }

    @Override
    public void rateDocument(Document document, int rating) throws DocumentNotFoundException {
        persistenceController.rateDocument(document, rating, this.usersManager.getCurrentUser().getEmail());
        try {
            documentsCollection.updateDocument(document, persistenceController.getDocument(document.getTitle(), document.getAuthor()));
        } catch (InvalidDetailsException e) {
            e.printStackTrace();
        } catch (AlreadyExistingDocumentException e) {
            e.printStackTrace();
        } catch (DocumentContentNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addDocumentToFavourites(Document document) throws DocumentNotFoundException {
        persistenceController.addDocumentToFavourites(document, this.usersManager.getCurrentUser().getEmail());
    }

    @Override
    public void deleteDocumentFromFavourites(Document document) throws DocumentNotFoundException {
        persistenceController.deleteDocumentFromFavourites(document, this.usersManager.getCurrentUser().getEmail());
    }

    @Override
    public SortedDocumentsSet getRelevantDocuments(SortedDocumentsSet list, double rv) {
        return this.searchDocument.getRelevantDocuments(list, rv);
    }

    @Override
    public SortedDocumentsSet getNonRelevantDocuments(SortedDocumentsSet list, double rv) {
        return this.searchDocument.getNonRelevantDocuments(list, rv);
    }

    @Override
    public Document getRocchioQuery(String query, SortedDocumentsSet rDocs, SortedDocumentsSet nrDocs, float b, float c) {
        String relatedDocContents = "", nonrelatedDocContents = "";
        try {
            relatedDocContents = searchDocument.getAggregatedContent(rDocs);
            nonrelatedDocContents = searchDocument.getAggregatedContent(nrDocs);
        } catch (DocumentContentNotFoundException e) {
            e.printStackTrace();
        }

        this.persistenceController.createContentFile(relatedDocContents, "rDocC.txt");
        Document docr = new Document("", "", "rDocC.txt");
        try {
            docr.updateFrequencies();
        } catch (DocumentContentNotFoundException e) {
            e.printStackTrace();
        }


        this.persistenceController.createContentFile(nonrelatedDocContents, "nrDocC.txt");
        Document docnr = new Document("", "", "nrDocC.txt");
        try {
            docnr.updateFrequencies();
        } catch (DocumentContentNotFoundException e) {
            e.printStackTrace();
        }


        this.persistenceController.createContentFile(query, "query.txt");
        Document docquery = new Document("", "", "query.txt");
        try {
            docquery.updateFrequencies();
        } catch (DocumentContentNotFoundException e) {
            e.printStackTrace();
        }

        try {
            docquery = searchDocument.getRocchioQuery(docquery,docr,docnr,b,c);
        } catch (DocumentContentNotFoundException e) {
            e.printStackTrace();
        }
        this.persistenceController.deleteContentFile("rDocC.txt");
        this.persistenceController.deleteContentFile("nrDocC.txt");
        this.persistenceController.deleteContentFile("query.txt");
        return docquery;
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
    public void storeNewDocument(Document document)
            throws AlreadyExistingDocumentException, InvalidDetailsException, DocumentContentNotFoundException {
        document.setUser(usersManager.getCurrentUser().getEmail());
        if (!document.isCorrect()) {
            throw new InvalidDetailsException();
        } else if (!document.isContentPathCorrect()) {
            throw new DocumentContentNotFoundException();
        } else if (documentsCollection.containsTitleAndAuthor(document.getTitle(), document.getAuthor())) {
            throw new AlreadyExistingDocumentException();
        } else {
            document.updateFrequencies();
            document.updatePositions();
            try {
                documentsCollection.addDocument(document);
                persistenceController.writeNewDocument(document);
                try {
                    this.addDocumentToFavourites(document);
                } catch (DocumentNotFoundException e) {
                    e.printStackTrace();
                }
                reloadDBData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateDocument(Document oldDoc, Document newDoc) throws InvalidDetailsException, AlreadyExistingDocumentException, DocumentContentNotFoundException {
        if(!(newDoc.getAuthor().equals("") && newDoc.getTitle().equals("") && newDoc.getContent().equals(""))){
            if(documentsCollection.containsTitleAndAuthor(newDoc.getTitle(), newDoc.getAuthor())) throw  new AlreadyExistingDocumentException();
            Document updatedDoc = documentsCollection.updateDocument(oldDoc, newDoc);
            persistenceController.updateDocument(oldDoc, updatedDoc);
            if(!oldDoc.getTitle().equals(updatedDoc.getTitle()) || !oldDoc.getAuthor().equals(updatedDoc.getAuthor())){
                persistenceController.deleteAllFavouritesOfDocument(oldDoc);
            }
            reloadDBData();
        }
    }

    @Override
    public void deleteDocument(String title, String authorName) throws DocumentNotFoundException {
        Document document = searchDocumentsByTitleAndAuthor(title, authorName);
        documentsCollection.deleteDocument(document);
        persistenceController.deleteDocument(document);
        persistenceController.deleteAllFavouritesOfDocument(document);
        reloadDBData();
    }

    //////////

    // TODO: Improve performance
    private void reloadDBData() {
        authorsCollection = persistenceController.getAuthors();
        documentsCollection = persistenceController.getDocuments();
    }

}
