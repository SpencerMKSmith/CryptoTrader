package com.smks.cyclictrading.hitbtctypes;

public class Bid
{
    private String price;
    private String size;

    public Double getPrice ()
    {
        return Double.parseDouble(this.price);
    }

    public Double getSize ()
    {
        return Double.parseDouble(this.size);
    }

	@Override
	public String toString() {
		return "Bids [price=" + price + ", size=" + size + "]";
	}

    
}
