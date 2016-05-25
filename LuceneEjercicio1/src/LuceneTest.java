import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;


public class LuceneTest {
	public static void main(String[] args) {
		try {
			//Specify the analyzer for tokenizing text.
		    //The same analyzer should be used for indexing and searching
			StandardAnalyzer analyzer = new StandardAnalyzer();
			
			//Code to create the index in memory
			Directory index = new RAMDirectory();
			
			//To store an index on disk, use this instead:			
		    //Directory index = FSDirectory.open(Paths.get("/tmp/testindex"));
			
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter w = new IndexWriter(index, config);
			
			//To add elements to document use function addDoc
			addDoc(w, "El Nombre del Viento", "Patrick Rothfuss");
			addDoc(w, "El Temor de un Hombre Sabio", "Patrick Rothfuss");
			
			addDoc(w, "Juego de Tronos", "George RR Martin");
			addDoc(w, "Choque de Reyes", "George RR Martin");
			addDoc(w, "Festin de Cuervos", "George RR Martin");
			addDoc(w, "Tormenta de Espadas", "George RR Martin");
			
			addDoc(w, "La Sombra del Viento", "Carlos Ruiz Zafón");
			addDoc(w, "El Juego del Angel", "Carlos Ruiz Zafón");
			
			w.close();
			
			//	Text to search
			String querystr = "Viento";
			
			//	The "title" arg specifies the default field to use when no field is explicitly specified in the query
			Query q = new QueryParser("title", analyzer).parse(querystr);
			
			// Searching code
			int hitsPerPage = 10;
		    IndexReader reader = DirectoryReader.open(index);
		    IndexSearcher searcher = new IndexSearcher(reader);
		    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
		    searcher.search(q, collector);
		    ScoreDoc[] hits = collector.topDocs().scoreDocs;
		    
		    //	Code to display the results of search
		    System.out.println("Found " + hits.length + " hits.");
		    for(int i=0;i<hits.length;++i) {
		      int docId = hits[i].doc;
		      Document d = searcher.doc(docId);
		      System.out.println((i + 1) + ". " + d.get("author") + " " + d.get("title"));
		    }
		    
		    // reader can only be closed when there is no need to access the documents any more
		    reader.close();
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	private static void addDoc(IndexWriter w, String title, String author) throws IOException {
		  Document doc = new Document();
		  
		  doc.add(new TextField("title", title, Field.Store.YES)); // A text field will be tokenized		  
		  doc.add(new StringField("author", author, Field.Store.YES)); // We use a string field for author because we don't want it tokenized
		  
		  w.addDocument(doc);
	}
}
