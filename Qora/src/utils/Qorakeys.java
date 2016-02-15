package utils;

public enum Qorakeys {

	DEFAULT("defaultkey", KeyVariation.DEFAULTKEY), WEBSITE("website",KeyVariation.DEFAULTKEY), BLOGWHITELIST("blogwhitelist", KeyVariation.LISTKEY), BLOGBLACKLIST(
			"blogblacklist",KeyVariation.LISTKEY), BLOGDESCRIPTION("blogdescription",KeyVariation.DEFAULTKEY), BLOGTITLE("blogtitle",KeyVariation.DEFAULTKEY), BLOGENABLE(
					"blogenable", KeyVariation.EXISTSKEY), PROFILEENABLE("profileenable", KeyVariation.EXISTSKEY), PROFILEAVATAR("profileavatar", KeyVariation.DEFAULTKEY), PROFILEFOLLOW(
							"profilefollow", KeyVariation.LISTKEY), PROFILEMAINGRAPHIC("profilemaingraphic",KeyVariation.DEFAULTKEY), PROFILELIKEPOSTS("profilelikeposts",KeyVariation.LISTKEY), BLOGBLOCKCOMMENTS("blogblockcomments",KeyVariation.EXISTSKEY);
	private final String keyname;
	private KeyVariation variation;

	private Qorakeys(String keyname, KeyVariation variation) {
		this.keyname = keyname;
		this.variation = variation;
	}

	public String getKeyname() {
		return keyname;
	}

	public String toString() {
		return keyname;
	}

	public KeyVariation getVariation() {
		return variation;
	}
	
	public static boolean isPartOf(String enumString)
	{
		Qorakeys[] values = Qorakeys.values();
		for (Qorakeys qorakey : values) {
			if(enumString.equals(qorakey.toString()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	

}
