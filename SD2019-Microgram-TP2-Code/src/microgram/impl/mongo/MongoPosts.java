package microgram.impl.mongo;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;

import java.util.List;

import com.mongodb.MongoWriteException;
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
		posts = db.posts;
		likes = null;
		userPosts = db.userPosts;
		profiles = db.profiles;
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
		// TODO Auto-generated method stub
		return null;
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
				likes.deleteOne(Filters.and(Filters.eq(DataBase.ID1, postId), Filters.eq(DataBase.ID2, userId)));

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

		//Verificamos se o userId existe?

		//Se o post nao existir retorna NOT_FOUND
		if( post == null )
			return error( NOT_FOUND );

		Pair res = likes.find(Filters.and(Filters.eq(DataBase.ID1, postId), Filters.eq(DataBase.ID2, userId))).first();

		return ok(res != null);
	}

	@Override
	public Result<List<String>> getPosts(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
