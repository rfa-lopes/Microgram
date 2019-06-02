package microgram.impl.mongo;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.ErrorCode.CONFLICT;

import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import utils.DataBase;
import utils.Pair;

public class MongoProfiles implements Profiles {

	private MongoCollection<Profile> profiles;
	private MongoCollection<Pair> followers;
	private MongoCollection<Pair> followings;
	private MongoCollection<Post> posts;
	private MongoCollection<Pair> likes;
	static MongoProfiles mProfiles;

	public MongoProfiles() {
		DataBase db = DataBase.init();
		profiles = db.getProfiles();
		followers = db.getFollowers();
		followings = db.getFollowings();
		posts = db.getPosts();
		likes = db.getLikes();
		mProfiles = this;
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		Profile res = profiles.find(Filters.eq(DataBase.USERID, userId)).first();
		if(res == null)
			return error(NOT_FOUND);

		//Atualizar as estatisticas
		res.setFollowers((int)followers.countDocuments(Filters.eq(DataBase.ID1, userId)));
		res.setFollowing((int)followings.countDocuments(Filters.eq(DataBase.ID1, userId)));
		res.setPosts( (int)posts.countDocuments(Filters.eq("ownerId", userId)) );

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

	public Result<Void> updateProfile(Profile profile) {
		try {
			profiles.updateOne(Filters.eq(DataBase.USERID, profile.getUserId()), 
					new Document("$set", profile));
			return ok();
		} catch( MongoWriteException x ) {
			return error( CONFLICT );
		}
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		Bson uf = Filters.eq(DataBase.USERID, userId);
		Profile res = profiles.find(uf).first();
		if(res == null)
			return error(NOT_FOUND);

		//Fazer delete na tabela Profiles (o profile)
		profiles.deleteOne(uf);

		//Fazer delete na tabela Posts (os posts do profile)
		posts.deleteMany(Filters.eq("ownerId", userId));

		//Fazer delete na tabela likes (os likes do profile)
		likes.deleteMany(Filters.eq(DataBase.USERID, userId));

		//Fazer delete na tabela followers (os followers do user / os que o user faz follow)
		followers.deleteMany(uf);
		followers.deleteMany(Filters.eq(DataBase.ID2, userId));

		//Fazer delete na tabela followings (os followings do user / os que o user faz following)
		followings.deleteMany(uf);
		followings.deleteMany(Filters.eq(DataBase.ID2, userId));

		return ok();
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		String p = "^" + prefix;
		FindIterable<Profile> found = profiles.find(Filters.regex(DataBase.USERID, p));

		List<Profile> res = new LinkedList<Profile>();
		for(Profile pro : found)
			res.add(pro);
		return ok(res);
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		Profile u1 = profiles.find(Filters.eq(DataBase.USERID, userId1)).first();
		Profile u2 = profiles.find(Filters.eq(DataBase.USERID, userId2)).first();

		if(u1 == null || u2 == null)
			return error(NOT_FOUND);

		try {
			if( isFollowing ) { //user1 quer seguir user2
				followers.insertOne( new Pair(userId2, userId1) );
				followings.insertOne( new Pair(userId1, userId2) );
			}else { //user1 quer deixar de seguir user2
				followers.deleteOne(Filters.and(Filters.eq(DataBase.ID1, userId2), Filters.eq(DataBase.ID2, userId1)));
				followings.deleteOne(Filters.and(Filters.eq(DataBase.ID1, userId1), Filters.eq(DataBase.ID2, userId2)));
			}
		} catch( MongoWriteException x ) {
			return error( CONFLICT );
		}
		return ok();
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		Profile u1 = profiles.find(Filters.eq(DataBase.USERID, userId1)).first();
		Profile u2 = profiles.find(Filters.eq(DataBase.USERID, userId2)).first();

		if(u1 == null || u2 == null)
			return error(NOT_FOUND);

		long count = followings.countDocuments(Filters.and(Filters.eq(DataBase.ID1, userId1), Filters.eq(DataBase.ID2, userId2)));
		return ok(count != 0);
	}
}
