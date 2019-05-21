package microgram.impl.mongo;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.ErrorCode.CONFLICT;

import java.util.List;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
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

	private static final String DB_NAME = "Projeto_2_SD";
	private static final String DB_TABLE_PRO = "Profiles";
	private static final String DB_TABLE_FOL = "Followers";
	private static final String DB_TABLE_FOW = "Followers";


	private MongoClient mongo;
	private MongoDatabase dbSistem;
	private MongoCollection<Profile> profiles;
	private MongoCollection<Pair> followers;
	private MongoCollection<Pair> followings;


	public MongoProfiles() {
		mongo = new MongoClient( "localhost" );
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		dbSistem = mongo.getDatabase(DB_NAME).withCodecRegistry(codecRegistry);
		profiles = dbSistem.getCollection(DB_TABLE_PRO, Profile.class);
		followers = dbSistem.getCollection(DB_TABLE_FOL, Pair.class);
		followings = dbSistem.getCollection(DB_TABLE_FOW, Pair.class);
		addIndexes();
	}

	private void addIndexes() {
		profiles.createIndex(Indexes.ascending("userId"), new IndexOptions().unique(true));
		followers.createIndex(Indexes.ascending("id1","id2"), new IndexOptions().unique(true));
		followings.createIndex(Indexes.ascending("id1","id2"), new IndexOptions().unique(true));
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		Profile res = profiles.find(Filters.eq("userId", userId)).first();
		if(res == null)
			return error(NOT_FOUND);
		return ok(res);
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		try {
			profiles.insertOne(profile);
			return ok();
		} catch( MongoWriteException x ) {
			return error( CONFLICT );
		}
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		// TODO Auto-generated method stub
		return null;
	}

}