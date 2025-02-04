package com.github.cichyvx.mapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ObjectMapperTest {
    private JsonToPropertyMapper jsonToPropertyMapper = new JsonToPropertyMapper();
    private ObjectMapper objectMapper = new ObjectMapper(jsonToPropertyMapper);

    private final static String JSON_1 = """
            {
                "b" : "1",
                "so" : "1",
                "i" : "2",
                "l" : "3",
                "f" : "0.1",
                "d" : "0.2",
                "c" : "c",
                "st" : "txt",
                "bool" : "false",
                "tenum": "VAL"
            }
            """;

    public record Test1(byte b,
                        short so,
                        int i,
                        long l,
                        float f,
                        double d,
                        char c,
                        String st,
                        boolean bool,
                        TENUM tenum) {}

    public enum TENUM {
        VAL
    }

    @Test
    public void test1() throws Exception {
        Test1 test1 = objectMapper.fromJson(JSON_1, Test1.class);
        assertNotNull(test1);
    }

    private final static String JSON_2 = """
            {
                "nestedObj" : {
                    "subString" : "bottom_text"
                }
            }
            """;

    public record Test2(Test2Nested nestedObj) {
        public record Test2Nested(String subString) {}
    }

    @Test
    public void test2() throws Exception {
        Test2 test = objectMapper.fromJson(JSON_2, Test2.class);
        assertNotNull(test);
    }

    private final static String JSON_3 = """
            {
                "b" : ["1", "2"],
                "so" : ["1", "2"],
                "i" : ["1", "2"],
                "l" : ["1", "2"],
                "f" : ["1.2", "3.4"],
                "d" : ["1.2", "3.4"],
                "c" : ["c", "h"],
                "st" : ["va1", "val2"],
                "bool" : ["true", "false"],
                "tenum": ["VAL1", "VAL2"]
            }
            """;

    public record Test3(byte[] b,
                        short[] so,
                        int[] i,
                        long[] l,
                        float[] f,
                        double[] d,
                        char[] c,
                        String[] st,
                        boolean[] bool,
                        TENUM[] tenum) {
        public enum TENUM {
            VAL1, VAL2
        }
    }

    @Test
    public void test3() throws Exception {
        Test3 test = objectMapper.fromJson(JSON_3, Test3.class);
        assertNotNull(test);
    }

    private final static String JSON_4 = """
            {
                "hi": [
                    {
                        "x": "x"
                    },
                    {
                        "x": "y"
                    }
                ]
            }
            """;

    public record Test4(Test4Nested[] hi) {
        public record Test4Nested (String x){}
    }

    @Test
    public void test4() throws Exception {
        Test4 test = objectMapper.fromJson(JSON_4, Test4.class);
        assertNotNull(test);
    }

    public record Output1 (String hello) { }
    public record Output2 (Output1 output) { }
    public record Output3 (int[] ints, Output1[] outputs) { }

    @Test
    public void outputTest1() {
        var result = objectMapper.toJson(new Output1("hello"));
        assertEquals("{\"hello\":\"hello\"}", result);
    }

    @Test
    public void outputTest2() {
        var result = objectMapper.toJson(new Output2(new Output1("hello")));
        assertEquals("{\"output\":{\"hello\":\"hello\"}}", result);
    }

    @Test
    public void outputTest3() {
        var result = objectMapper.toJson(new Output3(new int[] {1,2}, new Output1[]{new Output1("v1"), new Output1("v2")}));
        assertEquals("{\"ints\":[\"1\",\"2\"],\"outputs\":[{\"hello\":\"v1\"},{\"hello\":\"v2\"}]}", result);
    }


}
