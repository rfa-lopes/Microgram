package microgram.impl.java;

import java.util.List;

import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import microgram.impl.mongo.MongoPosts;

public final class JavaPosts implements Posts {

	static JavaPosts Posts;
	
	//private static final Set<String> EMPTY_SET = new HashSet<>();
	
	private MongoPosts postsManager;
	
	public JavaPosts() {
		postsManager = new MongoPosts();
		Posts = this;
	}

	@Override
	public Result<Post> getPost(String postId) {
		return postsManager.getPost(postId);
	}
	
	@Override
	public Result<String> createPost(Post post) {
		return postsManager.createPost(post);
	}

	@Override
	public Result<Void> deletePost(String postId) {
		return postsManager.deletePost(postId);
	}
	
	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {
		return postsManager.like(postId, userId, isLiked); 
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		return postsManager.isLiked(postId, userId);
	}
	
	@Override
	public Result<List<String>> getPosts(String userId) {
		return postsManager.getPosts(userId);
	}


	@Override
	public Result<List<String>> getFeed(String userId) {
		return postsManager.getFeed(userId);
	}

//	int getUserPostsStats( String userId) {
//		return userPosts.getOrDefault(userId, EMPTY_SET).size();
//	}
//	
//	void deleteAllUserPosts(String userId) {
//		for( String postId : userPosts.getOrDefault(userId, EMPTY_SET))
//			deletePost( postId );
//	}

}
