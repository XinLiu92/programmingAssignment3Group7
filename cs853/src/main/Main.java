package main;

import co.nstant.in.cbor.CborException;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.FSDirectory;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class Main {

    private static IndexSearcher is = null;
    private  static String methdName = "";
    private  static  String indexPath = "";
    private  static  boolean useDefaultScore = true;
    private  static  String paragraphPath = "";
    private  static  String pagePath = "";
    private  static  String qrelPath = "";
    private  static  String hierarchicalQrel = "";
    private static boolean customScore = false;
    private static QueryParser qp = null;
    private static List<Data.Page> getPageListFromPath(String path) {
        List<Data.Page> pageList = new ArrayList<Data.Page>();

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (Data.Page page : DeserializeData.iterableAnnotations(fis)) {
                pageList.add(page);
                //System.out.println(page.toString());

            }

        return pageList;
    }
    private static void rankParas(Data.Page page, int n, String filename)
            throws IOException, ParseException {
        if (is == null) {
            is = new IndexSearcher(DirectoryReader.open(FSDirectory
                    .open((new File(indexPath).toPath()))));
        }

        if (customScore) {
            SimilarityBase mySimiliarity = new SimilarityBase() {
                protected float score(BasicStats stats, float freq, float docLen) {
                    return freq;
                }

                @Override
                public String toString() {
                    return null;
                }
            };
            is.setSimilarity(mySimiliarity);
        }

        if (qp == null) {
            qp = new QueryParser("parabody", new StandardAnalyzer());
        }

        Query q;
        TopDocs tds;
        ScoreDoc[] retDocs;

        System.out.println("Query: " + page.getPageName());
        q = qp.parse(page.getPageName());

        tds = is.search(q, n);
        retDocs = tds.scoreDocs;
        Document d;
        ArrayList<String> runStringsForPage = new ArrayList<String>();
        String method = "lucene-score";
        if (customScore)
            method = "custom-score";
        for (int i = 0; i < retDocs.length; i++) {
            d = is.doc(retDocs[i].doc);
            System.out.println("Doc " + i);
            System.out.println("Score " + tds.scoreDocs[i].score);
            System.out.println(d.getField("paraid").stringValue());
            System.out.println(d.getField("parabody").stringValue() + "\n");

            // runFile string format $queryId Q0 $paragraphId $rank $score
            // $teamname-$methodname
            String runFileString = page.getPageId() + " Q0 "
                    + d.getField("paraid").stringValue() + " " + i + " "
                    + tds.scoreDocs[i].score + " Group7-" + method;
            runStringsForPage.add(runFileString);
        }

        FileWriter fw = new FileWriter("." + "/"
                + filename, true);
        for (String runString : runStringsForPage)
            fw.write(runString + "\n");
        fw.close();
    }


    public static void main(String[] args) throws IOException, ParseException, CborException {
        System.setProperty("file.encoding", "UTF-8");


        //
        ///Users/xinliu/Documents/UNH/18Fall/cs853/index /Users/xinliu/Documents/UNH/18Fall/cs853/test200/test200-train/train.pages.cbor-paragraphs.cbor
        //String defualtScore = args[0];
        indexPath = args[0];
        paragraphPath = args[1];
        pagePath = args[2];
        qrelPath = args[3];
        hierarchicalQrel = args[4];


//        indexPath = "/Users/xinliu/Documents/UNH/18Fall/cs853/index";
//        paragraphPath = "/Users/xinliu/Documents/UNH/18Fall/cs853/test200/test200-train/train.pages.cbor-paragraphs.cbor";
//        //pagePath = "/Users/xinliu/Documents/UNH/18Fall/cs853/test200/test200-train/train.pages.cbor-article.qrels";
//        pagePath = "/Users/xinliu/Documents/UNH/18Fall/cs853/test200/test200-train/train.pages.cbor-outlines.cbor";
//        qrelPath = "/Users/xinliu/Documents/UNH/18Fall/cs853/test200/test200-train/train.pages.cbor-article.qrels";
//        hierarchicalQrel = "/Users/xinliu/Documents/UNH/18Fall/cs853/test200/test200-train/train.pages.cbor-hierarchical.qrels"

//        useDefaultScore = Boolean.valueOf(defualtScore);
//        if (useDefaultScore){
//            methdName = "default";
//            System.out.println("using lucene default score function");
//        }else{
//            methdName = "custome";
//            System.out.println("using lucene customed score function");
//        }


        //Main main = new Main();
        Indexer id = new Indexer(useDefaultScore,indexPath,paragraphPath);
        id.indexParagraph();

        List<Data.Page> pagelist = getPageListFromPath(pagePath);

        File f = new File("." + "/result-lucene.run");
        if (f.exists()) {
            FileWriter createNewFile = new FileWriter(f);
            createNewFile.write("");
        }

        for (Data.Page page : pagelist) {

            rankParas(page, 100, "result-lucene.run");
        }

//
        TFIDF_anc_apc tfidf_anc_apc = new TFIDF_anc_apc(indexPath);
        tfidf_anc_apc.retrieveAllAncApcResults(pagelist, "." + "/tfidf_anc_apc.run");

//
//
//        //heading
//
        tfidf_anc_apc.retrieveAllAncApcResultsByQuryHeading(pagelist,"."+ "/tfidf_anc_apc_queryHeading.run");

        TFIDF_bnn_bnn tfidf_bnn_bnn = new TFIDF_bnn_bnn(pagelist, 100,indexPath);

        tfidf_bnn_bnn.doScoring();
////
////
        TFIDF_lnc_ltn tfidf_lnc_ltn = new TFIDF_lnc_ltn(pagelist, 100,indexPath);
        tfidf_lnc_ltn.dumpScoresTo("." + "/tfidf_lnc_ltn.run");
           String lucenedefault = "result-lucene.run";
			HashMap<String, HashMap<String, String>> lucene_data = read_dataFile(lucenedefault);


        			String tfIdf_anc_apc = "tfidf_anc_apc.run";
			HashMap<String, HashMap<String, String>> tfIdf_anc_apcData = read_dataFile(tfIdf_anc_apc);

			String tfidf_lnc_ltn1 = "./tfidf_lnc_ltn.run";
			HashMap<String, HashMap<String, String>> lnc_ltnData = read_dataFile(tfidf_lnc_ltn1);

			String tfidf_bnn_bnn1 = "./tfidf_bnn_bnn.run";
			HashMap<String, HashMap<String, String>> bnn_bnnData = read_dataFile(tfidf_bnn_bnn1);

			System.out
					.println("Correlation between lucene-default and anc_apc");
			calculateCorrelation(lucene_data, tfIdf_anc_apcData);

			System.out
					.println("Correlation between lucene-default and lnc_ltn");
			calculateCorrelation(lucene_data, lnc_ltnData);

			System.out
					.println("Correlation between lucene-default and bnn_bnn");
			calculateCorrelation(lucene_data, bnn_bnnData);

    }




    public static HashMap<String, HashMap<String, String>> read_dataFile(
            String file_name) {
        HashMap<String, HashMap<String, String>> query = new HashMap<String, HashMap<String, String>>();

        File f = new File(file_name);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            ArrayList<String> al = new ArrayList<>();
            String text = null;
            while ((text = br.readLine()) != null) {
                String queryId = text.split(" ")[0];
                String paraID = text.split(" ")[2];
                String rank = text.split(" ")[3];

                if (al.contains(queryId))
                    query.get(queryId).put(paraID, rank);
                else {
                    HashMap<String, String> docs = new HashMap<String, String>();
                    docs.put(paraID, rank);
                    query.put(queryId, docs);
                    al.add(queryId);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (br != null)
                br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return query;
    }

    public static void calculateCorrelation(
            HashMap<String, HashMap<String, String>> lucene_data,
            HashMap<String, HashMap<String, String>> TFIDF_data) {
        float SpearMan_rank_correlation = (float) 0.0;
        float d = 0, d_square = 0, rank_correlation = (float) 0.0;

        for (String q : lucene_data.keySet()) {
            HashMap<String, String> luceneRanks, customRanks;
            if (TFIDF_data.keySet().contains(q)) {
                luceneRanks = lucene_data.get(q);
                customRanks = TFIDF_data.get(q);
                int missingFile = 0;
                int n = luceneRanks.size();
                if (n == 1) {
                    n = 2;
                }
                for (String key : luceneRanks.keySet()) {
                    int num1 = Integer.parseInt(luceneRanks.get(key));
                    if (customRanks.containsKey(key)) {
                        int num2 = Integer.parseInt(customRanks.get(key));

                        d = Math.abs(num1 - num2);
                        d_square += (d * d);
                    } else {
                        missingFile++;

                        d = Math.abs(num1 - (n + missingFile));
                        d_square += (d * d);
                    }
                }

                rank_correlation = 1 - (6 * d_square / (n * n * n - n));

                SpearMan_rank_correlation += rank_correlation;
            }
        }
        System.out
                .println("\nSpearman Coefficient  between lucene-Default and TF_IDF data: "
                        + Math.abs(SpearMan_rank_correlation
                        / lucene_data.size()) + "\n");
    }


    public static HashMap<String, List<Rank>> rankResultMap(boolean useDefaultScore) throws ParseException, CborException, IOException {
        HashMap<String,List<Rank>> resultMap = new HashMap<>();

        List<Page> pageList = ReadData.getPageList();
        List<Paragraph> paragraphsList = ReadData.getParagraphList();

        Indexer indexer = new Indexer(useDefaultScore,indexPath,paragraphPath);

        indexer.rebuildIndexes(paragraphsList);

        for (Page page : pageList){
            System.out.println("searching for query: "+ page.getPageName());
            List<Rank> rankList = new ArrayList<>();
            String query = page.getPageName();

            SearchEngine se = new SearchEngine(useDefaultScore,indexPath);
            TopDocs topDocs = se.performSearch(query, 100);
            System.out.println("Result found: "+topDocs.totalHits);

            ScoreDoc[] hits = topDocs.scoreDocs;

            for (int i = 0; i < hits.length;i++){
                Document document = se.getDocument(hits[i].doc);
                Rank rank = new Rank();
                rank.setQueryId(page.getPageId());
                rank.setParagId(document.get("id"));
                rank.setRank(i+1);
                rank.setScore(hits[i].score);
                String methodTeamName = "Group7" + "-"+methdName;

                rank.setMethodTeamName(methodTeamName);
                rankList.add(rank);
            }

            if (!resultMap.containsKey(page.getPageId())){
                resultMap.put(page.getPageId(),rankList);
            }else {
                List<Rank> tmpRank = resultMap.get(page.getPageId());
                tmpRank.addAll(rankList);
                resultMap.put(page.getPageId(),tmpRank);
            }
            System.out.println("search done!    "+ "get rank list with size: " +rankList.size());

        }

        return  resultMap;


    }
}
