/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.basjes.parse.useragent.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringTable {

    private List<String> headers = new ArrayList<>();
    private List<List<String>> lines = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        List<Integer> columnWidths = new ArrayList<>();

        for (int column = 0; column < headers.size(); column++) {
            int maxWidth = headers.get(column).length();
            for (List<String> line : lines) {
                if (!line.isEmpty() && line.size() > column) {
                    String columnValue = line.get(column);
                    if (columnValue != null) {
                        maxWidth = Math.max(maxWidth, columnValue.length());
                    }
                }
            }
            columnWidths.add(maxWidth);
        }
        writeSeparator(sb, columnWidths);
        writeLine(sb, columnWidths, headers);
        writeSeparator(sb, columnWidths);
        for (List<String> line : lines) {
            writeLine(sb, columnWidths, line);
        }
        writeSeparator(sb, columnWidths);
        return sb.toString();
    }

    private void repeatedChar(StringBuilder sb, char charr, int length) {
        if (length <= 0) {
            return;
        }
        for (int i = 0; i < length; i++) {
            sb.append(charr);
        }
    }

    private void writeSeparator(StringBuilder sb, List<Integer> columnWidths) {
        boolean first = true;
        for (Integer columnWidth : columnWidths) {
            if (first) {
                sb.append('|');
                first = false;
            } else {
                sb.append('+');
            }
            repeatedChar(sb, '-', columnWidth + 2);
        }
        sb.append('|');
        sb.append('\n');
    }

    private void writeLine(StringBuilder sb, List<Integer> columnWidths, List<String> fields) {
        if (fields.isEmpty()) {
            writeSeparator(sb, columnWidths);
            return;
        }

        int columns = Math.max(columnWidths.size(), fields.size());

        for (int columnNr = 0; columnNr < columns; columnNr++) {
            int columnWidth = 1;
            if (columnNr < columnWidths.size()) {
                columnWidth = columnWidths.get(columnNr);
            }
            if (columnNr <= columnWidths.size()) {
                sb.append('|');
            }
            String field = "";
            if (columnNr < fields.size()) {
                field = fields.get(columnNr);
            }
            sb.append(String.format(" %-" + columnWidth + "s ", field)); // NOSONAR java:S3457 This is creative, I know.
        }
        if (columns <= columnWidths.size()) {
            sb.append('|');
        }
        sb.append('\n');
    }

    public StringTable withHeaders(String... fields) {
        this.headers = Arrays.asList(fields);
        return this;
    }

    public StringTable withHeaders(List<String> fields) {
        this.headers = new ArrayList<>(fields);
        return this;
    }

    public StringTable addRow(String... fields) {
        this.lines.add(Arrays.asList(fields));
        return this;
    }

    public StringTable addRow(List<String> fields) {
        this.lines.add(new ArrayList<>(fields));
        return this;
    }

    public StringTable addRowSeparator() {
        this.lines.add(new ArrayList<>()); // Empty array
        return this;
    }

}
