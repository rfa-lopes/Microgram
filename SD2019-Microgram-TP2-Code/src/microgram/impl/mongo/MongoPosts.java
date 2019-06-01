package microgram.impl.mongo;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;

import java.util.LinkedList;
import java.util.List;

import org.bson.conversions.Bson;

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
	private MongoCollection<Pair> userPosts;
	private MongoCollection<Profile> profiles;

	public MongoPosts() {
		DataBase db = DataBase.init();
		posts = db.getPosts();
		likes = db.getLikes();
		userPosts = db.getUserPosts();
		profiles = db.getProfiles();
	}

	@Override
	public Result<Post> getPost(String postId) {
		Post res = posts.find(Filters.eq(DataBase.POSTID, postId)).first();
		if(res == null)
			return error(NOT_FOUND);
		return ok(res);
	}

	@Override
	public Result<String> createPost(Post post) {
		try {
			posts.insertOne(post);
			return ok();
		} catch( MongoWriteException x ) {
			return error( CONFLICT );
		}
	}

	@Override
	public Result<Void> deletePost(String postId) {
		Bson pf = Filters.eq(DataBase.POSTID, postId);
		Post post = posts.find(pf).first();
		if(post == null)
			return error(NOT_FOUND);

		//fazer delete no Posts (apagar o postId)
		posts.deleteOne(pf);

		//Fazer delete no likes (postOd deixa de ter likes)
		likes.deleteMany(pf);

		//Fazer delete no userPosts (user deixa de ter aquele post)
		userPosts.deleteOne(pf);

		return ok();
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {

		Post post = posts.find(Filters.eq(DataBase.POSTID, postId)).first();
		Profile profile = profiles.find(Filters.eq(DataBase.USERID, userId)).first();

		//Se o post nao existir || o perfil nao existir retorna NOT_FOUND
		if( post == null || profile == null)
			return error( NOT_FOUND );

		try {
			if (isLiked) 		//Profile mete like no post
				likes.insertOne(new Pair(postId, userId));
			else 				//Profile retira o like no post
				likes.deleteOne(Filters.and(Filters.eq(DataBase.POSTID, postId), Filters.eq(DataBase.USERID, userId)));

		} catch( MongoWriteException x ) {
			//Caso queira meter like num post que ja tem o like
			//Caso queira retirar um like que nao existe
			return error( CONFLICT );
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

		Pair res = likes.find(Filters.and(Filters.eq(DataBase.POSTID, postId), Filters.eq(DataBase.USERID, userId))).first();

		return ok(res != null);
	}

	@Override
	public Result<List<String>> getPosts(String userId) {
		Bson uf = Filters.eq(DataBase.USERID, userId);
		Profile u = profiles.find(uf).first();
		if(u == null)
			return error(NOT_FOUND);

		List<String> res = new LinkedList<String>();
		FindIterable<Pair> up = userPosts.find(uf);
		for(Pair p : up)
			res.add(p.getId2());

		return ok(res);
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
