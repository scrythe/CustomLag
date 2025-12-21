package dev.scrythe.customlag.config;

import dev.scrythe.customlag.config.retentions.Comment;
import dev.scrythe.customlag.config.retentions.Comments;
import dev.scrythe.customlag.config.retentions.ConfigOption;
import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.except.TomlException;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigHandler {
    public static CustomLagConfig loadConfig(Path configFile) {
        JToml toml = JToml.jToml();

        try {
            TomlDocument doc = toml.read(configFile);
            return toml.fromToml(CustomLagConfig.class, doc);
        } catch (TomlException | IllegalArgumentException ignored) {
            return new CustomLagConfig();
        }
    }

    public static void writeConfig(Path configFile, CustomLagConfig config) {
        try (FileWriter writer = new FileWriter(configFile.toFile())) {
            String configTomlString = getConfigTomlString(config);
            writer.write(configTomlString);
        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getConfigTomlString(CustomLagConfig config) throws IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        List<Field> tableTypeFields = new ArrayList<>() {
        };
        for (Field field : CustomLagConfig.class.getFields()) {
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
