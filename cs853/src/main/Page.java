package main;

public class Page {
    private String pageId;
    private String pageName;

    public Page(){

    }

    public Page(String pageId,String pageName){
        this.pageId = pageId;
        this.pageName = pageName;
    }

    public void setPageId(String pageId){
        this.pageId = pageId;
    }

    public String getPageId() {
        return pageId;
    }

    public void  setPageName(String pageName){
        this.pageName = pageName;
    }

    public String getPageName(){
        return  pageName;
    }
}
