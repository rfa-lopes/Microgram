package microgram.impl.java;

import java.util.List;

import java.util.Set;


import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.mongo.MongoProfiles;

public final class JavaProfiles implements Profiles {
	
	
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
		return res;
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		Result<Void> res = profilesManager.deleteProfile(userId);
		return res;
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		Result<List<Profile>> res = profilesManager.search(prefix);
		return res;
	}


	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		Result<Boolean> res = profilesManager.isFollowing(userId1, userId2);
		return res;
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		Result<Void> res = profilesManager.follow(userId1, userId2, isFollowing);
		return res;
	}
}
