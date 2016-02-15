package database;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.DB;

import com.google.common.primitives.SignedBytes;

/**
 * Get the parent post for a comment (the blogpost that was commented)
 * @author Skerberus
 *
 */
public class CommentPostMap extends DBMap<byte[], byte[]> { 
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

	public CommentPostMap(DBSet databaseSet, DB database) {
		super(databaseSet, database);
	}

	public CommentPostMap(DBMap<byte[], byte[]> parent) {
		super(parent);
	}

	@Override
	protected Map<byte[], byte[]> getMap(DB database) {

		return database.createTreeMap("CommentPostMapTree")
				.comparator(SignedBytes.lexicographicalComparator())
				.makeOrGet();

	}
	
	public void add(byte[] signatureOfComment, byte[] signatureOfBlogPost)
	{
		set(signatureOfComment, signatureOfBlogPost);
	}
	
	public void remove(byte[] signatureOfComment)
	{
		delete(signatureOfComment);
	}

	@Override
	protected Map<byte[], byte[]> getMemoryMap() {
		return new HashMap<>();
	}

	@Override
	protected byte[] getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	@Override
	protected void createIndexes(DB database) {
	}
}

