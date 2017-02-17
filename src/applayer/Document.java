
package applayer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Create the document displayed in client
 * @author thanhvu
 */
public class Document {    
    String url;
    String content;
    String OPEN = "<<";
    String CLOSE = ">>";
    HashMap<String,Document> embdDocs;
    
    /**
     * Create a document with only URL (content unavailable)
     * @param url The URL of the document
     */
    public Document(String url){
        this.url = url;
        this.content = OPEN + url + CLOSE; // text is not available
        this.embdDocs = new HashMap<>();
    }
    
    /**
     * Create a document
     * @param url The URL of the document
     * @param content The content of the document
     */
    public Document(String url, String content){
        this.url = url;
        this.content = content;
        this.embdDocs = new HashMap<>();
        
        this.findEmbdFiles();
    }
    
    /**
     * Find all embedded files in the content of the document
     */
    private void findEmbdFiles(){ 
        int start = content.indexOf(OPEN,0);
        int end = content.indexOf(CLOSE,0);
        while( start >= 0 && end > start){
	    String embdUrl = content.substring(start+2,end);
            Document doc = new Document(embdUrl);
            embdDocs.put(embdUrl, doc);
	    start = content.indexOf(OPEN,end);
	    end = content.indexOf(CLOSE,start);
    	}
    }
    
    
    /**
     * Get the URL
     * @return The URL of the document
     */
    public String getUrl(){
        return url;
    }
    
    /**
     * Get the list of document embedded
     * @return the embedded documents
     */
    public ArrayList<Document> getEmbdFiles(){
        if(embdDocs.isEmpty()){
            this.findEmbdFiles();
        }
        return new ArrayList<>(embdDocs.values());
    }
    
    /**
     * Get the full text of the document, including embedded docs
     * @return The full text of the document, including embedded docs
     */
    public String getFullText(){
        String full_text = this.content;
        for(String embdUrl: embdDocs.keySet()){
            Document doc = embdDocs.get(embdUrl);
            full_text = full_text.replaceAll(OPEN + embdUrl + CLOSE, doc.getFullText());
        }
        return full_text;
    }
    
    public void addContent(String content){
        this.content = content;
    }
    
    
    public boolean addEmbdContent(String embdFile, String content){
        Document d = this.embdDocs.get(embdFile);
        if(d != null){
            d.addContent(content);
            return true;
        }
        return false;
    }
    
    public void addEmbdDoc(String embdFile, Document doc){
        embdDocs.put(embdFile, doc);
    }
    
    public String getEmbdContent(String embdFile){
        return this.embdDocs.get(embdFile).getContent();
    }
    
    public String getContent(){ return content;}
    
    public int getNumOfEmbdFiles(){ return embdDocs.size();}
    
    public String getOpenSign(){ return OPEN;}
    public String getCloseSign(){ return CLOSE;}
}
