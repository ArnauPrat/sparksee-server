package edu.upc.dama.sparksee;

import static spark.Spark.*;

public class SparkseeServer {
	public static void main(String[] args) {
		get("/hello", (req, res) -> "Hello World");
	}
}
