package main;

public class Rank {
    private String queryId;
    private  String paragId;
    private int rank;
    private float score;
    private String methodTeamName;

    public void setQueryId(String id){
        this.queryId = id;
    }

    public  String getGueryId(){
        return this.queryId;
    }

    public void setParagId(String id){
        this.paragId = id;
    }

    public String getParagId(){
        return this.paragId;
    }


    public void setRank(int rank){
        this.rank = rank;
    }

    public int getRank(){
        return this.rank;
    }

    public void setScore(float score){
        this.score = score;
    }

    public float getScore(){
        return this.score;
    }



    public void setMethodTeamName(String teamName){
        this.methodTeamName = teamName;
    }

    public String getMethodTeamName(){
        return this.methodTeamName;
    }


}
