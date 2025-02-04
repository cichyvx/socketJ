package com.github.cichyvx.mapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonToPropertyMapperTest {

    private final static String JSON_1 = """
            {
                "hi" : "hi"
            }
            """;

    private final static String JSON_2 = """
            {
                "hi" : "hi",
                "hello" : "hello"
            }
            """;

    private final static String JSON_3 = """
            {
                "hi" : "hi",
                "hello" : "hello",
                "key" : "value"
            }
            """;

    private final static String JSON_4 = """
            {"hi" : "hi"}
            """;

    private final static String JSON_5 = """
            {
                "hi" "hi"
            }
            """;

    private final static String JSON_6 = """
            {
                "hi":"hi"
                "x":"x"
            }
            """;

    private final static String JSON_7 = """
            {"hi":["v1","v2"]}
            """;

    private final static String JSON_8 = """
            {"hi":[{"x":"x"},{"x":"y"}]}
            """;

    private final static String JSON_9 = """
            {"hi":["v1","v2"]}
            """;

    private final JsonToPropertyMapper mapper = new JsonToPropertyMapper();

    @Test
    public void test1() {
        var result = mapper.mapJson2Map(JSON_1);

        assertTrue(result.containsKey("hi"));
        assertEquals("hi", result.get("hi").getValue().get(0));

        result = mapper.mapJson2Map(JSON_4);

        assertTrue(result.containsKey("hi"));
        assertEquals("hi", result.get("hi").getValue().get(0));
    }


    @Test
    public void test2() {
        var result = mapper.mapJson2Map(JSON_2);

        assertTrue(result.containsKey("hi"));
        assertEquals("hi", result.get("hi").getValue().get(0));

        assertTrue(result.containsKey("hello"));
        assertEquals("hello", result.get("hello").getValue().get(0));
    }

    @Test
    public void test3() {
        var result = mapper.mapJson2Map(JSON_3);

        assertTrue(result.containsKey("hi"));
        assertEquals("hi", result.get("hi").getValue().get(0));

        assertTrue(result.containsKey("hello"));
        assertEquals("hello", result.get("hello").getValue().get(0));

        assertTrue(result.containsKey("key"));
        assertEquals("value", result.get("key").getValue().get(0));
    }

    @Test
    public void test4() {
        assertThrows(RuntimeException.class, () -> mapper.mapJson2Map(JSON_5));
        assertThrows(RuntimeException.class, () -> mapper.mapJson2Map(JSON_6));
    }

    @Test
    public void test5() {
        var result = mapper.mapJson2Map(JSON_7);

        assertTrue(result.containsKey("hi"));
        var hi = result.get("hi").getValue();

        assertEquals("v1", hi.get(0));
        assertEquals("v2", hi.get(1));
    }

    @Test
    public void test6() {
        var result = mapper.mapJson2Map(JSON_8);

        assertTrue(result.containsKey("hi"));
        assertEquals("x", result.get("hi").getNestedProperty().get(0).get("x").getValue().get(0));
        assertEquals("y", result.get("hi").getNestedProperty().get(1).get("x").getValue().get(0));
    }


    @Test
    public void test7() {
        var result = mapper.mapJson2Map(JSON_9);
        assertTrue(result.containsKey("hi"));
        assertEquals("v1", result.get("hi").getValue().get(0));
        assertEquals("v2", result.get("hi").getValue().get(1));
    }

    private final static String JSON_10 = """
            {
                 "plain": [
                     "v1",
                     "v2"
                 ],
                 "normal": "normal_val",
                 "nested": [
                     {
                         "plain": [
                             "v1",
                             "v2"
                         ],
                         "normal": "normal_val"
                     },
                     {
                         "plain": [
                             "v1",
                             "v2"
                         ],
                         "normal": "normal_val"
                     }
                 ],
                 "extra_nested": {
                     "extra_nested_v2": {
                         "value": "success"
                     }
                 },
                 "extra_nested2": {
                     "extra_nested_v22": {
                         "value2": "success"
                     }
                 },
                 "extra_nested3": {
                    "plain": [
                             "v1",
                             "v2"
                         ]
                 }
             }
            """;

    @Test
    public void test8() {
        var result = mapper.mapJson2Map(JSON_10);
        assertTrue(result.containsKey("plain"));
        assertTrue(result.containsKey("normal"));
        assertTrue(result.containsKey("nested"));

        assertEquals("v1", result.get("plain").getValue().get(0));
        assertEquals("v2", result.get("plain").getValue().get(1));

        assertEquals("normal_val", result.get("normal").getValue().get(0));

        assertEquals("v1", result.get("nested").getNestedProperty().get(0).get("plain").getValue().get(0));
        assertEquals("v2", result.get("nested").getNestedProperty().get(0).get("plain").getValue().get(1));

        assertEquals("normal_val", result.get("nested").getNestedProperty().get(0).get("normal").getValue().get(0));

        assertEquals("v1", result.get("nested").getNestedProperty().get(1).get("plain").getValue().get(0));
        assertEquals("v2", result.get("nested").getNestedProperty().get(1).get("plain").getValue().get(1));

        assertEquals("normal_val", result.get("nested").getNestedProperty().get(1).get("normal").getValue().get(0));


        assertEquals("success", result.get("extra_nested").getNestedProperty().get(0).get("extra_nested_v2").getNestedProperty().get(0).get("value").getValue().get(0));
        assertEquals("success", result.get("extra_nested2").getNestedProperty().get(0).get("extra_nested_v22").getNestedProperty().get(0).get("value2").getValue().get(0));

        assertEquals("v1", result.get("extra_nested3").getNestedProperty().get(0).get("plain").getValue().get(0));
        assertEquals("v2", result.get("extra_nested3").getNestedProperty().get(0).get("plain").getValue().get(1));
    }


}
