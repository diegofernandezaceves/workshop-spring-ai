package dev.code.financial.assistant.ai.util;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.code.financial.assistant.ai.dto.TransactionDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TransactionCsvUtil {

    public static List<TransactionDto> read(InputStream inputStream) {

        CsvMapper csvMapper = new CsvMapper();
        csvMapper.registerModule(new JavaTimeModule());

        CsvSchema schema = csvMapper.schemaFor(TransactionDto.class)
                .withHeader()
                .withColumnSeparator(',');

        try (MappingIterator<TransactionDto> iterator = csvMapper.readerFor(TransactionDto.class)
                .with(schema)
                .readValues(inputStream)) {

            return iterator.readAll();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
