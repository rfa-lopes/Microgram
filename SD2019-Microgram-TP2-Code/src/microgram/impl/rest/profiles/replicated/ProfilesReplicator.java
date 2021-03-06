package microgram.impl.rest.profiles.replicated;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ErrorCode.NOT_IMPLEMENTED;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.GetProfile;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.CreateProfile;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.DeleteProfile;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.FollowProfile;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.UnFollowProfile;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.SearchProfile;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.IsFollowing;


import java.util.List;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.rest.replication.MicrogramOperation;
import microgram.impl.rest.replication.MicrogramOperationExecutor;
import microgram.impl.rest.replication.OrderedExecutor;
import microgram.impl.rest.replication.TotalOrderExecutor;

public  class ProfilesReplicator implements MicrogramOperationExecutor, Profiles {

	private static final int FOLLOWER = 0, FOLLOWEE = 1;

	final Profiles localReplicaDB;
	final OrderedExecutor executor;

	ProfilesReplicator( Profiles localDB, TotalOrderExecutor totalOrderExecutor) {
		this.localReplicaDB = localDB;
		this.executor = totalOrderExecutor.init(this);
	}

	@Override
	public Result<?> execute( MicrogramOperation op ) {
		switch( op.type ) {
		case CreateProfile:
			return localReplicaDB.createProfile( op.arg(Profile.class));
		case GetProfile:
			return localReplicaDB.getProfile( op.arg(String.class));
		case DeleteProfile: 
			return localReplicaDB.deleteProfile( op.arg(String.class));
		case FollowProfile: {
			String[] users = op.args(String[].class);
			return localReplicaDB.follow(users[FOLLOWER], users[FOLLOWEE], true);
		}case UnFollowProfile: {
			String[] users = op.args(String[].class);
			return localReplicaDB.follow(users[FOLLOWER], users[FOLLOWEE], false);
		}case SearchProfile:
			return localReplicaDB.search(op.args(String.class));
		case IsFollowing: {
			String[] users = op.args(String[].class);
			return localReplicaDB.isFollowing( users[FOLLOWER], users[FOLLOWEE]);
		}
		default:
			return error(NOT_IMPLEMENTED);
		}	
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		return executor.replicate( new MicrogramOperation(GetProfile, userId));
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		return executor.replicate( new MicrogramOperation(CreateProfile, profile));
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		return executor.replicate( new MicrogramOperation(DeleteProfile, userId));
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		return executor.replicate( new MicrogramOperation(SearchProfile, prefix));
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		String[] args = {userId1, userId2};
		if(isFollowing)
			return executor.replicate( new MicrogramOperation(FollowProfile, args));
		return executor.replicate( new MicrogramOperation(UnFollowProfile, args));
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		String[] users = {userId1, userId2};
		return executor.replicate( new MicrogramOperation(IsFollowing, users));
	}
}
