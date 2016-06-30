package com.taxiyab.Model;

/**
 * Created by MehrdadS on 6/23/2016.
 */
public class SpinnerModel {

    private  String CompanyName="";
    private  String Image="";
    private  String Url="";

    /*********** Set Methods ******************/
    public void setCompanyName(String CompanyName)
    {
        this.CompanyName = CompanyName;
    }

    public void setImage(String Image)
    {
        this.Image = Image;
    }

    public void setUrl(String Url)
    {
        this.Url = Url;
    }

    /*********** Get Methods ****************/
    public String getCompanyName()
    {
        return this.CompanyName;
    }

    public String toString()
    {
        return this.CompanyName;
    }

    public String getUrl()
    {
        return this.Url;
    }
}