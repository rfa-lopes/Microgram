package microgram.impl.mongo;

import java.util.List;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;

public class MongoProfiles implements Profiles {
	
	private static final String DB_NAME = "Projeto_2_SD";
	private static final String DB_TABLE = "Profiles";
	
	private MongoClient mongo;
	private MongoDatabase dbName;
	
	public MongoProfiles() {
		mongo = new MongoClient( "localhost" );
		CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		dbName = mongo.getDatabase(DB_NAME).withCodecRegistry(pojoCodecRegistry);
		MongoCollection<Profile> Profiles = dbName.getCollection(DB_TABLE, Profile.class);
	}
	
	@Override
	public Result<Profile> getProfile(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		// o melhor é tentar inserir logo e se não correr nao perdemos tempo
		// se a base de dados disser que nao deu retornar erro na base de dados
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