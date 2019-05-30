package microgram.impl.mongo;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.ErrorCode.CONFLICT;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import utils.Pair;

public class MongoProfiles implements Profiles {
	
	private static final String DB_NAME = "SDTP2";

	private static final String DB_TABLE_PROFILES = "Profiles";
	private static final String DB_TABLE_FOLLOWERS = "Followers";
	private static final String DB_TABLE_FOLLOWINGS = "Followings";

	private static MongoClient mongo;

	public static MongoDatabase dataBase;

	public MongoCollection<Profile> profiles;
	public MongoCollection<Pair> followers;
	public MongoCollection<Pair> followings;

	public static final String USERID = "userId";
	public static final String POSTID = "postId";
	public static final String ID1 = "id1";
	public static final String ID2 = "id2";

	public MongoProfiles() {
		mongo = new MongoClient("mongo1");
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		dataBase = mongo.getDatabase(DB_NAME).withCodecRegistry(codecRegistry);
		
		profiles = dataBase.getCollection(DB_TABLE_PROFILES, Profile.class);
//		followers = dataBase.getCollection(DB_TABLE_FOLLOWERS, Pair.class);
//		followings = dataBase.getCollection(DB_TABLE_FOLLOWINGS, Pair.class);

		profiles.createIndex(Indexes.ascending(USERID), new IndexOptions().unique(true));
//		followers.createIndex(Indexes.ascending(ID1,ID2), new IndexOptions().unique(true));
//		followings.createIndex(Indexes.ascending(ID1,ID2), new IndexOptions().unique(true));
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		String p = "^" + userId;
		FindIterable<Profile> res = null;
		res = profiles.find(Filters.regex("userId", p));
		if(res.first() == null)
			return Result.error(NOT_FOUND);
		return ok(res.first());
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		try {
			//profiles.insertOne(profile);
			System.out.println("profiles.insertOne(profile)");
			return ok();
		} catch( MongoWriteException x ) {
			return Result.error( CONFLICT );
		}
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		Profile res = profiles.find(Filters.eq(USERID, userId)).first();
		if(res == null)
			return error(NOT_FOUND);
		//TODO realizar a operação de eliminar
		return null;
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {

		Profile u1 = profiles.find(Filters.eq(USERID, userId1)).first();
		Profile u2 = profiles.find(Filters.eq(USERID, userId2)).first();

		//Profiles nao existem
		if(u1 == null || u2 == null)
			return error(NOT_FOUND);

		try {
			//user1 quer seguir user2
			if( isFollowing ) {
				followers.insertOne( new Pair(userId2, userId1) );
				followings.insertOne( new Pair(userId1, userId2) );
			}
			//user1 quer deixar de seguir user2
			else {
				followers.deleteOne(Filters.and(Filters.eq(ID1, userId2), Filters.eq(ID2, userId1) ));
				followings.deleteOne(Filters.and(Filters.eq(ID1, userId1), Filters.eq(ID2, userId2) ));
			}
		} catch( MongoWriteException x ) {
			//Caso queira seguir alguem que ja segue
			//Ou deixar de seguir alguem que ja nao segue
			return error( CONFLICT );
		}

		return ok();
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		Profile u1 = profiles.find(Filters.eq(USERID, userId1)).first();
		Profile u2 = profiles.find(Filters.eq(USERID, userId2)).first();

		//Profiles nao existem
		if(u1 == null || u2 == null)
			return error(NOT_FOUND);

		//Se userId1 seguir userId2 vai returnar um par
		//Se nao seguir, retorna null (porque nao existe na tabela)
		Pair res = followings.find(Filters.and(Filters.eq(ID1, userId1), Filters.eq(ID2, userId2))).first();
		return ok(res != null);
	}

}