package edu.upc.fib.prop.business.controllers.impl;

import edu.upc.fib.prop.business.controllers.BusinessController;
import edu.upc.fib.prop.business.models.AuthorsCollection;
import edu.upc.fib.prop.business.models.DocumentsCollection;
import edu.upc.fib.prop.business.search.SearchAuthor;
import edu.upc.fib.prop.business.search.SearchDocument;
import edu.upc.fib.prop.persistence.controllers.PersistenceController;
import edu.upc.fib.prop.persistence.controllers.impl.PersistenceControllerImpl;

import java.util.Set;
import java.util.TreeSet;

public class BusinessControllerImpl implements BusinessController {

    private PersistenceController persistenceController;

    private SearchAuthor searchAuthor;
    private SearchDocument searchDocument;

    private AuthorsCollection authorsCollection;
    private DocumentsCollection documentsCollection;

    private Set<String> excludedWordsCat;
    private Set<String> excludedWordsEng;
    private Set<String> excludedWordsEsp;

    public BusinessControllerImpl() {
        System.out.println("Initializating business controller");

        this.persistenceController = new PersistenceControllerImpl();
        this.searchAuthor = new SearchAuthor();
        this.searchDocument = new SearchDocument();

        //TODO: Implement as lazy load
        // Load in memory all authors and documents on instantiate
        this.authorsCollection = this.persistenceController.getAuthors();
        this.documentsCollection = this.persistenceController.getDocuments();

        excludedWordsCat = this.persistenceController.getExcludedWords("cat");
        excludedWordsEng = this.persistenceController.getExcludedWords("eng");
        excludedWordsEsp = this.persistenceController.getExcludedWords("esp");
    }

    @Override
    public AuthorsCollection searchMatchingAuthors(String authorPrefix) {
        return this.searchAuthor.filterByPrefix(this.authorsCollection, authorPrefix);
    }

    @Override
    public DocumentsCollection searchDocumentsByAuthor(String authorName) {
        return this.searchDocument.filterByAuthor(this.documentsCollection, authorName);
    }

}
