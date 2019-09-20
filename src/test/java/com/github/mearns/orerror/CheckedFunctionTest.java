package com.github.mearns.orerror;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class CheckedFunctionTest {
    @Test
    public void testGettingValueFromOf() throws Throwable {
        final String testValue = "test-value-123";
        final OrError<String, Throwable> uut = OrError.of(testValue);
        assertSame("Expected returned value to be the test value passed into `of`", testValue, uut.get());
    }

    @Test
    public void testGettingValueFromCaught() {
        final Exception testError = new Exception("Test");
        final OrError<String, Exception> uut = OrError.caught(testError);
        try {
            uut.get();
            fail("The get method should have thrown the test exception");
        } catch (Exception e) {
            assertSame("Expected thrown exception to be the test error passed to caught", testError, e);
        }
    }

    @Test
    public void testMappingValueFromOf() throws Throwable {
        final String testValue = "test-value-123";
        final OrError<String, Throwable> uut = OrError.of(testValue);
        final OrError<Integer, Throwable> res = uut.map(String::length);
        assertEquals("Expected `get` to return the mapped value, which should be the length of the test value",
                     Integer.valueOf(testValue.length()), res.get());
    }

    @Test
    public void testMappingFromCaught() {
        final Exception testError = new Exception("Test");
        final OrError<String, Exception> uut = OrError.caught(testError);
        final OrError<Integer, Exception> res = uut.map(String::length);
        try {
            res.get();
            fail("The get method should have thrown the test exception");
        } catch (Exception e) {
            assertSame("Expected thrown exception to be the test error passed to caught", testError, e);
        }
    }

    @Test
    public void testMappingValueWithCheckedFunctionFromOf() throws Throwable {
        final String testValue = "test-value-123";
        final OrError<String, Throwable> uut = OrError.of(testValue);
        final OrError<Integer, Throwable> res = uut.map(Throwable.class, String::length);
        assertEquals("Expected `get` to return the mapped value, which should be the length of the test value",
                     Integer.valueOf(testValue.length()), res.get());
    }

    @Test
    public void testMappingValueWithCheckedFunctionThatThrowsFromOf() {
        final String testValue = "test-value-123";
        final Exception testError = new Exception("Test");
        final OrError<String, Exception> uut = OrError.of(testValue);
        final OrError<Integer, Exception> res = uut.map(Exception.class, s -> {
            throw testError;
        });
        try {
            res.get();
            fail("The get method should have thrown the test exception");
        } catch (Exception e) {
            assertSame("Expected thrown exception to be the test error passed to caught", testError, e);
        }
    }

    @Test
    public void testWrappingCheckedFunctionThatReturns() throws Exception {
        final String testString = "test-value-123";
        final CheckedFunction<String, Integer, Exception> uut = String::length;
        final Function<String, OrError<Integer, Exception>> wrapped = OrError.wrap(Exception.class, uut);
        final OrError<Integer, Exception> result = wrapped.apply(testString);
        assertEquals("Expect wrapped function to return a wrapped value holding the result of applying the checked function",
            testString.length(), result.get().intValue());
    }

    @Test
    public void testWrappingCheckedFunctionThatThrows() {
        final String testString = "test-value-123";
        final Exception testError = new Exception("Test");
        final CheckedFunction<String, Integer, Exception> uut = s -> {
            throw testError;
        };
        final Function<String, OrError<Integer, Exception>> wrapped = OrError.wrap(Exception.class, uut);
        final OrError<Integer, Exception> result = wrapped.apply(testString);
        try {
            result.get();
            fail("The get method should have thrown the test exception");
        } catch (Exception e) {
            assertSame("Expected thrown exception to be the test error passed to caught", testError, e);
        }
    }

    @Test
    public void testUnpackingAListOfOfs() throws Exception {
        final String testValue1 = "test-value-123";
        final String testValue2 = "test-value-456";
        final List<OrError<String, ? extends Exception>> packed = Arrays.asList(
                OrError.of(testValue1),
                OrError.of(testValue2)
        );
        final OrError<List<String>, Exception> unpacked = OrError.unpack(packed);
        final List<String> res = unpacked.get();
        assertEquals(2, res.size());
        assertSame(testValue1, res.get(0));
        assertSame(testValue2, res.get(1));
    }
}
