package com.github.cichyvx.mapper;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class JsonToPropertyMapper {

    public Map<String, ParsedProperty> mapJson2Map(String json) {
        Map<String, ParsedProperty> map = new HashMap<>();

        char[] chars = json.toCharArray();
        Helper helper = new Helper();

        for (int i = 0; i < chars.length; i++) {
            if (helper.isBeforeFirstKey) {
                i = processFirst(i, chars, helper);
            } else if (helper.isKeyProperty) {
                i = processKey(i, chars, helper);
            } else if (helper.isBetweenKeyAndValueProperties) {
                i = processBetweenKeyAndValueProperties(i, chars, helper);
            } else if (helper.isValueProperty) {
                if (isArrayType(i, chars, helper)) {
                    if (helper.objectTypeArray) {
                        i = procesObjectArrayValue(chars, map, helper);
                    } else {
                        i = processNonObjectArrayType(helper, chars, map);
                    }
                } else if (helper.isObjectTypeValue) {
                  i = processObjectValue(i, chars, map, helper);
                } else {
                    i = processSingleValue(i, chars, helper);
                    map.put(helper.key.toString(), new ParsedProperty(helper.value.toString()));
                }

                helper.key = new StringBuilder();
                helper.value = new StringBuilder();
                helper.objectTypeArray = false;
                helper.arrayStartIndex = -1;
                helper.isAfterValueProperty = true;
                helper.isValueProperty = false;
                helper.isBetweenKeyAndValueProperties = true;
                helper.isObjectTypeValue = false;

            }

        }

        return map;
    }

    private int processObjectValue(int i, char[] chars, Map<String, ParsedProperty> map, Helper helper) {
        int j = i;

        int open = 1;
        int close = 0;
        StringBuilder subJson = new StringBuilder("{");
        for (; j < chars.length; j++) {
            if (chars[j] == '{') open++;
            if (chars[j] == '}') close++;
            subJson.append(chars[j]);
            if (open == close) {
                break;
            }
        }

        var subObject = this.mapJson2Map(subJson.toString());
        var val = map.get(helper.key.toString());
        if (val == null) {
            map.put(helper.key.toString(), new ParsedProperty(subObject));
        } else {
            val.appendNested(subObject);
        }
        return j;
    }

    private int procesObjectArrayValue(char[] chars, Map<String, ParsedProperty> map, Helper helper) {
        List<String> subJsonList = new LinkedList<>();
        int j = helper.arrayStartIndex + 1;
        int open = 1;
        int close = 0;

        int subOpen = 0;
        int subClose = 0;

        int subOpenIndex = 0;

        for (; j < chars.length; j++) {

            if (chars[j] == '[') open++;
            if (chars[j] == ']') close++;
            if (chars[j] == '}') subClose++;
            if (chars[j] == '{') {
                if (subOpen == 0) {
                    subOpenIndex = j;
                }
                subOpen++;
            }


            if (subOpen == subClose && subOpen != 0) {
                StringBuilder subJson = new StringBuilder();
                for (int k = subOpenIndex; k <= j; k++) {
                    subJson.append(chars[k]);
                }

                subJsonList.add(subJson.toString());
                subOpenIndex = 0;
                subOpen = 0;
                subClose = 0;
            }

            if (open == close) {
                break;
            }
        }

        for (String subJson : subJsonList) {
            var parsed = mapJson2Map(subJson);
            var val = map.get(helper.key.toString());

            if (val == null) {
                map.put(helper.key.toString(), new ParsedProperty(parsed));
            } else {
                val.appendNested(parsed);
            }
        }

        return j;
    }

    private int processSingleValue(int i, char[] chars, Helper helper) {
        int j = i;
        for (; j < chars.length && chars[j] != '"'; j++) {
            helper.value.append(chars[j]);
        }

        return j;
    }

    private int processNonObjectArrayType(Helper helper, char[] chars, Map<String, ParsedProperty> map) {
        int j = helper.arrayStartIndex + 1;
        boolean started = false;
        for (; j < chars.length; j++) {
            if (!started && chars[j] == '"') {
                started = true;
            } else if (chars[j] == ']') {
                break;
            } else if (started){
                if (chars[j] == '"') {
                    started = false;
                    var val = map.get(helper.key.toString());
                    if (val == null) {
                        map.put(helper.key.toString(), new ParsedProperty(helper.value.toString()));
                    } else {
                        map.get(helper.key.toString()).append(helper.value.toString());
                    }
                    helper.value = new StringBuilder();
                } else {
                    helper.value.append(chars[j]);
                }
            }
        }

        return j;
    }

    private boolean isArrayType(int i, char[] chars, Helper helper) {
        int j = i;

        for (; j < chars.length && chars[j] != '"'; j++) {}

        for (; j >= 0; j--) {
            if (chars[j] == '[') {
                helper.arrayStartIndex = j;
                return true;
            } else if (chars[j] == ':') {
                return false;
            } else if (chars[j] == '{') {
                helper.objectTypeArray = true;
            }
        }

        throw new RuntimeException();
    }

    private int processBetweenKeyAndValueProperties(int i, char[] chars, Helper helper) {
        int j = i;
        boolean haveColon = false;
        boolean haveComma = false;
        for (; j < chars.length && chars[j] != '"' & chars[j] != '[' && chars[j] != '{'; j++) {
            if (chars[j] == ':') {
                if (haveColon) {
                    throw new RuntimeException();
                } else {
                    haveColon = true;
                }
            } else if (chars[j] == ',') {
                if (haveComma) {
                    throw new RuntimeException();
                } else {
                    haveComma = true;
                }
            }
        }
        if (!haveColon && j != chars.length && helper.isAfterKeyProperty) {
            throw new RuntimeException();
        } else if (!haveComma && j != chars.length && helper.isAfterValueProperty) {
            throw new RuntimeException();
        }

        if (helper.isAfterValueProperty) {
            helper.isKeyProperty = true;
        } else {
            helper.isValueProperty = true;
            if (chars[j] == '{') {
                helper.isObjectTypeValue = true;
            }
        }

        helper.isBetweenKeyAndValueProperties = false;
        helper.isAfterValueProperty = false;
        helper.isAfterKeyProperty = false;

        return j;
    }

    private int processFirst(int i, char[] chars, Helper helper) {
        int j = i;
        for (; j < chars.length && chars[j] != '"'; j++) {
            validFirstBrace(chars, j, helper);
        }
        helper.isBeforeFirstKey = false;
        helper.isKeyProperty = true;
        return j;
    }

    private int processKey(int i, char[] chars, Helper helper) {
        int j = i;
        for (; j < chars.length && chars[j] != '"'; j++) {
            helper.key.append(chars[j]);
        }
        helper.isKeyProperty = false;
        helper.isBetweenKeyAndValueProperties = true;
        helper.isAfterKeyProperty = true;
        return j;
    }

    private void validFirstBrace(char[] chars, int j, Helper helper) {
        if (chars[j] == '{') {
            if (helper.openBrace) {
                throw new RuntimeException();
            } else {
                helper.openBrace = true;
            }
        }
    }

    private static class Helper {
        boolean isBeforeFirstKey = true;
        boolean openBrace = false;
        boolean isKeyProperty = false;
        boolean isBetweenKeyAndValueProperties = false;
        boolean isValueProperty = false;
        boolean isAfterValueProperty = false;
        boolean isAfterKeyProperty = false;
        boolean isObjectTypeValue = false;

        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();

        int arrayStartIndex;
        boolean objectTypeArray = false;
    }

}
