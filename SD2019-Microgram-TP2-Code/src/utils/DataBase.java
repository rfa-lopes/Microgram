package utils;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import microgram.api.Profile;

public class DataBase {

	private static final String DB_NAME = "SD_TP2";
	private static final String DB_TABLE_PROFILES = "Profiles";
	private static final String DB_TABLE_FOLLOWERS = "Followers";
	private static final String DB_TABLE_FOLLOWINGS = "Followings";

	private static MongoClient mongo;

	public static MongoDatabase dataBase;

	public MongoCollection<Profile> profiles;
	public MongoCollection<Pair> followers;
	public MongoCollection<Pair> followings;

	private DataBase() {
		mongo = new MongoClient( "localhost" );
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		dataBase = mongo.getDatabase(DB_NAME).withCodecRegistry(codecRegistry);

		profiles = dataBase.getCollection(DB_TABLE_PROFILES, Profile.class);
		followers = dataBase.getCollection(DB_TABLE_FOLLOWERS, Pair.class);
		followings = dataBase.getCollection(DB_TABLE_FOLLOWINGS, Pair.class);

		profiles.createIndex(Indexes.ascending("userId"), new IndexOptions().unique(true));
		followers.createIndex(Indexes.ascending("id1","id2"), new IndexOptions().unique(true));
		followings.createIndex(Indexes.ascending("id1","id2"), new IndexOptions().unique(true));
	}

	public static DataBase init() {
		return new DataBase();
	}


}
