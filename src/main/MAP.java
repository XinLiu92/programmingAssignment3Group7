package main;

import co.nstant.in.cbor.CborException;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class MAP {

    public static void main(String args[]) throws ParseException, IOException, CborException {
        String defaulScoreInput = args[0];

        boolean defaultScore = Boolean.valueOf(defaulScoreInput);

        HashMap<String, List<Rank>> rankMap = Main.rankResultMap(defaultScore);

        TreeMap<String,List<String>> relevantMap = ReadData.getRelevant();

        HashMap<String,Double> ap_map = getApMap(rankMap,relevantMap);

        writeResult(ap_map,defaultScore);

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

        String output = "Ap-MAP-";

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

            double map = 0;
            double size = 0;

            for (String queryId :resultMap.keySet() ){

                if (resultMap.get(queryId) == Double.NaN){
                    continue;
                }
                map += resultMap.get(queryId);
                size++;


            }
            String line = String.format("MAP "+ map/size);
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
