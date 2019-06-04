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

	private MongoCollection<Profile> profiles;
	private MongoCollection<Pair> followings;
	private MongoCollection<Post> posts;
	private MongoCollection<Pair> likes;

	public MongoPosts() {
		DataBase db = DataBase.init();
		profiles = db.getProfiles();
		followings = db.getFollowings();
		posts = db.getPosts();
		likes = db.getLikes();
	}

	@Override
	public Result<Post> getPost(String postId) {
		//Verificar se o post existe
		Post res = posts.find(Filters.eq(DataBase.POSTID, postId)).first();
		if(res == null) return error(NOT_FOUND);

		//Atualizar as estatisticas
		res.setLikes( (int) likes.countDocuments(Filters.eq(DataBase.ID1, postId)) );
		return ok(res);
	}

	@Override
	public Result<String> createPost(Post post) {
		//Verificar de o owner existe, se existir devolve 1, ou 0 caso nao exista
		long res = profiles.countDocuments(Filters.eq(DataBase.USERID, post.getOwnerId()));
		if( res == 0 )
			return error(NOT_FOUND);

		//Inserir o post na tabela de posts
		posts.insertOne(post);
		return ok(post.getPostId());
	}

	@Override
	public Result<Void> deletePost(String postId) {
		//Tenta remover o post, se conseguir devolve o post, ou devolve null caso nao exista
		Post post = posts.findOneAndDelete(Filters.eq(DataBase.POSTID, postId));
		if(post == null) return error(NOT_FOUND);

		//Fazer delete no likes (postId deixa de ter likes)
		likes.deleteMany(Filters.eq(DataBase.ID1, postId));

		return ok();
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {
		//Verificar se o post e o user existem (se nao existirem retorna 0)
		long post = posts.countDocuments(Filters.eq(DataBase.POSTID, postId));
		long profile = profiles.countDocuments(Filters.eq(DataBase.USERID, userId));

		//Se o post nao existir || o perfil nao existir retorna NOT_FOUND
		if( post == 0 || profile == 0)
			return error( NOT_FOUND );
		//Profile mete like no post
		if (isLiked)
			try{
				likes.insertOne(new Pair(postId, userId));
			}catch(MongoWriteException x) {
				return error( CONFLICT );
			}
		//Profile retira o like no post
		else {
			Pair res = likes.findOneAndDelete(Filters.and(Filters.eq(DataBase.ID1, postId), Filters.eq(DataBase.ID2, userId)));
			if(res == null) return error( NOT_FOUND );
		}
		return ok();
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		//Verificar se o post e o user existem (se nao existirem retorna 0)
		long post = posts.countDocuments(Filters.eq(DataBase.POSTID, postId));
		long user = profiles.countDocuments(Filters.eq(DataBase.USERID, userId));

		//Se o post ou o user nao existirem retorna NOT_FOUND
		if( post == 0 || user == 0 )
			return error( NOT_FOUND );

		long res = likes.countDocuments(Filters.and(Filters.eq(DataBase.ID1, postId), Filters.eq(DataBase.ID2, userId)));

		return ok(res != 0);
	}

	@Override
	public Result<List<String>> getPosts(String userId) {
		//Verificar se o user existe, caso nao exista devolve 0
		long user = profiles.countDocuments(Filters.eq(DataBase.USERID, userId));

		if(user == 0)
			return error(NOT_FOUND);

		List<String> res = new LinkedList<String>();
		FindIterable<Post> userPostsList = posts.find(Filters.eq(DataBase.OWNERID, userId));
		for(Post p : userPostsList)
			res.add(p.getPostId());

		return ok(res);
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		//Verificar se o user existe, caso nao exista devolve 0
		long user =  profiles.countDocuments(Filters.eq(DataBase.USERID, userId));
		if(user == 0)
			return error(NOT_FOUND);

		List<String> res = new LinkedList<String>();
		FindIterable<Pair> foll = followings.find(Filters.eq(DataBase.ID1, userId));
		//Iterar todos os users que o userId segue
		for(Pair f : foll) {
			//Em cada um deles adicionar todos os posts
			FindIterable<Post> userposts = posts.find(Filters.eq(DataBase.OWNERID, f.getId2()));
			for(Post p : userposts)
				res.add(p.getPostId());
		}

		return ok(res);
	}

}
