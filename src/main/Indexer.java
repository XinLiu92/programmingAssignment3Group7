package main;

import co.nstant.in.cbor.CborException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;


public class Indexer {
    private boolean defualtEngine = true;
    private String indexPath = "";
    public Indexer(){

    }

    public Indexer(boolean defualtEngine,String indexPath){
        this.defualtEngine = defualtEngine;
        this.indexPath = indexPath;
    }


    private IndexWriter indexWriter = null;

    public IndexWriter getIndexWriter(boolean create,boolean defualtEngine) throws IOException {
        if (indexWriter == null){
            Directory indexDir = FSDirectory.open(Paths.get(indexPath));
            IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

            if (create) {
                config.setOpenMode(OpenMode.CREATE);
            } else {
                config.setOpenMode(OpenMode.CREATE_OR_APPEND);
            }

            if (!defualtEngine){
                config.setSimilarity(createCustomeSimiliarity());
            }


            indexWriter= new IndexWriter(indexDir,config);
        }
        return indexWriter;
    }
    private Similarity createCustomeSimiliarity() {

        Similarity sim = new SimilarityBase() {

            @Override
            protected float score(BasicStats stats, float freq, float docLen) {

                return freq;
            }

            @Override
            public String toString() {

                return null;
            }

        };

        return sim;
    }

    public void closeIndexWriter() throws IOException{
        if (indexWriter != null){
            indexWriter.close();
        }
    }

    public void indexFile(Paragraph p) throws IOException {
        if(p != null){
            IndexWriter writer = getIndexWriter(false,defualtEngine);
            Document d = new Document();
            d.add(new StringField("id",p.getParaID(), Field.Store.YES));
            d.add(new TextField("text",p.getParaText(),Field.Store.YES));
            d.add(new TextField("content",p.getParaText(), Field.Store.NO));

            writer.addDocument(d);
        }
    }

    public void rebuildIndexes(List<Paragraph> list) throws IOException, CborException {
        getIndexWriter(true,defualtEngine);
        if (!list.isEmpty()){
            for (Paragraph p : list){
                indexFile(p);
            }
            closeIndexWriter();
        }
    }

}
