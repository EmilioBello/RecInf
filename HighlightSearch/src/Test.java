
 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
 
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
 
public class Test {
 
    public static Analyzer analyzer = new StandardAnalyzer();
    public static IndexWriterConfig config = new IndexWriterConfig(
            analyzer);
    public static RAMDirectory ramDirectory = new RAMDirectory();
    public static IndexWriter indexWriter;
 
    public static String readFileString(String file) {
        StringBuffer text = new StringBuffer();
        try {
 
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(file)), "UTF8"));
            String line;
            while ((line = in.readLine()) != null) {
                text.append(line + "\r\n");
            }
 
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
 
        return text.toString();
    }
 
    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        Document doc = new Document(); // create a new document
 
        /**
         * Create a field with term vector enabled
         */
        FieldType type = new FieldType();
        type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        type.setStored(true);
        type.setStoreTermVectors(true);
        type.setTokenized(true);
        type.setStoreTermVectorOffsets(true);
 
        Field field = new Field("title",
                "Como si llevara aquí una eternidad "
                + "Como si fuera a quedarse pa' siempre a cenar "
                + "Como si fuera el principio de un largo final, no se"
                + "Acaba de llegar pero ya pilla el compás, no se "
                + "Como si llevara aquí una eternidad "
                + "En mi trinchera para la batalla final "
                + "Capitaneando mi bando contra la maldad, no se "
                + "Acaba de llegar pero ya sabe luchar, no se. "
                + "Como el recuerdo que nunca se va... "
                + "Como las noches que nunca se acaban "
                + "Como la mano que no has de soltar "
                + "Viene para acompañarme en este caminar "
                + "Si se trata de ti te hago hueco aunque este lleno "
                + "No hace falta decir que en tu guerra mato y muero "
                + "Sabes bien que esta casa es tu hogar "
                + "Y yo ya se que vienes para quedarte. "
                + "", type); //term vector enabled
        Field f = new TextField("content", readFileString("/home/amanda/content.txt"),
                Field.Store.YES); 
        doc.add(field);
        doc.add(f);
 
        try {
            indexWriter = new IndexWriter(ramDirectory, config);
            indexWriter.addDocument(doc);
            indexWriter.close();
 
            IndexReader idxReader = DirectoryReader.open(ramDirectory);
            IndexSearcher idxSearcher = new IndexSearcher(idxReader);
            Query queryToSearch = new QueryParser("title", analyzer).parse("eternidad");
            TopDocs hits = idxSearcher
                    .search(queryToSearch, idxReader.maxDoc());
            SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
            Highlighter highlighter = new Highlighter(htmlFormatter,
                    new QueryScorer(queryToSearch));
 
            System.out.println("reader maxDoc is " + idxReader.maxDoc());
            System.out.println("scoreDoc size: " + hits.scoreDocs.length);
            for (int i = 0; i < hits.totalHits; i++) {
                int id = hits.scoreDocs[i].doc;
                Document docHit = idxSearcher.doc(id);
                String text = docHit.get("content");
                TokenStream tokenStream = TokenSources.getAnyTokenStream(idxReader, id, "content", analyzer);
                TextFragment[] frag = highlighter.getBestTextFragments(tokenStream, text, false, 4);
                for (int j = 0; j < frag.length; j++) {
                    if ((frag[j] != null) && (frag[j].getScore() > 0)) {
                        System.out.println((frag[j].toString()));
                    }
                }
 
                System.out.println("start highlight the title");
                // Term vector
                text = docHit.get("title");
                tokenStream = TokenSources.getAnyTokenStream(
                        idxSearcher.getIndexReader(), hits.scoreDocs[i].doc,
                        "title", analyzer);
                frag = highlighter.getBestTextFragments(tokenStream, text,
                        false, 4);
                for (int j = 0; j < frag.length; j++) {
                    if ((frag[j] != null) && (frag[j].getScore() > 0)) {
                        System.out.println((frag[j].toString()));
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidTokenOffsetsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
 