package org.mike.logicbomb.components;

public class Truple {
	private int a,b,c;

	public Truple(int a, int b, int c){
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public int a(){
		return a;
	}

	public int b(){
		return b;
	}

	public int c(){
		return c;
	}

	public void setA(int v){
		this.a = v;
	}

	public void setB(int v){
		this.b = v;
	}

	public void setC(int v){
		this.c = v;
	}

	public void incA(){
		this.a ++;
	}

	public void incB(){
		this.b ++;
	}

	public void incC(){
		this.c ++;
	}
}