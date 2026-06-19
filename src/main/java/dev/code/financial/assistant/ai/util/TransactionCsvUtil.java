package dev.code.financial.assistant.ai.util;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.code.financial.assistant.ai.model.Transaction;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TransactionCsvUtil {

    public static List<Transaction> read(InputStream inputStream) {

        CsvMapper csvMapper = new CsvMapper();
        csvMapper.registerModule(new JavaTimeModule());

        CsvSchema schema = csvMapper.schemaFor(Transaction.class)
                .withHeader()
                .withColumnSeparator(',');

        try (MappingIterator<Transaction> iterator = csvMapper.readerFor(Transaction.class)
                .with(schema)
                .readValues(inputStream)) {

            return iterator.readAll();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
