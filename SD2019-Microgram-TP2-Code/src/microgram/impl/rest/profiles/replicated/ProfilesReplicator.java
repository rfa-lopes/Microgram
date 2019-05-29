package microgram.impl.rest.profiles.replicated;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ErrorCode.NOT_IMPLEMENTED;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.GetProfile;

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
			case CreateProfile: {
				return localReplicaDB.createProfile( op.arg( Profile.class));
			}case GetProfile: {
				System.out.println("op.args");
				return localReplicaDB.getProfile( op.arg(String.class));
			}case DeleteProfile: {
				
			}case FollowProfile: {
				
			}case UnFollowProfile: {
				
			}case SearchProfile: {
				
			}case IsFollowing: {
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		// TODO Auto-generated method stub
		return null;
	}
}
