package com.smks.cyclictrading.hitbtctypes;

public class Ask
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
		return "Asks [price=" + price + ", size=" + size + "]";
	}
}
