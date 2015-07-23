package com.example.q4n9yzb1.demo_player;

/**
 * Created by q4N9YZB1 on 21-07-2015.
 */
public class ListModel {
    private  String textLine="";
    private  String timeStamp="";


    /*********** Set Methods ******************/

    public void setTextLine(String textLine)
    {
        this.textLine = textLine;
    }

    public void setTimeStamp(String timeStamp)
    {
        this.timeStamp=timeStamp;
    }

    /*********** Get Methods ****************/

    public String getTextLine()
    {
        return this.textLine;
    }

    public String getTimeStamp()
    {
        return this.timeStamp;
    }

}

