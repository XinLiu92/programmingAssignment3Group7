package main;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ReadData {

    private static String paragPath = "";
    private static String pagePath = "";
    private static String qrelPath = "";


    public ReadData(){

    }

    public ReadData(String paragraphPath,String pagedir, String qreldir){
        this.paragPath = paragraphPath;
        this.pagePath = pagedir;
        this.qrelPath = qreldir;
    }


    public static void main(String args[]){
//        if (args.length < 1){
//            System.out.println("command line argument : FileDirectory");
//        }

//        String filePath = args[0];

        paragPath = args[0];
        pagePath = args[1];
        qrelPath = args[2];

    }


    public static List<Paragraph> getParagraphList() {
        List<Paragraph> paragraphsList = new ArrayList<>();
//        String fileDir = "./test200-train/train.pages.cbor-paragraphs.cbor";
        System.out.println(pagePath);
        File file = new File(paragPath);

        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);

        } catch (Exception e) {
            e.printStackTrace();
        }


        if (stream == null){
            System.out.println("file input stream created failed");
        }

        try {
            for (Data.Paragraph parag : DeserializeData.iterableParagraphs(stream)){
                Paragraph p = new Paragraph(parag.getParaId(),parag.getTextOnly());
                paragraphsList.add(p);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        return  paragraphsList;
    }

    public static List<Page> getPageList(){
        List<Page> pageList = new ArrayList<>();

//        String fileDir = "./test200-train/train.pages.cbor-outlines.cbor";

        File file = new File(pagePath);

        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);

        } catch (Exception e) {
            e.printStackTrace();
        }


        if (stream == null){
            System.out.println("file input stream created failed");
        }

        try {
            for (Data.Page page : DeserializeData.iterableAnnotations(stream)){
                Page p = new Page(page.getPageId(),page.getPageName());
                pageList.add(p);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return  pageList;
    }

    public static TreeMap<String, List<String>> getRelevant(){
        TreeMap<String,List<String>> res = new TreeMap<>();

//        String path = "./test200-train/train.pages.cbor-article.qrels";
        BufferedReader bufferedReader = null;

        File file = new File(qrelPath);
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            bufferedReader = new BufferedReader(new InputStreamReader(stream));
            String line = bufferedReader.readLine();

            while (line != null){
                String[] strList = line.split("\\s");
                String queryId = strList[0];
                String docId = strList[2];
                int isRelevant = Integer.valueOf(strList[3]);


                if (res.containsKey(queryId)){
                    if (isRelevant > 0){
                        List<String> relevantDoc = res.get(queryId);
                        relevantDoc.add(docId);
                        res.put(queryId,relevantDoc);
                    }
                }else{
                    List<String> releventDoc = new ArrayList<>();

                    if (isRelevant > 0){
                        releventDoc.add(docId);

                    }

                    res.put(queryId,releventDoc);
                }

                line = bufferedReader.readLine();

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
        }


        return res;
    }

}
