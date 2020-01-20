/*
 * Copyright 2019 Bunjlabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.overstreamapp.commands.parser;

import java.io.IOException;
import java.io.Reader;

import static com.overstreamapp.commands.parser.Token.*;

class Lexer {

    private final Reader r;
    private int column = -1;
    private int curr = -1;
    private String sval = null;
    private Token token = TK_ERROR;

    Lexer(Reader reader) {
        this.r = reader;
    }

    Token next() throws CommandParserException {
        sval = null;

        try {
            token = doNext();
        } catch (IOException e) {
            sval = e.getLocalizedMessage();
            token = TK_ERROR;
        }
        return token;
    }

    int getColumn() {
        return column;
    }

    String getValue() {
        return sval;
    }

    Token getToken() {
        return token;
    }

    private Token doNext() throws IOException, CommandParserException {
        if (curr < 0) {
            skip();
        }

        for (; ; ) {
            if (currIsEos()) return TK_EOS;

            switch (curr) {
                case '\r':
                case '\n':
                case ' ':
                case '\f':
                case '\t':
                    skip();
                    break;
                case '=':
                    skip();
                    return TK_EQ;
                case '"':
                case '\'':
                    nextString(curr);
                    return TK_STRING;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    nextNumber();
                    return TK_NUMBER;
                default: {
                    if (currIsKey()) {
                        nextWord();

                        switch (sval) {
                            case "true":
                            case "false":
                                return TK_BOOL;
                            case "null":
                                return TK_NULL;
                            default:
                                return TK_STRING;
                        }
                    } else {
                        sval = "" + (char) curr;
                        skip();
                        return TK_ERROR;
                    }
                }
            }

        }
    }

    private void nextNumber() throws IOException {
        StringBuilder sb = new StringBuilder();

        boolean isDouble = false;
        while (currIsDigit() || (!isDouble && curr == '.')) {
            if (curr == '.') isDouble = true;
            sb.append((char) curr);
            skip();
        }

        sval = sb.toString();
    }

    private void nextWord() throws IOException {
        StringBuilder sb = new StringBuilder();

        while (currIsKey() || currIsDigit()) {
            sb.append((char) curr);
            skip();
        }
        sval = sb.toString();
    }

    private void nextString(int delimiter) throws IOException, CommandParserException {
        StringBuilder sb = new StringBuilder();
        skip();
        while (curr != delimiter) {
            switch (curr) {
                case -1:
                case '\n':
                case '\r':
                    throw new CommandParserException(this, "Unfinished string");
                case '\\':
                    skip();
                    if (curr == 'n') {
                        sb.append('\n');
                    } else if (curr == 'r') {
                        sb.append('\r');
                    } else if (curr == 't') {
                        sb.append('\t');
                    } else if (curr == 'f') {
                        sb.append('\f');
                    } else if (curr == '\\') {
                        sb.append('\\');
                    } else {
                        throw new CommandParserException(this, "Unsupported char escape sequence: \\" + ((char) curr));
                    }
                    break;
                default:
                    sb.append((char) curr);
                    break;
            }
            skip();
        }
        skip();
        sval = sb.toString();
    }

    private void skip() throws IOException {
        curr = r.read();
        column++;
    }

    private boolean currIsDigit() {
        return (curr >= '0' && curr <= '9');
    }

    private boolean currIsKey() {
        return (curr >= 'A' && curr <= 'Z')
                || (curr >= 'a' && curr <= 'z')
                || "_-.".indexOf(curr) >= 0;
    }

    private boolean currIsEos() {
        return curr < 0;
    }

}
