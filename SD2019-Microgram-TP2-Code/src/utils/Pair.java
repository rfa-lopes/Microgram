package utils;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

public class Pair {

	private final int id1;
	private final int id2;
	
	@BsonCreator
	public Pair(@BsonProperty("id1")int id1, @BsonProperty("id2")int id2) {
		this.id1 = id1;
		this.id2 = id2;
	}
	
	public int getId1() {
		return id1;
	}

	public int getId2() {
		return id2;
	}

}
