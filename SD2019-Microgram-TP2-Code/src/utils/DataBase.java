package utils;


import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import microgram.api.Post;
import microgram.api.Profile;

public class DataBase {

	private static final String DB_NAME = "SD_TP2";

	private static final String DB_TABLE_PROFILES = "Profiles";
	private static final String DB_TABLE_FOLLOWINGS = "Followings";

	private static final String DB_TABLE_POSTS = "Posts";
	private static final String DB_TABLE_LIKES = "Likes";

	private static MongoClient mongo;
	public static MongoDatabase dataBase;

	public static final String USERID = "userId";
	public static final String POSTID = "postId";
	public static final String ID1 = "id1";
	public static final String ID2 = "id2";
	public static final String OWNERID = "ownerId";


	private DataBase() {
		//Inicialização da base de dados
		mongo = new MongoClient("localhost");
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries( MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()) );
		dataBase = mongo.getDatabase(DB_NAME).withCodecRegistry(codecRegistry);
	}

	public static DataBase init() {
		//Metodo estatico
		return new DataBase();
	}

	public MongoCollection<Post> getPosts() {
		MongoCollection<Post> posts = dataBase.getCollection(DB_TABLE_POSTS, Post.class);
		//Chave primaria (postId)
		posts.createIndex(Indexes.ascending(POSTID), new IndexOptions().unique(true));
		return posts;
	}

	public MongoCollection<Profile> getProfiles() {
		MongoCollection<Profile> profiles = dataBase.getCollection(DB_TABLE_PROFILES, Profile.class);
		//Chave primaria (userId)
		profiles.createIndex(Indexes.ascending(USERID), new IndexOptions().unique(true));
		return profiles;
	}

	public MongoCollection<Pair> getLikes() {
		MongoCollection<Pair> likes = dataBase.getCollection(DB_TABLE_LIKES, Pair.class);
		//Chaves primarias (userId1, postId) (userId1 tem like no postId)
		likes.createIndex(Indexes.ascending(ID1,ID2), new IndexOptions().unique(true));
		return likes;
	}

	public MongoCollection<Pair> getFollowings() {
		MongoCollection<Pair> followings = dataBase.getCollection(DB_TABLE_FOLLOWINGS, Pair.class);
		//Chaves primarias (userId1, userId2) (userId1 segue userId2)
		followings.createIndex(Indexes.ascending(ID1,ID2), new IndexOptions().unique(true));
		//Facilitar a pequisa ao contrário
		followings.createIndex(Indexes.hashed(ID2));
		return followings;
	}

}
