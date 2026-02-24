package cn.taotxi.Superman.util;

import java.util.List;
import java.util.stream.Collectors;

public class StringUtils {
    public static String withDefaultNameSpace(String name) {
        if (name.contains(":")) {
            return name;
        }
        return "minecraft:" + name;
    }
    public static List<String> withDefaultNameSpace(List<String> names) {
        System.out.println(names);
        System.out.println(names.stream().map(StringUtils::withDefaultNameSpace).collect(Collectors.toList()));
        return names.stream().map(StringUtils::withDefaultNameSpace).collect(Collectors.toList());
    }
}
