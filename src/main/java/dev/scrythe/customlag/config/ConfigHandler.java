package dev.scrythe.customlag.config;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.except.TomlIOException;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConfigHandler {
    public static void loadConfig(Path configFile) throws IOException {
//        JToml toml = JToml.jToml();
//        TomlDocument doc;
//        try {
//            doc = toml.read(configFile);
//        } catch (TomlException ignored) {
//            return;
//        }
//
//        for (Field field : CustomLagConfig.class.getFields()) {
//            if (!field.isAnnotationPresent(ConfigOption.class)) continue;
//            if (!doc.contains(field.getName())) continue;
//            TomlValue fieldValue = doc.get(field.getName());
//            try {
//                field.set(null, fieldValue);
//            } catch (IllegalArgumentException | IllegalAccessException ignored) {}
//        }
    }

    public static void writeConfig(Path configFile) {
//        JToml toml = JToml.jToml();
        CustomLagConfigTest config = new CustomLagConfigTest();
        config.playerLag.put("hm", 2);
        config.playerLag.put("hms", 2);
        
//        TomlTable table = toml.toToml(CustomLagConfigTest.class, config);
//        toml.write(configFile, table);
//        System.out.println("hm");

        try {
            writeConfig3(configFile, config);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeConfig2(Path configFile, CustomLagConfigTest config) throws IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        for (Field field: CustomLagConfigTest.class.getFields()) {
            if (field.isAnnotationPresent(Comment.class)) {
                Comment comment = field.getAnnotation(Comment.class);
                sb.append("# %s\n".formatted(comment.value()));
            } else {
                Comments comments = field.getAnnotation(Comments.class);
                for (Comment comment: comments.value()){
                    sb.append("# %s\n".formatted(comment.value()));
                }
            }

            if (field.getGenericType() instanceof ParameterizedType) {
                Type[] mapTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                sb.append("[%s]\n".formatted(field.getName()));
                Map<String, Object> map = (Map<String, Object>) field.get(config);
                for (Map.Entry<String, Object> entry: map.entrySet()) {
                    sb.append("%s = %s\n".formatted(entry.getKey(), entry.getValue()));
                }
            } else {
                sb.append("%s = %s\n\n".formatted(field.getName(), field.get(config)));
            }
        }
        try (FileWriter writer = new FileWriter(configFile.toFile())) {
            writer.write(sb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeConfig3(Path configFile, CustomLagConfigTest config) throws IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        List<Field> tableTypeFields = new ArrayList<>() {};
        for (Field field: CustomLagConfigTest.class.getFields()) {
            if (field.getGenericType() instanceof ParameterizedType) {
                tableTypeFields.add(field);
                continue;
            }
            addComments(field, sb);
            sb.append("%s = %s\n\n".formatted(field.getName(), field.get(config)));
        }

        for (Field field: tableTypeFields) {
            addComments(field, sb);
            sb.append("[%s]\n".formatted(field.getName()));
            Map<String, Object> map = (Map<String, Object>) field.get(config);
            for (Map.Entry<String, Object> entry: map.entrySet()) {
                sb.append("%s = %s\n".formatted(entry.getKey(), entry.getValue()));
            }
        }

        try (FileWriter writer = new FileWriter(configFile.toFile())) {
            writer.write(sb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addComments(Field field, StringBuilder sb) {
        if (field.isAnnotationPresent(Comment.class)) {
            Comment comment = field.getAnnotation(Comment.class);
            sb.append("# %s\n".formatted(comment.value()));
        } else {
            Comments comments = field.getAnnotation(Comments.class);
            for (Comment comment: comments.value()){
                sb.append("# %s\n".formatted(comment.value()));
            }
        }
    }
}
