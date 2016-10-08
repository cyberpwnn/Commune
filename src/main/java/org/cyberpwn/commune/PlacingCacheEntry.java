package org.cyberpwn.commune;

public class PlacingCacheEntry
{
    private String image;
    private boolean fastsend;
    
    public PlacingCacheEntry(String image, boolean fastsend)
    {
        this.image = image;
        this.fastsend = fastsend;
    }
    
    public String getImage()
    {
        return image;
    }
    
    public boolean isFastSend()
    {
        return fastsend;
    }
}
