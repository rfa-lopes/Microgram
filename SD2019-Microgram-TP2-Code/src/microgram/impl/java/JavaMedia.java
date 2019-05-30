package microgram.impl.java;

import static microgram.api.java.Result.error;

import static microgram.api.java.Result.ErrorCode.INTERNAL_ERROR;

import microgram.api.java.Media;
import microgram.api.java.Result;
import microgram.impl.dropbox.DropboxMedia;

public class JavaMedia implements Media {

	private static final String MEDIA_EXTENSION = ".jpg";
	private static final String ROOT_DIR = "/";
	
	private DropboxMedia media;
	public JavaMedia() throws Exception {
		media = DropboxMedia.createClientWithAccessToken();
	}

	@Override
	public Result<String> upload(byte[] bytes) {
		try {
			Result<String> res = media.upload(bytes);
			return res;
		} catch (Exception x) {
			return error(INTERNAL_ERROR);
		}
	}

	@Override
	public Result<byte[]> download(String id) {
		try {
			Result<byte[]> res = media.download(ROOT_DIR + id);
			return res;
		} catch (Exception x) {
			return error(INTERNAL_ERROR);
		}
	}

	@Override
	public Result<Void> delete(String id) {
		try {
			Result<Void> res = media.delete(ROOT_DIR + id);
			return res;
		} catch (Exception x) {
			return error(INTERNAL_ERROR);
		}
	}
}
