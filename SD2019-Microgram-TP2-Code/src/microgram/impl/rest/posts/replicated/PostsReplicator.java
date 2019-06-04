package microgram.impl.rest.posts.replicated;
import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ErrorCode.NOT_IMPLEMENTED;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.CreatePost;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.GetPost;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.DeletePost;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.LikePost;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.UnLikePost;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.IsLiked;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.GetPosts;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.GetFeed;

import java.util.List;

import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import microgram.impl.rest.replication.MicrogramOperation;
import microgram.impl.rest.replication.MicrogramOperationExecutor;
import microgram.impl.rest.replication.OrderedExecutor;
import microgram.impl.rest.replication.TotalOrderExecutor;

public class PostsReplicator implements MicrogramOperationExecutor, Posts {

	private static final int PostID = 0, UserID = 1;

	final Posts localReplicaDB;
	final OrderedExecutor executor;

	PostsReplicator( Posts localDB, TotalOrderExecutor totalOrderExecutor) {
		this.localReplicaDB = localDB;
		this.executor = totalOrderExecutor.init(this);
	}

	@Override
	public Result<?> execute(MicrogramOperation op) {
		switch( op.type ) {
		case CreatePost :
			return localReplicaDB.createPost( op.arg(Post.class));
		case GetPost :
			return localReplicaDB.getPost(op.args(String.class));
		case DeletePost :
			return localReplicaDB.deletePost(op.args(String.class));
		case LikePost :{
			String[] users = op.args(String[].class);
			return localReplicaDB.like(users[PostID], users[UserID], true);
		}case UnLikePost : {
			String[] users = op.args(String[].class);
			return localReplicaDB.like(users[PostID], users[UserID], false);
		}case IsLiked : {
			String[] users = op.args(String[].class);
			return localReplicaDB.isLiked(users[PostID], users[UserID]);
		}case GetPosts :
			return localReplicaDB.getPosts(op.args(String.class));
		case GetFeed :
			return localReplicaDB.getFeed(op.args(String.class));
		default:
			return error(NOT_IMPLEMENTED);
		}
	}

	@Override
	public Result<Post> getPost(String postId) {
		return executor.replicate( new MicrogramOperation(GetPost, postId));
	}

	@Override
	public Result<String> createPost(Post post) {
		return executor.replicate( new MicrogramOperation(CreatePost, post));
	}

	@Override
	public Result<Void> deletePost(String postId) {
		return executor.replicate( new MicrogramOperation(DeletePost, postId));
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {
		String[] args = {postId, userId};
		if(isLiked)
			return executor.replicate( new MicrogramOperation(LikePost, args));
		return executor.replicate( new MicrogramOperation(UnLikePost, args));
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		String[] args = {postId, userId};
		return executor.replicate( new MicrogramOperation(IsLiked, args));
	}

	@Override
	public Result<List<String>> getPosts(String userId) {
		return executor.replicate( new MicrogramOperation(GetPosts, userId));
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		return executor.replicate( new MicrogramOperation(GetFeed, userId));
	}	

}
