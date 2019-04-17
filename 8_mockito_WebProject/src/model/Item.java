package com.example.demo.model;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
@Entity
public class Item {
	@Id
	private int id;
	private String name;
	private int price;
	private int quantity;

	@Transient
	private int value;
	
	//We have to define "no-argument" constructor as per JPA standard else we will get error while hitting services.
	protected Item() {
	}
	
	public Item(int id, String name, int price, int quantity)
	{
		this.id = id;
		this.name = name;
		this.price = price;
		this.quantity = quantity;
	}
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public int getPrice() {
		return price;
	}
	public int getQuantity() {
		return quantity;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String toString() {
		return String.format("Item[%d, %s, %d, %d]", id, name, price, quantity);
	}
}