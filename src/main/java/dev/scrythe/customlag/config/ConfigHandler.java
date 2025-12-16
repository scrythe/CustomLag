package dev.scrythe.customlag.config;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.table.TomlTable;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigHandler {
    public static CustomLagConfigTest loadConfig(Path configFile) {
        JToml toml = JToml.jToml();

        try {
            TomlDocument doc = toml.read(configFile);
            return toml.fromToml(CustomLagConfigTest.class, doc);
        } catch (TomlException | IllegalArgumentException ignored) {
            return new CustomLagConfigTest();
        }
    }

    public static void writeConfig(Path configFile, CustomLagConfigTest config) {
        try (FileWriter writer = new FileWriter(configFile.toFile())) {
            String configTomlString = getConfigTomlString(config);
            writer.write(configTomlString);
        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getConfigTomlString(CustomLagConfigTest config) throws IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        List<Field> tableTypeFields = new ArrayList<>() {
        };
        for (Field field : CustomLagConfigTest.class.getFields()) {
            if (!field.isAnnotationPresent(ConfigOption.class)) continue;
            if (field.getGenericType() instanceof ParameterizedType) {
                tableTypeFields.add(field);
                continue;
            }
            addComments(field, sb);
            sb.append("%s = %s\n\n".formatted(field.getName(), field.get(config)));
        }

        for (Field field : tableTypeFields) {
            addComments(field, sb);
            sb.append("[%s]\n".formatted(field.getName()));
            if (field.get(config) instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    sb.append("%s = %s\n".formatted(entry.getKey(), entry.getValue()));
                }
            }
        }

        if (tableTypeFields.isEmpty()) {
            sb.setLength(sb.length() - 2);
        } else {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    private static void addComments(Field field, StringBuilder sb) {
        Comment[] comments = null;
        if (field.isAnnotationPresent(Comment.class)) {
            comments = new Comment[]{field.getAnnotation(Comment.class)};
        } else if (field.isAnnotationPresent(Comments.class)) {
            comments = field.getAnnotation(Comments.class).value();
        }
        if (comments == null) return;
        for (Comment comment : comments) {
            sb.append("# %s\n".formatted(comment.value()));
        }
    }
}
