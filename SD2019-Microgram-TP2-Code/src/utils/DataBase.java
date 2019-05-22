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
	private static final String DB_TABLE_FOLLOWERS = "Followers";
	private static final String DB_TABLE_FOLLOWINGS = "Followings";

	private static final String DB_TABLE_POSTS = "Posts";
	private static final String DB_TABLE_LIKES = "Likes";
	private static final String DB_TABLE_USERPOSTS = "UserPosts";

	private static MongoClient mongo;

	public static MongoDatabase dataBase;

	public MongoCollection<Profile> profiles;
	public MongoCollection<Pair> followers;
	public MongoCollection<Pair> followings;

	public MongoCollection<Post> posts;
	public MongoCollection<Pair> likes;
	public MongoCollection<Pair> userPosts;

	public static final String USERID = "userId";
	public static final String POSTID = "postId";
	public static final String ID1 = "id1";
	public static final String ID2 = "id2";


	private DataBase() {
		mongo = new MongoClient( "localhost" );
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		dataBase = mongo.getDatabase(DB_NAME).withCodecRegistry(codecRegistry);

		profiles = dataBase.getCollection(DB_TABLE_PROFILES, Profile.class);
		followers = dataBase.getCollection(DB_TABLE_FOLLOWERS, Pair.class);
		followings = dataBase.getCollection(DB_TABLE_FOLLOWINGS, Pair.class);

		posts = dataBase.getCollection(DB_TABLE_POSTS, Post.class);
		likes = dataBase.getCollection(DB_TABLE_LIKES, Pair.class);
		userPosts = dataBase.getCollection(DB_TABLE_USERPOSTS, Pair.class);
		
		//--------------------------------------------------------

		profiles.createIndex(Indexes.ascending(USERID), new IndexOptions().unique(true));
		followers.createIndex(Indexes.ascending(ID1,ID2), new IndexOptions().unique(true));
		followings.createIndex(Indexes.ascending(ID1,ID2), new IndexOptions().unique(true));

		posts.createIndex(Indexes.ascending(POSTID), new IndexOptions().unique(true));
		likes.createIndex(Indexes.ascending(ID1,ID2), new IndexOptions().unique(true));
		userPosts.createIndex(Indexes.ascending(ID1,ID2), new IndexOptions().unique(true));
	}

	public static DataBase init() {
		return new DataBase();
	}


}
