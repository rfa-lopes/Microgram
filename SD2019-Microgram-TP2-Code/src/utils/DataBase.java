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

	public static final String USERID = "userId";
	public static final String POSTID = "postId";
	public static final String ID1 = "id1";
	public static final String ID2 = "id2";


	private DataBase() {
		mongo = new MongoClient( "localhost" );
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries( MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()) );
		dataBase = mongo.getDatabase(DB_NAME).withCodecRegistry(codecRegistry);
	}

	public static DataBase init() {
		return new DataBase();
	}

	public MongoCollection<Post> getPosts() {
		MongoCollection<Post> posts = dataBase.getCollection(DB_TABLE_POSTS, Post.class);
		posts.createIndex(Indexes.ascending(POSTID), new IndexOptions().unique(true));
		return posts;
	}

	public MongoCollection<Pair> getLikes() {
		MongoCollection<Pair> likes = dataBase.getCollection(DB_TABLE_LIKES, Pair.class);
		likes.createIndex(Indexes.ascending(ID1,ID2), new IndexOptions().unique(true));
		return likes;
	}


	public MongoCollection<Pair> getUserPosts() {
		MongoCollection<Pair> userPosts = dataBase.getCollection(DB_TABLE_USERPOSTS, Pair.class);
		userPosts.createIndex(Indexes.ascending(ID1,ID2), new IndexOptions().unique(true));
		return userPosts;
	}

	public MongoCollection<Profile> getProfiles() {
		MongoCollection<Profile> profiles = dataBase.getCollection(DB_TABLE_PROFILES, Profile.class);
		profiles.createIndex(Indexes.ascending(USERID), new IndexOptions().unique(true));
		return profiles;
	}

	public MongoCollection<Pair> getFollowers() {
		MongoCollection<Pair> followers = dataBase.getCollection(DB_TABLE_FOLLOWERS, Pair.class);
		followers.createIndex(Indexes.ascending(ID1,ID2), new IndexOptions().unique(true));
		return followers;
	}

	public MongoCollection<Pair> getFollowings() {
		MongoCollection<Pair> followings = dataBase.getCollection(DB_TABLE_FOLLOWINGS, Pair.class);
		followings.createIndex(Indexes.ascending(ID1,ID2), new IndexOptions().unique(true));
		return followings;
	}




}
