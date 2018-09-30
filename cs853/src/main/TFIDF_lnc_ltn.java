package main;

import edu.unh.cs.treccar_v2.Data;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class ResultComparator implements Comparator<DocumentResults>
{
    public int compare(DocumentResults d2, DocumentResults d1)
    {
        if(d1.getScore() < d2.getScore())
            return -1;
        if(d1.getScore() == d2.getScore())
            return 0;
        return 1;
    }
}


public class TFIDF_lnc_ltn {

    private IndexSearcher searcher;
    private QueryParser parser;

    private List<Data.Page> pageList;

    private int numDocs;

    private HashMap<Query, ArrayList<DocumentResults>> queryResults;


    TFIDF_lnc_ltn(List<Data.Page> pl, int n, String index) throws ParseException, IOException
    {

        numDocs = n;
        pageList = pl;

        parser = new QueryParser("parabody", new StandardAnalyzer());

        String INDEX_DIRECTORY = index;
        searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(INDEX_DIRECTORY).toPath()))));

        SimilarityBase lnc_ltn = new SimilarityBase() {
            protected float score(BasicStats stats, float freq, float docLen) {
                return (float)(1 + Math.log10(freq));
            }

            @Override
            public String toString() {
                return null;
            }
        };
        searcher.setSimilarity(lnc_ltn);
    }

    /**
     *
     * @param runfile
     * @throws IOException
     * @throws ParseException
     */
    public void dumpScoresTo(String runfile) throws IOException, ParseException
    {
        queryResults = new HashMap<>();

        for(Data.Page page:pageList)
        {
            HashMap<Document, Float> scores = new HashMap<>();
            HashMap<Document, DocumentResults> docMap = new HashMap<>();
            PriorityQueue<DocumentResults> docQueue = new PriorityQueue<>(new ResultComparator());
            ArrayList<DocumentResults> docResults = new ArrayList<>();
            HashMap<TermQuery, Float> queryweights = new HashMap<>();
            ArrayList<TermQuery> terms = new ArrayList<>();
            Query q = parser.parse(page.getPageName());
            String qid = page.getPageId();

            for(String term: page.getPageName().split(" "))
            {
                TermQuery tq = new TermQuery(new Term("parabody", term));
                terms.add(tq);

                queryweights.put(tq, queryweights.getOrDefault(tq, 0.0f)+1.0f);
            }
            for(TermQuery query: terms)
            {

                IndexReader reader = searcher.getIndexReader();

                float DF = (reader.docFreq(query.getTerm()) == 0) ? 1 : reader.docFreq(query.getTerm());

                float qTF = (float)(1 + Math.log10(queryweights.get(query)));
                float qIDF = (float)(Math.log10(reader.numDocs()/DF));
                float qWeight = qTF * qIDF;

                queryweights.put(query, qWeight);

                TopDocs tpd = searcher.search(query, numDocs);
                for(int i = 0; i < tpd.scoreDocs.length; i++)
                {
                    Document doc = searcher.doc(tpd.scoreDocs[i].doc);
                    double score = tpd.scoreDocs[i].score * queryweights.get(query);

                    DocumentResults dResults = docMap.get(doc);
                    if(dResults == null)
                    {
                        dResults = new DocumentResults(doc);
                    }
                    float prevScore = dResults.getScore();
                    dResults.score((float)(prevScore+score));
                    dResults.queryId(qid);
                    dResults.paragraphId(doc.getField("paraid").stringValue());
                    dResults.teamName("Group7");
                    dResults.methodName("tf.idf_lnc_ltn");
                    docMap.put(doc, dResults);

                    scores.put(doc, (float)(prevScore+score));
                }
            }

            float cosineLength = 0.0f;
            for(Map.Entry<Document, Float> entry: scores.entrySet())
            {
                Document doc = entry.getKey();
                Float score = entry.getValue();

                cosineLength = (float)(cosineLength + Math.pow(score, 2));
            }
            cosineLength = (float)(Math.sqrt(cosineLength));

            for(Map.Entry<Document, Float> entry: scores.entrySet())
            {
                Document doc = entry.getKey();
                Float score = entry.getValue();


                scores.put(doc, score/scores.size());
                DocumentResults dResults = docMap.get(doc);
                dResults.score(dResults.getScore()/cosineLength);

                docQueue.add(dResults);
            }

            int rankCount = 0;
            DocumentResults current;
            while((current = docQueue.poll()) != null)
            {
                current.rank(rankCount);
                docResults.add(current);
                rankCount++;
            }

            queryResults.put(q, docResults);
        }


        System.out.println("TFIDF_lnc_ltn writing results to: \t\t" + runfile);
        FileWriter runfileWriter = new FileWriter(new File(runfile));
        for(Map.Entry<Query, ArrayList<DocumentResults>> results: queryResults.entrySet())
        {
            ArrayList<DocumentResults> list = results.getValue();
            for(int i = 0; i < list.size(); i++)
            {
                DocumentResults dr = list.get(i);
                runfileWriter.write(dr.getRunfileString());
            }
        }
        runfileWriter.close();


    }
}
