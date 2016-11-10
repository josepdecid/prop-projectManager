package edu.upc.fib.prop.models;

import edu.upc.fib.prop.business.documents.DocumentTools;
import edu.upc.fib.prop.exceptions.AlreadyExistingDocumentException;
import edu.upc.fib.prop.exceptions.InvalidDetailsException;
import edu.upc.fib.prop.utils.Strings;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DocumentsCollection {

    private List<Document> documents;
    private Map<String, Integer> wordsFrequencies;

    public DocumentsCollection() {
        this.documents = new ArrayList<>();
        this.wordsFrequencies = new TreeMap<>();
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void addDocument(Document document) throws InvalidDetailsException{
        if(!DocumentTools.isCorrect(document)) throw new InvalidDetailsException();
        this.documents.add(document);

        for(Map.Entry<String,Float> entry : document.getTermFrequencyList().entrySet()) {
            addWord(entry.getKey());
        }
    }

    public void deleteDocument(Document document) {
        this.documents.remove(document);
        for(Map.Entry<String,Float> entry : document.getTermFrequencyList().entrySet()) {
            removeWord(entry.getKey());
        }
    }

    public boolean containsTitleAndAuthor(String title, String author){
        return (this.getDocument(title,author) != null);
    }

    public Document updateDocument(Document oldDoc, Document newDoc) throws InvalidDetailsException, AlreadyExistingDocumentException {
        Document updatedDoc = DocumentTools.mergeDocs(oldDoc, newDoc);
        if(containsTitleAndAuthor(updatedDoc.getTitle(), updatedDoc.getAuthor())) throw new AlreadyExistingDocumentException();

        if(!DocumentTools.isCorrect(updatedDoc)) throw new InvalidDetailsException();
        this.documents.remove(oldDoc);
        for(Map.Entry<String,Float> entry : oldDoc.getTermFrequencyList().entrySet()) {
            removeWord(entry.getKey());
        }
        this.documents.add(updatedDoc);
        for(Map.Entry<String,Float> entry : updatedDoc.getTermFrequencyList().entrySet()) {
            addWord(entry.getKey());
        }
        return updatedDoc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentsCollection that = (DocumentsCollection) o;
        return documents != null ? documents.equals(that.documents) : that.documents == null;

    }

    public void addWord(String word){
        if(wordsFrequencies.containsKey(word)) wordsFrequencies.put(word, wordsFrequencies.get(word)+1);
        else wordsFrequencies.put(word, 1);
    }

    public void removeWord(String word){
        if(wordsFrequencies.containsKey(word)) wordsFrequencies.put(word, wordsFrequencies.get(word)-1);
       if(wordsFrequencies.get(word)<=0) wordsFrequencies.remove(word);
    }

    public Float getIdf(String word){
        return (float)Math.log(documents.size()/wordsFrequencies.get(word));
    }

    public int size(){ return documents.size();}

    public Document getDocument(String title, String author){
        for(Document d : documents){
            if(d.getTitle().toLowerCase().equals(title.toLowerCase()) && d.getAuthor().toLowerCase().equals(author.toLowerCase())) return d;
        }
        return null;
    }

    public DocumentsSet getAllDocuments() {
        DocumentsSet ds = new DocumentsSet();
        for(Document doc : this.documents) ds.add(doc);
        return ds;
    }

}
