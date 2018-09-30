package main;

import co.nstant.in.cbor.CborException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.omg.CORBA.BAD_CONTEXT;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class NDCG {

    //DCG top 20

    // 1 get top 20 in rank list and get there relevant value
    // 2 calculate log2(i+1),where i is the rank id
    //3. calculate dcg
    //4. sum every document's dcg togeter,


    /*


    the reason why my results is little different with trec-eval's result:
        in trec-eval, it is baed on Jarvelin and Kekalainen (ACM ToIS v. 20, pp. 422-446, 2002),
        the grade function is like 1=3.5,2=9.0,4=7.0, they will give gains 3.5, 9.0, 3.0, 7.0 for relevance levels 1,2,3,4 respectively
           Gains are allowed to be 0 or negative,

        in my calculation, the grade is binary value only based on doc relevance value which is either 0 and 1. No negative, no other value.

        Moreover, for some queries, their retreived docs number is less than 20. If it is less than 20, I select total of them to calculate both DCG and iDCG.
        for example, for query 1, it retreived 16 docs. I calculate the DCG and iDCG on top 16 files to make them consistent.
    */


    public static void main(String args[]) throws ParseException, IOException, CborException {
        String defaulScoreInput = args[0];

        boolean defaultScore = Boolean.valueOf(defaulScoreInput);

        HashMap<String, List<Rank>> rankMap = Main.rankResultMap(defaultScore);

        TreeMap<String,List<String>> relevantMap = ReadData.getRelevant();

        HashMap<String, Double> NDCG_map = getNDCG_map(rankMap,relevantMap);
        writeResult(NDCG_map,defaultScore);
//        HashMap<String,Double> ap_map = getApMap(rankMap,relevantMap);
//        double map = 0;
//        double size = 0;
//
//        for (String queryId :ap_map.keySet() ){
//
//                if (ap_map.get(queryId) == Double.NaN){
//                    continue;
//                }
//                map += ap_map.get(queryId);
//                size++;
//
//            System.out.println(queryId+"  "+ ap_map.get(queryId));
//        }
//
//
//        //writeResult(ap_map,defaultScore);
//        System.out.println("======= map "+map/size);
//        System.out.println("ap size "+ap_map.size());
    }

    public static HashMap<String, Double> getNDCG_map(HashMap<String, List<Rank>> rankMap, TreeMap<String,List<String>> relevantMap){
        HashMap<String, Double> NDCGmap = new HashMap<>();
        double DCG = 0;
        double iDCG = 0;
        double NDCG = 0;
        for (String queryId : rankMap.keySet()){
            //for each query
            System.out.print("query : ===> "+ queryId + "   ");
            if (rankMap.get(queryId) != null){
                List<Rank> list = rankMap.get(queryId);
                List<String> relevantDocList = relevantMap.get(queryId);
                int top = list.size() < 20 ? list.size() : 20;
                System.out.print("top is "+top+"   ");
                for (int i = 0; i < top ; i++){
                    double base = Math.log(i+2)/Math.log(2);
                    Rank rank = list.get(i);

                    iDCG += 1/ base;
                    if (relevantDocList.contains(rank.getParagId())){
                        System.out.println("relevance value is 1");

                        DCG += (1/base);
                    }else {
                        System.out.println("relevance value is 0");
                    }
                }
                System.out.println("");
            }

            NDCG = DCG / iDCG;

            NDCGmap.put(queryId,NDCG);
        }

        return  NDCGmap;
    }


    public static HashMap<String,Double> getApMap(HashMap<String,List<Rank>> rankMap,TreeMap<String,List<String>> relevantMap){
        HashMap<String,Double> ap_map = new HashMap<>();

        for (String queryId : rankMap.keySet()){

            List<Rank> list = rankMap.get(queryId);
            List<String> relevantDocList = relevantMap.get(queryId);

            double sum = 0;
            double tp = 0;
            double ap = 0;
            for (Rank rank : list){

                if (relevantDocList.contains(rank.getParagId())&&rank.getRank()!= 0){
                    tp++;
                    sum += tp/(double) rank.getRank();
                }
            }

            ap = sum/tp;
            if (Double.isNaN(ap)){
                continue;
            }else {
                ap_map.put(queryId,ap);
            }

        }
        return  ap_map;
    }


    public static void writeResult(HashMap<String, Double> resultMap,boolean useDefaultScore){
        if (resultMap.isEmpty()){
            System.out.println("rank map is empty");
            return;
        }

        String output = "nDCG";

        if (useDefaultScore){
            output = output + "-defaultScoreFunc.txt";
        }else{
            output = output + "-customScoreFunc.txt";
        }

        String path = "./"+output;

        BufferedWriter bufferWriter = null;
        FileWriter fileWriter = null;

        try {

            fileWriter = new FileWriter(path);
            bufferWriter = new BufferedWriter(fileWriter);

            for (String queryId: resultMap.keySet()){

                String line = String.format("%-40s %20s \r\n", queryId, resultMap.get(queryId));
                bufferWriter.write(line);

                bufferWriter.newLine();
            }

            double meanCDNG = 0;

            for (String queryID : resultMap.keySet()){
                meanCDNG += resultMap.get(queryID);
            }

            meanCDNG = meanCDNG/resultMap.size();

            String line = String.format("final ndcg is "+meanCDNG);
            bufferWriter.write(line);

            System.out.println("output file wrote to file: "+path);
        }catch (IOException e){
            e.printStackTrace();
        }finally {

            try {
                if (bufferWriter != null){
                    bufferWriter.close();
                }
                if (fileWriter!=null){
                    fileWriter.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }

        }


    }
}
