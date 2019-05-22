package microgram.impl.mongo;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.ErrorCode.CONFLICT;

import java.util.List;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import utils.DataBase;
import utils.Pair;

public class MongoProfiles implements Profiles {

	private MongoCollection<Profile> profiles;
	private MongoCollection<Pair> followers;
	private MongoCollection<Pair> followings;

	public MongoProfiles() {
		DataBase db = DataBase.init();
		profiles = db.profiles;
		followers = db.followers;
		followings = db.followings;
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		Profile res = profiles.find(Filters.eq(DataBase.USERID, userId)).first();
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

		Profile u1 = profiles.find(Filters.eq(DataBase.USERID, userId1)).first();
		Profile u2 = profiles.find(Filters.eq(DataBase.USERID, userId2)).first();

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
				followers.deleteOne(Filters.and(Filters.eq(DataBase.ID1, userId2), Filters.eq(DataBase.ID2, userId1) ));
				followings.deleteOne(Filters.and(Filters.eq(DataBase.ID1, userId1), Filters.eq(DataBase.ID2, userId2) ));
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
		Profile u1 = profiles.find(Filters.eq(DataBase.USERID, userId1)).first();
		Profile u2 = profiles.find(Filters.eq(DataBase.USERID, userId2)).first();

		//Profiles nao existem
		if(u1 == null || u2 == null)
			return error(NOT_FOUND);

		//Se userId1 seguir userId2 vai returnar um par
		//Se nao seguir, retorna null (porque nao existe na tabela)
		Pair res = followings.find(Filters.and(Filters.eq(DataBase.ID1, userId1), Filters.eq(DataBase.ID2, userId2))).first();
		return ok(res != null);
	}

}