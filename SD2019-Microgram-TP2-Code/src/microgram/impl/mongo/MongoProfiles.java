package microgram.impl.mongo;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.ErrorCode.CONFLICT;

import java.util.LinkedList;
import java.util.List;

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
	private MongoCollection<Pair> followings;
	private MongoCollection<Post> posts;
	private MongoCollection<Pair> likes;

	public MongoProfiles() {
		DataBase db = DataBase.init();
		profiles = db.getProfiles();
		followings = db.getFollowings();
		posts = db.getPosts();
		likes = db.getLikes();
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		//Verificar se o user existe
		Profile res = profiles.find(Filters.eq(DataBase.USERID, userId)).first();
		if(res == null) return error( NOT_FOUND );

		//Atualizar as estatisticas
		res.setFollowing((int)followings.countDocuments(Filters.eq(DataBase.ID1, userId)));
		res.setFollowers((int) followings.countDocuments(Filters.eq(DataBase.ID2, userId)));
		res.setPosts((int) posts.countDocuments(Filters.eq(DataBase.OWNERID, userId)));

		return ok(res);
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		//Verificar se o user existe (se nao existe retorna 0)
		long pro = profiles.countDocuments(Filters.eq(DataBase.USERID, profile.getUserId()));
		if(pro != 0) return error( CONFLICT );

		//inserir na tabela de profiles o novo user
		profiles.insertOne(profile);
		return ok();
	}

	@Override
	public Result<Void> deleteProfile(String userId) {

		//Verificar se o user foi removido, se nao foi e porque nao exitia e devolve null
		Profile pro = profiles.findOneAndDelete(Filters.eq(DataBase.USERID, userId));
		if ( pro == null ) return error(NOT_FOUND);

		//Fazer delete na tabela Posts (os posts do profile)
		posts.deleteMany(Filters.eq(DataBase.OWNERID, userId));

		//Fazer delete na tabela likes (os likes do profile)
		likes.deleteMany(Filters.eq(DataBase.ID2, userId));

		//Fazer delete na tabela followings (os followings do user / os que o user faz following)
		followings.deleteMany(Filters.eq(DataBase.ID1, userId));
		followings.deleteMany(Filters.eq(DataBase.ID2, userId));

		return ok();
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		String p = "^" + prefix;
		FindIterable<Profile> found = profiles.find(Filters.regex(DataBase.USERID, p));

		//LinkedList e mais facil de adicionar para um numero indeterminado de users com o prefix indicado
		List<Profile> res = new LinkedList<Profile>();
		for(Profile pro : found)
			res.add(pro);
		//Pode devolver uma lista vazia
		return ok(res);
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		long u1 = profiles.countDocuments(Filters.eq(DataBase.USERID, userId1));
		long u2 = profiles.countDocuments(Filters.eq(DataBase.USERID, userId2));
		//Verificar se os users existem, se nao exitirem devolve 0
		if(u1 == 0 || u2 == 0)
			return error(NOT_FOUND);

		try {
			//user1 quer seguir user2
			if( isFollowing )
				followings.insertOne( new Pair(userId1, userId2) );

			//user1 quer deixar de seguir user2
			else
				followings.deleteOne(Filters.and(Filters.eq(DataBase.ID1, userId1), Filters.eq(DataBase.ID2, userId2)));

		}catch(Exception x) {
			//Caso queira adicionar um follow que ja existe
			//Caso queira remover um follow que nao existe
			return error(CONFLICT);
		}
		return ok();
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		long u1 = profiles.countDocuments(Filters.eq(DataBase.USERID, userId1));
		long u2 = profiles.countDocuments(Filters.eq(DataBase.USERID, userId2));
		//Verificar se os users existem, se nao exitirem devolve 0
		if(u1 == 0 || u2 == 0)
			return error(NOT_FOUND);

		//Se existe devolve 1, se nao devolve 0
		long count = followings.countDocuments(Filters.and(Filters.eq(DataBase.ID1, userId1), Filters.eq(DataBase.ID2, userId2)));
		return ok(count != 0);
	}

}
