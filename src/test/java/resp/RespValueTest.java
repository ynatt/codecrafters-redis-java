package resp;

import org.junit.jupiter.api.Test;
import vel.vn.resp.RespValue;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RespValueTest {

    @Test
    void testSimpleStringEncode() {
        RespValue.SimpleString simpleString = new RespValue.SimpleString("OK");
        assertEquals("+OK\r\n", simpleString.encode());
    }

    @Test
    void testSimpleStringDecode() throws IOException {
        String input = "+OK\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.SimpleString.class, result);
        assertEquals("OK", ((RespValue.SimpleString) result).value());
    }

    @Test
    void testSimpleStringDecodeWithMessage() throws IOException {
        String input = "+Hello World\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.SimpleString.class, result);
        assertEquals("Hello World", ((RespValue.SimpleString) result).value());
    }

    @Test
    void testErrorEncode() {
        RespValue.Error error = new RespValue.Error("ERR", "Something went wrong");
        assertEquals("-ERR Something went wrong\r\n", error.encode());
    }

    @Test
    void testErrorEncodeWithEmptyMessage() {
        RespValue.Error error = new RespValue.Error("ERR", "");
        assertEquals("-ERR\r\n", error.encode());
    }

    @Test
    void testErrorDecode() throws IOException {
        String input = "-ERR unknown command\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.Error.class, result);
        RespValue.Error error = (RespValue.Error) result;
        assertEquals("ERR", error.errorType());
        assertEquals("unknown command", error.message());
    }

    @Test
    void testErrorDecodeWithoutMessage() throws IOException {
        String input = "-WRONGTYPE\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.Error.class, result);
        RespValue.Error error = (RespValue.Error) result;
        assertEquals("WRONGTYPE", error.errorType());
        assertEquals("", error.message());
    }

    @Test
    void testLongEncode() {
        RespValue.Long longValue = new RespValue.Long(1000);
        assertEquals(":1000\r\n", longValue.encode());
    }

    @Test
    void testLongEncodeZero() {
        RespValue.Long longValue = new RespValue.Long(0);
        assertEquals(":0\r\n", longValue.encode());
    }

    @Test
    void testLongEncodeNegative() {
        RespValue.Long longValue = new RespValue.Long(-123);
        assertEquals(":-123\r\n", longValue.encode());
    }

    @Test
    void testLongDecode() throws IOException {
        String input = ":1000\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.Long.class, result);
        assertEquals(1000, ((RespValue.Long) result).value());
    }

    @Test
    void testLongDecodeNegative() throws IOException {
        String input = ":-42\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.Long.class, result);
        assertEquals(-42, ((RespValue.Long) result).value());
    }

    @Test
    void testBulkStringEncode() {
        RespValue.BulkString bulkString = new RespValue.BulkString("hello");
        assertEquals("$5\r\nhello\r\n", bulkString.encode());
    }

    @Test
    void testBulkStringEncodeEmpty() {
        RespValue.BulkString bulkString = new RespValue.BulkString("");
        assertEquals("$0\r\n\r\n", bulkString.encode());
    }

    @Test
    void testBulkStringDecode() throws IOException {
        String input = "$5\r\nhello\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.BulkString.class, result);
        assertEquals("hello", ((RespValue.BulkString) result).value());
    }

    @Test
    void testBulkStringDecodeEmpty() throws IOException {
        String input = "$0\r\n\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.BulkString.class, result);
        assertEquals("", ((RespValue.BulkString) result).value());
    }

    @Test
    void testBulkStringDecodeWithSpaces() throws IOException {
        String input = "$11\r\nhello world\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.BulkString.class, result);
        assertEquals("hello world", ((RespValue.BulkString) result).value());
    }

    @Test
    void testBulkStringWithCRLFInContent() {
        // Critical test: bulk strings can contain CRLF in their data
        RespValue.BulkString bulkString = new RespValue.BulkString("hello\r\nworld");
        assertEquals("$12\r\nhello\r\nworld\r\n", bulkString.encode());
    }

    @Test
    void testBulkStringDecodeWithCRLFInContent() throws IOException {
        // Critical test: bulk strings can contain CRLF in their data
        String input = "$12\r\nhello\r\nworld\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.BulkString.class, result);
        assertEquals("hello\r\nworld", ((RespValue.BulkString) result).value());
    }

    @Test
    void testNullEncode() {
        RespValue.Null nullValue = new RespValue.Null();
        assertEquals("$-1\r\n", nullValue.encode());
    }

    @Test
    void testNullDecode() throws IOException {
        String input = "$-1\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.Null.class, result);
    }

    @Test
    void testNullArrayDecode() throws IOException {
        // Null array in RESP is represented as *-1\r\n
        String input = "*-1\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.Null.class, result);
    }

    @Test
    void testArrayEncode() {
        RespValue.Array array = new RespValue.Array(List.of(
            new RespValue.SimpleString("OK"),
            new RespValue.Long(123)
        ));
        assertEquals("*2\r\n+OK\r\n:123\r\n", array.encode());
    }

    @Test
    void testArrayEncodeEmpty() {
        RespValue.Array array = new RespValue.Array(List.of());
        assertEquals("*0\r\n", array.encode());
    }

    @Test
    void testArrayEncodeWithBulkStrings() {
        RespValue.Array array = new RespValue.Array(List.of(
            new RespValue.BulkString("GET"),
            new RespValue.BulkString("key")
        ));
        assertEquals("*2\r\n$3\r\nGET\r\n$3\r\nkey\r\n", array.encode());
    }

    @Test
    void testArrayDecode() throws IOException {
        String input = "*2\r\n+OK\r\n:123\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.Array.class, result);
        RespValue.Array array = (RespValue.Array) result;
        assertEquals(2, array.elements().size());

        assertInstanceOf(RespValue.SimpleString.class, array.elements().get(0));
        assertEquals("OK", ((RespValue.SimpleString) array.elements().get(0)).value());

        assertInstanceOf(RespValue.Long.class, array.elements().get(1));
        assertEquals(123, ((RespValue.Long) array.elements().get(1)).value());
    }

    @Test
    void testArrayDecodeWithBulkStrings() throws IOException {
        String input = "*2\r\n$3\r\nGET\r\n$3\r\nkey\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.Array.class, result);
        RespValue.Array array = (RespValue.Array) result;
        assertEquals(2, array.elements().size());

        assertInstanceOf(RespValue.BulkString.class, array.elements().get(0));
        assertEquals("GET", ((RespValue.BulkString) array.elements().get(0)).value());

        assertInstanceOf(RespValue.BulkString.class, array.elements().get(1));
        assertEquals("key", ((RespValue.BulkString) array.elements().get(1)).value());
    }

    @Test
    void testArrayDecodeEmpty() throws IOException {
        String input = "*0\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.Array.class, result);
        RespValue.Array array = (RespValue.Array) result;
        assertEquals(0, array.elements().size());
    }

    @Test
    void testNestedArrayEncode() {
        RespValue.Array innerArray = new RespValue.Array(List.of(
            new RespValue.Long(1),
            new RespValue.Long(2)
        ));
        RespValue.Array outerArray = new RespValue.Array(List.of(
            new RespValue.SimpleString("OK"),
            innerArray
        ));
        assertEquals("*2\r\n+OK\r\n*2\r\n:1\r\n:2\r\n", outerArray.encode());
    }

    @Test
    void testNestedArrayDecode() throws IOException {
        String input = "*2\r\n+OK\r\n*2\r\n:1\r\n:2\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.Array.class, result);
        RespValue.Array outerArray = (RespValue.Array) result;
        assertEquals(2, outerArray.elements().size());

        assertInstanceOf(RespValue.SimpleString.class, outerArray.elements().get(0));
        assertEquals("OK", ((RespValue.SimpleString) outerArray.elements().get(0)).value());

        assertInstanceOf(RespValue.Array.class, outerArray.elements().get(1));
        RespValue.Array innerArray = (RespValue.Array) outerArray.elements().get(1);
        assertEquals(2, innerArray.elements().size());
        assertEquals(1, ((RespValue.Long) innerArray.elements().get(0)).value());
        assertEquals(2, ((RespValue.Long) innerArray.elements().get(1)).value());
    }

    @Test
    void testRedisCommandSetEncode() {
        // Real Redis command: SET mykey myvalue
        RespValue.Array command = new RespValue.Array(List.of(
            new RespValue.BulkString("SET"),
            new RespValue.BulkString("mykey"),
            new RespValue.BulkString("myvalue")
        ));
        assertEquals("*3\r\n$3\r\nSET\r\n$5\r\nmykey\r\n$7\r\nmyvalue\r\n", command.encode());
    }

    @Test
    void testRedisCommandSetDecode() throws IOException {
        // Real Redis command: SET mykey myvalue
        String input = "*3\r\n$3\r\nSET\r\n$5\r\nmykey\r\n$7\r\nmyvalue\r\n";
        RespValue result = RespValue.decode(input);
        assertInstanceOf(RespValue.Array.class, result);
        RespValue.Array array = (RespValue.Array) result;
        assertEquals(3, array.elements().size());
        assertEquals("SET", ((RespValue.BulkString) array.elements().get(0)).value());
        assertEquals("mykey", ((RespValue.BulkString) array.elements().get(1)).value());
        assertEquals("myvalue", ((RespValue.BulkString) array.elements().get(2)).value());
    }

    @Test
    void testArrayWithNullElement() {
        // Array containing null elements
        RespValue.Array array = new RespValue.Array(List.of(
            new RespValue.BulkString("foo"),
            new RespValue.Null(),
            new RespValue.BulkString("bar")
        ));
        assertEquals("*3\r\n$3\r\nfoo\r\n$-1\r\n$3\r\nbar\r\n", array.encode());
    }

    @Test
    void testConstantOK() {
        assertEquals("+OK\r\n", RespValue.OK.encode());
    }

    @Test
    void testConstantNULL() {
        assertEquals("$-1\r\n", RespValue.NULL.encode());
    }
}