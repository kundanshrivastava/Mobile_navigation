package com.example.q4n9yzb1.demo_player;

/**
 * Created by q4N9YZB1 on 21-07-2015.
 */
public class rowItem {
    private String line_time;
    private  String line;
    public rowItem(String t,String line)
    {
        super();
        this.line=line;
        this.line_time=t;
    }
    public  rowItem()
    {
        super();
    }
    public void setLinedef(String line)
    {
        this.line=line;
        return;
    }
    public void setLine_time(String t)
    {
        this.line_time=t;
    }


    public String getLine_time()
    {
        return line_time;
    }
    public String getLine()
    {
        return line;
    }
}
