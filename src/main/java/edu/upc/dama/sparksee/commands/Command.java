package edu.upc.dama.sparksee.commands;

public interface Command {
	
	public void execute() throws Exception;
	
	public String getName();

}
