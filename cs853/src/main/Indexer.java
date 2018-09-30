package main;

import co.nstant.in.cbor.CborException;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;


public class Indexer {
    private boolean defualtEngine = true;
    private String indexPath = "";
    private String paragraphPath = "";
    public Indexer(){

    }

    public Indexer(boolean defualtEngine,String indexPath,String paragraphPath){
        this.defualtEngine = defualtEngine;
        this.indexPath = indexPath;
        this.paragraphPath = paragraphPath;
    }


    private IndexWriter indexWriter = null;

    public IndexWriter getIndexWriter(boolean defualtEngine) throws IOException {
        if (indexWriter == null){
            Directory indexDir = FSDirectory.open(Paths.get(indexPath));
            IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

            config.setOpenMode(OpenMode.CREATE);


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
            IndexWriter writer = getIndexWriter(defualtEngine);
            Document d = new Document();
            d.add(new StringField("id",p.getParaID(), Field.Store.YES));
            d.add(new TextField("text",p.getParaText(),Field.Store.YES));
            d.add(new TextField("content",p.getParaText(), Field.Store.NO));

            writer.addDocument(d);
        }
    }

    public void rebuildIndexes(List<Paragraph> list) throws IOException, CborException {
        getIndexWriter(defualtEngine);
        if (!list.isEmpty()){
            for (Paragraph p : list){
                indexFile(p);
            }
            closeIndexWriter();
        }
    }


    public void indexParagraph() throws IOException {
//        Directory indexdir = FSDirectory.open((new File(indexPath)).toPath());
//        IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
//        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
//        IndexWriter iw = new IndexWriter(indexdir, conf);
        //for (Data.Paragraph p : DeserializeData
          //      .iterableParagraphs(new FileInputStream(new File(paragraphPath)))) {
            //this.indexPara(iw, p);
        //}
//        iw.close();


        IndexWriter iw = getIndexWriter(defualtEngine);

        for (Data.Paragraph p : DeserializeData.iterableParagraphs(new FileInputStream(new File(paragraphPath)))){
            Document d = new Document();


            d.add(new StringField("paraid", p.getParaId(), Field.Store.YES));
            d.add(new TextField("parabody", p.getTextOnly(),
                    Field.Store.YES));
            FieldType indexType = new FieldType();
            indexType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
            indexType.setStored(true);
            indexType.setStoreTermVectors(true);

            d.add(new Field("content", p.getTextOnly(), indexType));

            iw.addDocument(d);
        }

        iw.close();

    }

}
