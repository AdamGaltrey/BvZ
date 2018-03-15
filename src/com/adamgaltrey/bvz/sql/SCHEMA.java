package com.adamgaltrey.bvz.sql;

public enum SCHEMA {
	
	MySQL,
	SQLite;
	
	@Override
	public String toString(){
		return this.toString().toUpperCase();
	}

}
