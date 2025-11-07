package com.example.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;

import java.util.List;
import java.util.Map;

@Configuration
public class R2dbcConfig {

    private final ObjectMapper mapper = new ObjectMapper();

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        // ✅ Usa el dialecto para Postgres
        var dialect = PostgresDialect.INSTANCE;
        var converters = List.of(
                new JsonToMapConverter(mapper),
                new MapToJsonConverter(mapper)
        );
        var storeConversions = CustomConversions.StoreConversions.of(dialect.getSimpleTypeHolder(), converters);
        return new R2dbcCustomConversions(storeConversions, converters);
    }

    @ReadingConverter
    static class JsonToMapConverter implements Converter<Json, Map<String, Object>> {
        private final ObjectMapper mapper;
        JsonToMapConverter(ObjectMapper mapper){ this.mapper = mapper; }

        @Override
        public Map<String, Object> convert(Json source) {
            try { return mapper.readValue(source.asString(), Map.class); }
            catch (Exception e){ throw new RuntimeException("Error reading JSONB", e); }
        }
    }

    @WritingConverter
    static class MapToJsonConverter implements Converter<Map<String, Object>, Json> {
        private final ObjectMapper mapper;
        MapToJsonConverter(ObjectMapper mapper){ this.mapper = mapper; }

        @Override
        public Json convert(Map<String, Object> source) {
            try { return Json.of(mapper.writeValueAsString(source)); }
            catch (Exception e){ throw new RuntimeException("Error writing JSONB", e); }
        }
    }
}
