package main;

import co.nstant.in.cbor.CborException;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import javax.print.Doc;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {


    private  static String methdName = "";
    private  static  String indexPath = "";
    private  static  boolean useDefaultScore = true;
    private  static  String paragraphPath = "";
    private  static  String pagePath = "";
    private  static  String qrelPath = "";

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
                System.out.println(page.toString());

            }

        return pageList;
    }

//    private void indexAllParagraphs() throws CborException, IOException {
//        Directory indexdir = FSDirectory.open((new File(indexPath))
//                .toPath());
//        IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
//        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
//        IndexWriter iw = new IndexWriter(indexdir, conf);
//        for (Data.Paragraph p : DeserializeData
//                .iterableParagraphs(new FileInputStream(new File(paragraphPath)))) {
//            this.indexPara(iw, p);
//        }
//        iw.close();
//
//
//    }
//
//    private void indexPara(IndexWriter iw, Data.Paragraph para)
//            throws IOException {
//        Document paradoc = new Document();
//        paradoc.add(new StringField("paraid", para.getParaId(), Field.Store.YES));
//        paradoc.add(new TextField("parabody", para.getTextOnly(),
//                Field.Store.YES));
//        FieldType indexType = new FieldType();
//        indexType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
//        indexType.setStored(true);
//        indexType.setStoreTermVectors(true);
//
//        paradoc.add(new Field("content", para.getTextOnly(), indexType));
//
//        iw.addDocument(paradoc);
//    }

    public static void main(String[] args) throws IOException, ParseException, CborException {
        System.setProperty("file.encoding", "UTF-8");
//        String methdName = "";
        //read data

        //
        ///Users/xinliu/Documents/UNH/18Fall/cs853/index /Users/xinliu/Documents/UNH/18Fall/cs853/test200/test200-train/train.pages.cbor-paragraphs.cbor
        String defualtScore = args[0];
        //indexPath = args[1];
        //paragraphPath = args[2];
        //pagePath = args[3];
        //qrelPath = args[4];


        indexPath = "/Users/xinliu/Documents/UNH/18Fall/cs853/index";
        paragraphPath = "/Users/xinliu/Documents/UNH/18Fall/cs853/test200/test200-train/train.pages.cbor-paragraphs.cbor";
        //pagePath = "/Users/xinliu/Documents/UNH/18Fall/cs853/test200/test200-train/train.pages.cbor-article.qrels";
        pagePath = "/Users/xinliu/Documents/UNH/18Fall/cs853/test200/test200-train/train.pages.cbor-outlines.cbor";
        qrelPath = "/Users/xinliu/Documents/UNH/18Fall/cs853/test200/test200-train/train.pages.cbor-article.qrels";



        useDefaultScore = Boolean.valueOf(defualtScore);
        if (useDefaultScore){
            methdName = "default";
            System.out.println("using lucene default score function");
        }else{
            methdName = "custome";
            System.out.println("using lucene customed score function");
        }


        Main main = new Main();
        Indexer id = new Indexer(useDefaultScore,indexPath,paragraphPath);
        id.indexParagraph();
//        main.indexAllParagraphs();

        //List<Data.Page> pagelist = getPageListFromPath(pagePath);

        ReadData readData = new ReadData(paragraphPath,pagePath,qrelPath);
        //List<Paragraph> paragraphsList = readData.getParagraphList();
//        List<Paragraph> paragraphsList = ReadData.getParagraphList();

        List<Page> pageList = readData.getPageList();
//
//        List<Rank> rankList = getRankList(paragraphsList,pageList,useDefaultScore,methdName,indexPath);

//        writeResult(rankList,useDefaultScore);

        //List<Data.Page> pageList = getPageListFromPath(pagePath);
        //System.out.println(pageList.size());
//
//        TFIDF_anc_apc tfidf_anc_apc = new TFIDF_anc_apc();
//        tfidf_anc_apc.retrieveAllAncApcResults(pagelist, "/Users/xinliu/Documents/GitHub/programmingAssignment3Group7"
//                + "/tfidf_anc_apc.run");
    }





//    public static List<Rank> getRankList(List<Paragraph> paragraphList,List<Page> pageList, boolean useDefaultScore, String methodName,String indexPath) throws IOException, ParseException, CborException {
//        List<Rank> rankList = new ArrayList<>();
//        Indexer indexer = new Indexer(useDefaultScore,indexPath);
//
//        indexer.rebuildIndexes(paragraphList);
//       for (Page page : pageList){
//           System.out.println("searching for query: "+ page.getPageName());
//
//           String query = page.getPageName();
//
//           SearchEngine se = new SearchEngine(useDefaultScore,indexPath);
//           TopDocs topDocs = se.performSearch(query, 100);
//
//
//            ScoreDoc[] hits = topDocs.scoreDocs;
//           System.out.println("================== hits size: "+ hits.length);
//            for (int i = 0; i < hits.length;i++){
//                Document document = se.getDocument(hits[i].doc);
//
//                Rank rank = new Rank();
//                rank.setQueryId(page.getPageId());
//                rank.setParagId(document.get("id"));
//                rank.setRank(i+1);
//                rank.setScore(hits[i].score);
//                String methodTeamName = "Group7" + "-"+methodName;
//
//                rank.setMethodTeamName(methodTeamName);
//                rankList.add(rank);
//            }
//
//
//       }
//        System.out.println("search done!    "+ "get rank list with size: " +rankList.size());
//        return rankList;
//    }


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


//    public static void writeResult(List<Rank> rankList,boolean useDefaultScore){
//        if (rankList.isEmpty()){
//            System.out.println("rank list is empty");
//            return;
//        }
//
//        String output = "rankResult";
//
//        if (useDefaultScore){
//            output = output + "-defaultScoreFunc.txt";
//        }else{
//            output = output + "-customScoreFunc.txt";
//        }
//
//        String path = "./"+output;
//
//        BufferedWriter bufferWriter = null;
//        FileWriter fileWriter = null;
//
//        try {
//
//            fileWriter = new FileWriter(path);
//            bufferWriter = new BufferedWriter(fileWriter);
//
//            for (Rank rank : rankList){
//                String line = rank.getGueryId() + " "+"Q0"+" "+rank.getParagId()+" "+rank.getRank()+" "
//                        +rank.getScore()+" "+rank.getMethodTeamName();
//                bufferWriter.write(line);
//
//                bufferWriter.newLine();
//            }
//
//            System.out.println("output file wrote to file: "+path);
//        }catch (IOException e){
//            e.printStackTrace();
//        }finally {
//
//            try {
//                if (bufferWriter != null){
//                    bufferWriter.close();
//                }
//                if (fileWriter!=null){
//                    fileWriter.close();
//                }
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//
//        }
//
//
//    }
}
