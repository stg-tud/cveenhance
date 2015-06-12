package tud.cpe.preparation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Before;
import org.junit.Test;

public class LuceneIndexCreatorTest {

	String[] cpes;

	@Before
	public void setUp() {
		cpes = new String[] { "cpe:/a:ibm:java:7.0.0.0", "cpe:/a:ibm:java:7.0.1.0", "cpe:/a:ibm:java:7.0.2.0",
				"cpe:/a:ibm:java:7.0.3.0", "cpe:/a:ibm:java:7.0.4.0", "cpe:/a:ibm:java:7.0.4.1",
				"cpe:/a:ibm:java:7.0.4.2", "cpe:/a:ibm:java:5.0.14.0", "cpe:/a:ibm:java:5.0.15.0",
				"cpe:/a:ibm:java:5.0.11.1", "cpe:/a:ibm:java:5.0.0.0", "cpe:/a:ibm:java:5.0.11.2",
				"cpe:/a:ibm:java:5.0.12.0", "cpe:/a:ibm:java:5.0.12.1", "cpe:/a:ibm:java:5.0.12.2",
				"cpe:/a:ibm:java:5.0.12.3", "cpe:/a:ibm:java:5.0.12.4", "cpe:/a:ibm:java:5.0.12.5",
				"cpe:/a:ibm:java:5.0.13.0", "cpe:/a:ibm:java:5.0.16.2", "cpe:/a:oracle:jre:5.0.16.1",
				"cpe:/a:oracle:jre:5.0.16.0", "cpe:/a:oracle:jre:5.0.11.0", "cpe:/a:ibm:java:6.0.1.0",
				"cpe:/a:ibm:java:6.0.11.0", "cpe:/a:ibm:java:6.0.10.1", "cpe:/a:ibm:java:6.0.0.0",
				"cpe:/a:ibm:java:6.0.12.0", "cpe:/a:ibm:java:6.0.2.0", "cpe:/a:ibm:java:6.0.3.0",
				"cpe:/a:ibm:java:6.0.4.0", "cpe:/a:ibm:java:6.0.5.0", "cpe:/a:ibm:java:6.0.7.0",
				"cpe:/a:ibm:java:6.0.6.0", "cpe:/a:ibm:java:6.0.8.1", "cpe:/a:ibm:java:6.0.8.0",
				"cpe:/a:sun:jre:6.0.9.1", "cpe:/a:sun:jre:6.0.9.0", "cpe:/a:ibm:java:6.0.10.0",
				"cpe:/a:ibm:java:6.0.9.2", "cpe:/a:ibm:java:6.0.13.0", "cpe:/a:ibm:java:6.0.13.1",
				"cpe:/a:ibm:java:6.0.13.2" };
	}

	@Test
	public void cpeDecoding_Test1() {
		assertEquals(LuceneIndexCreator.cpeDecoding(""), "");
	}

	@Test
	public void cpeDecoding_Test2() {
		assertEquals(LuceneIndexCreator.cpeDecoding("cpe:/a:ibm:java:1.6.0:update_10"),
				"cpe a ibm java 1.6.0 update_10");
	}

	@Test
	public void cpeDecoding_Test4() {
		assertEquals(LuceneIndexCreator.cpeDecoding("cpe:/o:ibm:java:1.6.0:update_10"),
				"cpe o ibm java 1.6.0 update_10");
	}

	@Test
	public void cpeDecoding_Test5() {
		assertEquals(LuceneIndexCreator.cpeDecoding("cpe:/h:ibm:java:1.6.0:update_10"),
				"cpe h ibm java 1.6.0 update_10");
	}

	@Test
	public void cpeDecoding_Test3() {
		assertEquals(LuceneIndexCreator.cpeDecoding(null), "");
	}

	@Test
	public void transformTitle_Test1() {
		assertEquals(LuceneIndexCreator.transformTitle(""), "");
	}

