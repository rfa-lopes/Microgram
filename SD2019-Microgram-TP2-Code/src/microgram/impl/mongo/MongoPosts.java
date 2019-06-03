package microgram.impl.mongo;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;

import java.util.LinkedList;
import java.util.List;

import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import utils.DataBase;
import utils.Pair;

public class MongoPosts implements Posts {

	private MongoCollection<Post> posts;
	private MongoCollection<Pair> likes;
	private MongoCollection<Profile> profiles;
	private MongoCollection<Pair> followings;

	public MongoPosts() {
		DataBase db = DataBase.init();
		posts = db.getPosts();
		likes = db.getLikes();
		profiles = db.getProfiles();
		followings = db.getFollowings();
	}

	@Override
	public Result<Post> getPost(String postId) {
		try {
			Post res = posts.find(Filters.eq(DataBase.POSTID, postId)).first();
			//Atualizar as estatisticas
			res.setLikes( (int) likes.countDocuments(Filters.eq(DataBase.ID1, postId)) );
			return ok(res);
		}catch(MongoWriteException x) {
			return error(NOT_FOUND);
		}
	}

	@Override
	public Result<String> createPost(Post post) {
		Profile res = profiles.find(Filters.eq(DataBase.USERID, post.getOwnerId())).first();
		if( res == null ) return error(NOT_FOUND);
		
		posts.insertOne(post);
		return ok(post.getPostId());
	}

	@Override
	public Result<Void> deletePost(String postId) {
		//Melhorar isto
		Post post = posts.find(Filters.eq(DataBase.POSTID, postId)).first();
		if(post == null) return error(NOT_FOUND);

		//fazer delete no Posts (apagar o postId)
		posts.deleteOne(Filters.eq(DataBase.POSTID, postId));

		//Fazer delete no likes (postOd deixa de ter likes)
		likes.deleteMany(Filters.eq(DataBase.ID1, postId));

		return ok();
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {

		Post post = posts.find(Filters.eq(DataBase.POSTID, postId)).first();
		Profile profile = profiles.find(Filters.eq(DataBase.USERID, userId)).first();

		//Se o post nao existir || o perfil nao existir retorna NOT_FOUND
		if( post == null || profile == null)
			return error( NOT_FOUND );

		if (isLiked)			//Profile mete like no post
			try{
				likes.insertOne(new Pair(postId, userId));
			}catch(MongoWriteException x) {
				return error( CONFLICT );
			}
		else { 					//Profile retira o like no post
			Pair res = likes.find(Filters.and(Filters.eq(DataBase.ID1, postId), Filters.eq(DataBase.ID2, userId))).first();
			if(res == null) return error( NOT_FOUND );
			likes.deleteOne(Filters.and(Filters.eq(DataBase.ID1, postId), Filters.eq(DataBase.ID2, userId)));
		}
		return ok();
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		Post post = posts.find(Filters.eq(DataBase.POSTID, postId)).first();
		Profile user = profiles.find(Filters.eq(DataBase.USERID, userId)).first();

		//Se o post ou o user nao existirem retorna NOT_FOUND
		if( post == null || user == null )
			return error( NOT_FOUND );

		Pair res = likes.find(Filters.and(Filters.eq(DataBase.ID1, postId), Filters.eq(DataBase.ID2, userId))).first();

		return ok(res != null);
	}

	@Override
	public Result<List<String>> getPosts(String userId) {

		Profile user = profiles.find(Filters.eq(DataBase.USERID, userId)).first();

		if(user == null)
			return error(NOT_FOUND);

		List<String> res = new LinkedList<String>();
		FindIterable<Post> userPostsList = posts.find(Filters.eq("ownerId", userId));
		for(Post p : userPostsList)
			res.add(p.getPostId());

		return ok(res);
	}

	@Override
	public Result<List<String>> getFeed(String userId) {

		Profile user = profiles.find(Filters.eq(DataBase.USERID, userId)).first();
		if(user == null)
			return error(NOT_FOUND);

		List<String> res = new LinkedList<String>();

		FindIterable<Pair> foll = followings.find(Filters.eq(DataBase.ID1, userId));

		for(Pair f : foll) {
			FindIterable<Post> userposts = posts.find(Filters.eq("ownerId", f.getId2()));
			for(Post p : userposts)
				res.add(p.getPostId());
		}

		return ok(res);
	}
}
