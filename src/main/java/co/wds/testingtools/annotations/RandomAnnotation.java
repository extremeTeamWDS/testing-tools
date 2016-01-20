package co.wds.testingtools.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RandomAnnotation {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD})
	public @interface Randomise {

	}
	
	private static final Random randomiser = new Random();
	
	public static void randomiseFields(Object testClass) throws IllegalArgumentException, IllegalAccessException {
		Class<? extends Object> theClass = testClass.getClass();

		Set<Field> fieldsToBeRandomised = extractFieldsToBeRandomised(theClass);
	
		randomise(testClass, fieldsToBeRandomised);
	}

	private static Set<Field> extractFieldsToBeRandomised(Class<? extends Object> theClass) {
		Set<Field> fieldsToBeRandomised = new HashSet<Field>();
		Field[] fields = theClass.getDeclaredFields();
		for (Field field : fields ) {
			if (null != field.getAnnotation(Randomise.class)) {
				fieldsToBeRandomised.add(field);
			}
		}
		
		return fieldsToBeRandomised;
	}

	private static void randomise(Object testClass, Set<Field> fieldsToBeRandomised) throws IllegalArgumentException, IllegalAccessException {
		for (Field field: fieldsToBeRandomised) {
			field.set(testClass, randomise(field.getType()));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <S> S randomise(Class<S> classToRandomise) {
		if (classToRandomise.equals(Long.class) || classToRandomise.equals(long.class)) {
			return (S) (Long) randomiser.nextLong();
		} else if (classToRandomise.equals(Integer.class) || classToRandomise.equals(int.class)) {
			return (S) (Integer) randomiser.nextInt();
		} else if (classToRandomise.equals(Boolean.class) || classToRandomise.equals(boolean.class)) {
			return (S) (Boolean) randomiser.nextBoolean();
		} else if (classToRandomise.equals(Double.class) || classToRandomise.equals(double.class)) {
			return (S) (Double) randomiser.nextDouble();
		} else if (classToRandomise.equals(Float.class) || classToRandomise.equals(float.class)) {
			return (S) (Float) randomiser.nextFloat();
		} else if (classToRandomise.equals(Byte.class) || classToRandomise.equals(byte.class)) {
			return (S) (Byte)((Integer)randomiser.nextInt(Byte.MAX_VALUE)).byteValue();
		} else if (classToRandomise.equals(String.class)) {
			return (S) String.format("%h",  randomiser.nextLong());
		} else {
			throw new IllegalArgumentException("Unsupported Class " + classToRandomise);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <S> S randomise(Class<S> classToRandomise, S lower, S upper) {
		if (classToRandomise.equals(Integer.class) || classToRandomise.equals(int.class)) {
			if ((Integer)lower > (Integer)upper) {
				throw new IllegalArgumentException("Lower bound cannot be greater than upper bound");
			}
			int bound = (Integer)upper - (Integer)lower;
			Integer i = randomiser.nextInt(bound) + (Integer)lower;
			return (S) i;
		} else {
			throw new IllegalArgumentException("Unsupported Class " + classToRandomise);
		}
	}

}