	@Test
	public void transformTitle_Test2() {
		assertEquals(LuceneIndexCreator.transformTitle(null), "");
	}

	@Test
	public void transformTitle_Test3() {
		assertEquals(LuceneIndexCreator.transformTitle("Oracle Java Runtime Environment 1.6.0 Update 55"),
				"Oracle Java Runtime Environment 1 6 0 Update 55");
	}

	@Test
	public void findTitle_Test1() {
		try {
			assertEquals(LuceneIndexCreator.findTitle("cpe:/a:oracle:jre:1.5.0:update_55"),
					"Oracle JRE 1 5 0 Update 55");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void findTitle_Test2() {
		try {
			assertEquals(LuceneIndexCreator.findTitle(""), "");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void findTitle_Test3() {
		try {
			assertEquals(LuceneIndexCreator.findTitle("cpe:/a:bogratz:rumtag:" + (int) (100 * Math.random())), "");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void getAllCpesWithVersionPrefix_Test1() {
		assertEquals(LuceneIndexCreator.getAllCpesWithVersionPrefix(
				"cpe:/a:bogratz:rumtag:" + (int) (100 * Math.random()), new ArrayList<String>()),
				Arrays.asList(new String[] {}));
	}

	@Test
	public void getAllCpesWithVersionPrefix_Test2() {
		assertEquals(LuceneIndexCreator.getAllCpesWithVersionPrefix(null, new ArrayList<String>()),
				Arrays.asList(new String[] {}));
	}

	@Test
	public void getAllCpesWithVersionPrefix_Test3() {
		assertEquals(LuceneIndexCreator.getAllCpesWithVersionPrefix("cpe:/a:bogratz:rumtag", new ArrayList<String>()),
				Arrays.asList(new String[] {}));
	}

	@Test
	public void getAllCpesWithVersionPrefix_Test4() {
		assertEquals(
				LuceneIndexCreator.getAllCpesWithVersionPrefix("7", Arrays.asList(cpes)),
				Arrays.asList(new String[] { "cpe:/a:ibm:java:7.0.0.0", "cpe:/a:ibm:java:7.0.1.0",
						"cpe:/a:ibm:java:7.0.2.0", "cpe:/a:ibm:java:7.0.3.0", "cpe:/a:ibm:java:7.0.4.0",
						"cpe:/a:ibm:java:7.0.4.1", "cpe:/a:ibm:java:7.0.4.2" }));
	}

	@Test
	public void getAllCpesWithVersionPrefix_Test5() {
		assertEquals(
				LuceneIndexCreator.getAllCpesWithVersionPrefix("7.0.4", Arrays.asList(cpes)),
				Arrays.asList(new String[] { "cpe:/a:ibm:java:7.0.4.0", "cpe:/a:ibm:java:7.0.4.1",
						"cpe:/a:ibm:java:7.0.4.2" }));
	}

	@Test
	public void cpeEncoding_Test1() {
		assertEquals(LuceneIndexCreator.cpeEncoding("cpe a fux baum 1.3.4 t l b"), "cpe:/a:fux:baum:1.3.4:t:l:b");
	}

	@Test
	public void cpeEncoding_Test4() {
		assertEquals(LuceneIndexCreator.cpeEncoding("cpe o fux baum 1.3.4 t l b"), "cpe:/o:fux:baum:1.3.4:t:l:b");
	}

	@Test
	public void cpeEncoding_Test5() {
		assertEquals(LuceneIndexCreator.cpeEncoding("cpe h fux baum 1.3.4 t l b"), "cpe:/h:fux:baum:1.3.4:t:l:b");
	}

	@Test
	public void cpeEncoding_Test2() {
		assertEquals(LuceneIndexCreator.cpeEncoding(""), "");
	}

	@Test
	public void cpeEncoding_Test3() {
		assertEquals(LuceneIndexCreator.cpeEncoding(null), "");
	}

}