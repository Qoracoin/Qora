package database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mapdb.DB;

import utils.ByteArrayUtils;

import com.google.common.primitives.SignedBytes;

public class CommentPostMap extends DBMap<byte[], List<byte[]>> {

	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

	public CommentPostMap(DBSet databaseSet, DB database) {
		super(databaseSet, database);
	}

	public CommentPostMap(DBMap<byte[], List<byte[]>> parent) {
		super(parent);
	}

	@Override
	protected Map<byte[], List<byte[]>> getMap(DB database) {

		return database.createTreeMap("CommentPostMap")
				.comparator(SignedBytes.lexicographicalComparator())
				.makeOrGet();

	}
	
	public void add(byte[] signatureOfPostToComment, byte[] signatureOfComment) {
		List<byte[]> list;
		list = get(signatureOfPostToComment);

		if (list == null) {
			list = new ArrayList<>();
		}

		if (!ByteArrayUtils.contains(list, signatureOfComment)) {
			list.add(signatureOfComment);
		}

		set(signatureOfPostToComment, list);

	}
	
	public void remove(byte[] signatureOfRelatedComment, byte[] signatureOfComment) {
		List<byte[]> list;
		list = get(signatureOfRelatedComment);

		if (list == null) {
			return;
		}

		if (ByteArrayUtils.contains(list, signatureOfComment)) {
			list.remove(signatureOfComment);
		}
		
		if(list.size() == 0)
		{
			delete(signatureOfRelatedComment);
		}else
		{
			set(signatureOfRelatedComment, list);
		}


	}
	

	@Override
	protected Map<byte[], List<byte[]>> getMemoryMap() {
		return new HashMap<>();
	}

	@Override
	protected List<byte[]> getDefaultValue() 
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
