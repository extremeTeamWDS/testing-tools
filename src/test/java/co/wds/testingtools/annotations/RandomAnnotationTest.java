package co.wds.testingtools.annotations;

import static co.wds.testingtools.annotations.RandomAnnotation.randomise;
import static co.wds.testingtools.annotations.RandomAnnotation.randomiseFields;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import co.wds.testingtools.annotations.RandomAnnotation.Randomise;

public class RandomAnnotationTest {
	@Randomise String randomString_1;
	@Randomise String randomString_2;
	@Randomise Long randomLong;
	@Randomise Integer randomInt;
	@Randomise Boolean randomBool;
	@Randomise Double randomDouble;
	@Randomise Float randomFloat;
	@Randomise Byte randomByte;
	@Randomise boolean randomBooleanPrimitive;
	@Randomise long randomLongPrimitive;
	@Randomise int randomIntPrimitive;
	@Randomise float randomFloatPrimitive;
	@Randomise double randomDoublePrimitive;
	@Randomise byte randomBytePrimitive;
	
	@Before
	public void setup() throws Exception {
		randomiseFields(this);
	}
	
	@Test
	public void shouldGiveMeANonNullString() throws Exception {
		assertThat(randomString_1, is(not(nullValue())));
		assertThat(randomString_2, is(not(nullValue())));
	}
	
	@Test
	public void shouldGiveMeDifferentValues() throws Exception {
		assertThat(randomString_1, is(not(randomString_2)));
	}
	
	@Test
	public void shouldGiveMeLotsOfDifferentRandomValues() throws Exception {
		Set<String> randomValues = new HashSet<String>();
		
		for (int i = 0; i < 1000; i++) {
			String s = randomise(String.class);
			randomValues.add(s);
		}
		
		assertThat(randomValues.size(), is(1000));
	}
	
	@Test
	public void shouldRandomiseLongs() throws Exception {
		Long l = randomise(Long.class);
		assertThat(l, is(not(nullValue())));
		assertThat(randomLong, is(not(nullValue())));
	}
	
	@Test
	public void shouldRandomiseIntegers() throws Exception {
		Integer i = randomise(Integer.class);
		assertThat(i, is(not(nullValue())));
		assertThat(randomInt, is(not(nullValue())));
	}
	
	@Test
	public void shouldRandomiseBooleans() throws Exception {
		Boolean b = randomise(Boolean.class);
		assertThat(b, is(not(nullValue())));
		assertThat(randomBool, is(not(nullValue())));
	}
	
	@Test
	public void shouldRandomiseDoubless() throws Exception {
		Double d = randomise(Double.class);
		assertThat(d, is(not(nullValue())));
		assertThat(randomDouble, is(not(nullValue())));
	}
	
	@Test
	public void shouldRandomiseFloats() throws Exception {
		Float f = randomise(Float.class);
		assertThat(f, is(not(nullValue())));
		assertThat(randomFloat, is(not(nullValue())));
	}
	
	@Test
	public void shouldRandomiseByte() throws Exception {
		Byte b = randomise(Byte.class);
		assertThat(b, is(not(nullValue())));
		assertThat(randomByte, is(not(nullValue())));
	}
	
	@Test
	public void randomisePrimitivesShouldNotThrowAnException() throws Exception {
		randomise(int.class);
		randomise(double.class);
		randomise(boolean.class);
		randomise(float.class);
		randomise(long.class);
		randomise(byte.class);
	}
	
	@Test
	public void shouldRandomiseIntegerWithinRange() throws Exception {
		Integer lower = 10;
		Integer upper = 20;
		Integer i = randomise(Integer.class, lower, upper);
		
		assertThat(i, is(greaterThanOrEqualTo(lower)));
		assertThat(i, is(lessThanOrEqualTo(upper)));
	}
	
	@Test
	public void shouldComplainIfLowerBoundIsMoreThanUpperBound() throws Exception {
		IllegalArgumentException caught = null;
		try {
			randomise(Integer.class, 10, 9);
		} catch (IllegalArgumentException e) {
			caught = e;
		}
		
		assertThat(caught, is(not(nullValue())));
	}
}
