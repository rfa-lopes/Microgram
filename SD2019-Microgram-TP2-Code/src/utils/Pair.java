package utils;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

public class Pair {

	private final String id1;
	private final String id2;
	
	@BsonCreator
	public Pair(@BsonProperty("id1")String id1, @BsonProperty("id2")String id2) {
		this.id1 = id1;
		this.id2 = id2;
	}
	
	public String getId1() {
		return id1;
	}

	public String getId2() {
		return id2;
	}

}
