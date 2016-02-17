package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import utils.LinkUtils;

public class LinkUtilsTest {
	@Test
	public void testGetAllLinks() throws Exception {
		String text = "anytext https://test.com/test-test/qora some other text	";
		
		ArrayList<String> links = LinkUtils.getAllLinks(text);
		assertEquals("https://test.com/test-test/qora", links.get(0));
		
		String transformURLIntoLinks = LinkUtils.transformURLIntoLinks(links.get(0));
		assertEquals("<a target=\"_blank\" href='https://test.com/test-test/qora'>https://test.com/test-test/qora</a>",transformURLIntoLinks);
		
	}
}
