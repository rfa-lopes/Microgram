package microgram.impl.java;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.impl.java.JavaPosts.Posts;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.mongo.MongoProfiles;

public final class JavaProfiles implements Profiles {
	
	static JavaProfiles Profiles;
	
	static final Set<String> DUMMY_SET = ConcurrentHashMap.newKeySet();

	final Map<String, Profile> users = new ConcurrentHashMap<>();
	final Map<String, Set<String>> followers = new ConcurrentHashMap<>();
	final Map<String, Set<String>> following = new ConcurrentHashMap<>();
	
	private MongoProfiles profilesManager;
		
	public JavaProfiles() {
		profilesManager = new MongoProfiles();
	}
	
	@Override
	
	public Result<Profile> getProfile(String userId) {
		Result<Profile> res = profilesManager.getProfile(userId);
		return res;
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		Result<Void> res = profilesManager.createProfile(profile);
		
		if(!res.isOK())
			return res;
		//TODO atualizar as tabelas de followers e followings
		followers.put(profile.getUserId(), ConcurrentHashMap.newKeySet());
		following.put(profile.getUserId(), ConcurrentHashMap.newKeySet());

		return res;
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		Result<Void> res = profilesManager.deleteProfile(userId);
		if(!res.isOK())
			return res;
		//TODO atualizar as tabelas de followers e followings
		for (String follower : followers.remove(userId))
			following.getOrDefault(follower, DUMMY_SET).remove(userId);
	
		for (String followee : following.remove(userId))
			followers.getOrDefault(followee, DUMMY_SET).remove(userId);
	
		//users.remove(userId);
		//TODO realizar invocação remota pois iston ao esta bem
		//Posts.deleteAllUserPosts(userId);
		
		return ok();
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		return null;
		//return ok(users.values().stream().filter(p -> p.getUserId().startsWith(prefix)).collect(Collectors.toList()));
	}


	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		Result<Boolean> res = profilesManager.isFollowing(userId1, userId2);
		return res;
//		Set<String> s1 = following.get(userId1);
//
//		if (s1 == null)
//			return error(NOT_FOUND);
//		else
//			return ok(s1.contains(userId2));
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		Result<Void> res = profilesManager.follow(userId1, userId2, isFollowing);
		return res;
//		Set<String> s1 = following.get(userId1);
//		Set<String> s2 = followers.get(userId2);
//
//		if (s1 == null || s2 == null)
//			return error(NOT_FOUND);
//
//		if (isFollowing) {
//			s1.add(userId2);
//			s2.add(userId1);
//		} else {
//			s1.remove(userId2); 
//			s2.remove(userId1);
//		}
//		return ok();
	}

	Set<String> following(String userId) {
		return following.get( userId );
	}
}
