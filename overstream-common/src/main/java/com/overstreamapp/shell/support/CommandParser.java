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

package com.overstreamapp.shell.support;

import com.overstreamapp.shell.CommandRequest;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.overstreamapp.shell.support.Token.*;

class CommandParser {

    private static CommandParserException unexpectedSymbol(Lexer lexer) {
        return new CommandParserException(lexer, "Unexpected symbol: " + lexer.getValue());
    }

    CommandRequest parse(String source) throws CommandParserException {
        return parse(new StringReader(source));
    }

    CommandRequest parse(Reader reader) throws CommandParserException {
        return parseCommand(new Lexer(reader));
    }

    private CommandRequest parseCommand(Lexer lexer) throws CommandParserException {
        lexer.next();

        if (lexer.getToken() != TK_STRING) {
            throw unexpectedSymbol(lexer);
        }

        var arguments = new ArrayList<>();
        var namedArguments = new HashMap<String, Object>();

        var command = lexer.getValue();
        parseArguments(lexer, arguments, namedArguments);

        return new CommandRequest(command, arguments, namedArguments);
    }

    private void parseArguments(Lexer lexer, List<Object> arguments, Map<String, Object> namedArguments) throws CommandParserException {
        lexer.next();

        while (lexer.getToken() != TK_EOS) {
            if (!isValueToken(lexer.getToken())) {
                throw unexpectedSymbol(lexer);
            }

            var key = getJavaValue(lexer);

            lexer.next();

            if (lexer.getToken() != TK_EQ) {
                arguments.add(key);
                continue;
            }

            lexer.next();

            if(key != null) {
                namedArguments.put(key.toString(), getJavaValue(lexer));
            }

            lexer.next();
        }
    }

    private static Object getJavaValue(Lexer lexer) throws CommandParserException {
        var t = lexer.getToken();
        var v = lexer.getValue();
        switch (t) {
            case TK_STRING:
                return v;
            case TK_NUMBER:
                if (v.indexOf('.') > 0) {
                    return Double.parseDouble(v);
                } else {
                    return Integer.parseInt(v);
                }
            case TK_BOOL:
                return v.equalsIgnoreCase("true");
            case TK_NULL:
                return null;
            default:
                throw unexpectedSymbol(lexer);
        }
    }

    private static boolean isValueToken(Token t) {
        return t == TK_NULL || t == TK_BOOL || t == TK_NUMBER || t == TK_STRING;
    }
}
