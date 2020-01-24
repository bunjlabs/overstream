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

import com.overstreamapp.commands.CommandRequest;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class CommandParser {

    private static CommandParserException unexpectedSymbol(Lexer lexer) {
        return new CommandParserException(lexer, "Unexpected symbol: " + lexer.getValue());
    }

    public CommandRequest parse(String source) throws CommandParserException {
        return parse(new StringReader(source));
    }

    public CommandRequest parse(Reader reader) throws CommandParserException {
        return parseCommand(new Lexer(reader));
    }

    private CommandRequest parseCommand(Lexer lexer) throws CommandParserException {
        lexer.next();

        if (lexer.getToken() != Token.TK_STRING) {
            throw unexpectedSymbol(lexer);
        }

        var command = lexer.getValue();
        var parameters = parseParameters(lexer);

        return new CommandRequest(command, parameters);
    }

    private Map<String, Object> parseParameters(Lexer lexer) throws CommandParserException {
        lexer.next();

        var parameters = new HashMap<String, Object>();
        while (lexer.getToken() != Token.TK_EOS) {
            if (lexer.getToken() != Token.TK_STRING) {
                throw unexpectedSymbol(lexer);
            }

            var key = lexer.getValue();

            lexer.next();

            if (lexer.getToken() != Token.TK_EQ) {
                throw unexpectedSymbol(lexer);
            }

            lexer.next();

            Object value;
            switch (lexer.getToken()) {
                case TK_STRING:
                    value = lexer.getValue();
                    break;
                case TK_NUMBER:
                    if (lexer.getValue().indexOf('.') > 0) {
                        value = Double.parseDouble(lexer.getValue());
                    } else {
                        value = Integer.parseInt(lexer.getValue());
                    }
                    break;
                case TK_BOOL:
                    value = lexer.getValue().equalsIgnoreCase("true");
                    break;
                default:
                    throw unexpectedSymbol(lexer);
            }

            lexer.next();

            parameters.put(key, value);
        }

        return parameters;
    }
}
