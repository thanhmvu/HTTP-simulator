
package applayer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author thanhvu
 */
public class Document {    
    String url;
    String content;
    String OPEN = "<<";
    String CLOSE = ">>";
    HashMap<String,Document> embdDocs;
    
    public Document(String url){
        this.url = url;
        this.content = OPEN + url + CLOSE; // text is not available
        this.embdDocs = new HashMap<>();
    }
    
    public Document(String url, String content){
        this.url = url;
        this.content = content;
        this.embdDocs = new HashMap<>();
        
        this.findEmbdFiles();
    }
    
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
    
    public String getUrl(){
        return url;
    }
    
    public ArrayList<Document> getEmbdFiles(){
        if(embdDocs.isEmpty()){
            this.findEmbdFiles();
        }
        return new ArrayList<>(embdDocs.values());
    }
    
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
